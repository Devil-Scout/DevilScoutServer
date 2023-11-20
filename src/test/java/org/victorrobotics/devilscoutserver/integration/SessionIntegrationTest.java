package org.victorrobotics.devilscoutserver.integration;

import static org.victorrobotics.devilscoutserver.Utils.base64Decode;

import org.victorrobotics.devilscoutserver.Server;
import org.victorrobotics.devilscoutserver.controller.Controller;
import org.victorrobotics.devilscoutserver.data.AuthRequest;
import org.victorrobotics.devilscoutserver.data.AuthResponse;
import org.victorrobotics.devilscoutserver.data.LoginChallenge;
import org.victorrobotics.devilscoutserver.data.LoginRequest;
import org.victorrobotics.devilscoutserver.data.User;
import org.victorrobotics.devilscoutserver.data.UserAccessLevel;
import org.victorrobotics.devilscoutserver.database.SessionDB;
import org.victorrobotics.devilscoutserver.database.UserDB;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.stream.Stream;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class SessionIntegrationTest {
  private static Server server = new Server();

  @BeforeAll
  static void startServer() {
    Controller.setSessionDB(new SessionDB());
    server.start();
  }

  @AfterAll
  static void stopServer() {
    server.stop();
  }

  @ParameterizedTest
  @MethodSource("testCases")
  void testLoginSequence(TestCase testCase) throws IOException, InterruptedException {
    testCase.inject();

    HttpClient client = HttpClient.newHttpClient();
    ObjectMapper json = new ObjectMapper();

    SecureRandom random = new SecureRandom();
    byte[] clientNonce = new byte[8];
    random.nextBytes(clientNonce);

    // Login Request
    LoginRequest loginRequest =
        new LoginRequest(testCase.user.team(), testCase.user.username(), clientNonce);
    HttpResponse<String> response =
        client.send(HttpRequest.newBuilder(URI.create("http://localhost:8000/login"))
                               .POST(BodyPublishers.ofString(json.writeValueAsString(loginRequest)))
                               .build(),
                    BodyHandlers.ofString());
    assertNotNull(response);
    assertEquals(200, response.statusCode());
    String responseBody = response.body();
    assertNotNull(responseBody);

    LoginChallenge challenge = json.readValue(responseBody, LoginChallenge.class);
    byte[] nonce = challenge.nonce();
    assertNotNull(nonce);
    assertEquals(16, nonce.length);
    assertArrayEquals(clientNonce, Arrays.copyOf(nonce, 8));
    byte[] salt = challenge.salt();
    assertNotNull(salt);
    assertEquals(8, salt.length);

    // Compute clientProof and serverSignature (thoroughly vetted)
    SecretKeyFactory factory;
    MessageDigest hashFunction;
    Mac hmacFunction;
    try {
      hashFunction = MessageDigest.getInstance("SHA-256");
      hmacFunction = Mac.getInstance("HmacSHA256");
      factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }

    KeySpec keySpec = new PBEKeySpec(testCase.password.toCharArray(), salt, 4096, 256);
    try {
      SecretKey saltedPassword = factory.generateSecret(keySpec);
      hmacFunction.init(saltedPassword);
    } catch (InvalidKeySpecException | InvalidKeyException e) {
      throw new IllegalStateException(e);
    }

    byte[] clientKey = hmacFunction.doFinal("Client Key".getBytes());
    byte[] serverKey = hmacFunction.doFinal("Server Key".getBytes());
    byte[] storedKey = hashFunction.digest(clientKey);

    byte[] userAndNonce = toStr(testCase.user.team() + testCase.user.username(), nonce);
    try {
      hmacFunction.init(new SecretKeySpec(storedKey, "HmacSHA256"));
    } catch (InvalidKeyException e) {
      throw new IllegalStateException(e);
    }
    byte[] clientSignature = hmacFunction.doFinal(userAndNonce);
    byte[] clientProof = new byte[32];
    for (int i = 0; i < clientProof.length; i++) {
      clientProof[i] = (byte) (clientSignature[i] ^ clientKey[i]);
    }

    try {
      hmacFunction.init(new SecretKeySpec(serverKey, "HmacSHA256"));
    } catch (InvalidKeyException e) {
      throw new IllegalStateException(e);
    }
    byte[] serverSignature = hmacFunction.doFinal(userAndNonce);

    // Auth Request
    AuthRequest authRequest =
        new AuthRequest(testCase.user.team(), testCase.user.username(), nonce, clientProof);
    response = client.send(HttpRequest.newBuilder(URI.create("http://localhost:8000/auth"))
                                      .POST(BodyPublishers.ofString(json.writeValueAsString(authRequest)))
                                      .build(),
                           BodyHandlers.ofString());
    assertNotNull(response);
    assertEquals(200, response.statusCode());
    responseBody = response.body();
    assertNotNull(responseBody);

    AuthResponse authResponse = json.readValue(responseBody, AuthResponse.class);
    assertNotNull(authResponse);
    assertEquals(UserAccessLevel.SUDO, authResponse.accessLevel());
    assertEquals(testCase.user.fullName(), authResponse.fullName());
    String sessionID = authResponse.sessionID();
    assertNotNull(sessionID);
    assertArrayEquals(serverSignature, authResponse.serverSignature());

    // Logout
    response = client.send(HttpRequest.newBuilder(URI.create("http://localhost:8000/logout"))
                                      .DELETE()
                                      .header(Controller.SESSION_HEADER, sessionID)
                                      .build(),
                           BodyHandlers.ofString());
    assertEquals(204, response.statusCode());

    // Second logout should fail
    response = client.send(response.request(), BodyHandlers.ofString());
    assertNotEquals(200, response.statusCode());
  }

  static byte[] toStr(String username, byte[] nonce) {
    byte[] bytes = new byte[username.length() + nonce.length];
    byte[] userBytes = username.getBytes();
    System.arraycopy(userBytes, 0, bytes, 0, userBytes.length);
    System.arraycopy(nonce, 0, bytes, userBytes.length, nonce.length);
    return bytes;
  }

  static Stream<TestCase> testCases() {
    return Stream.<TestCase>builder()
                 .add(new TestCase(new User((long) 5, "xander", "Xander Bhalla", 1559,
                                            UserAccessLevel.SUDO, base64Decode("YmFkLXNhbHQ="),
                                            base64Decode("jMeQaCzoJs81MobCQfcMSq4W298aAnSsF5WRGRf7U1s="),
                                            base64Decode("hsEcMmcap9WWLv+XYoT/gamB6b/P3tgOoOOIgbi26W8=")),
                                   "password"))
                 .build();
  }

  record TestCase(User user,
                  String password) {
    void inject() {
      UserDB users = new UserDB();
      users.addUser(user);
      Controller.setUserDB(users);
    }
  }
}
