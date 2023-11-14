package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.Main;
import org.victorrobotics.devilscoutserver.controller.SessionController.AuthRequest;
import org.victorrobotics.devilscoutserver.controller.SessionController.AuthResponse;
import org.victorrobotics.devilscoutserver.controller.SessionController.LoginChallenge;
import org.victorrobotics.devilscoutserver.controller.SessionController.LoginRequest;
import org.victorrobotics.devilscoutserver.database.User;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SessionIntegrationTest {
  @BeforeAll
  static void startServer() {
    Main.main();
  }

  @Test
  void testServer() throws IOException, InterruptedException {
    // Client setup
    int team = 1559;
    String name = "xander";
    String password = "password";

    HttpClient client = HttpClient.newHttpClient();
    ObjectMapper json = new ObjectMapper();

    SecureRandom random = new SecureRandom();
    byte[] clientNonce = new byte[8];
    random.nextBytes(clientNonce);

    // Login Request
    LoginRequest loginRequest = new LoginRequest(1559, "xander", clientNonce);
    HttpResponse<String> response =
        client.send(HttpRequest.newBuilder(URI.create("http://localhost:8000/sessions/login"))
                               .POST(BodyPublishers.ofString(json.writeValueAsString(loginRequest)))
                               .build(),
                    BodyHandlers.ofString());
    assertNotNull(response);
    assertEquals(200, response.statusCode());
    String responseBody = response.body();
    assertNotNull(responseBody);

    LoginChallenge challenge = json.readValue(responseBody, LoginChallenge.class);
    assertNotNull(challenge);
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

    KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 4096, 256);
    try {
      SecretKey saltedPassword = factory.generateSecret(keySpec);
      hmacFunction.init(saltedPassword);
    } catch (InvalidKeySpecException | InvalidKeyException e) {
      throw new IllegalStateException(e);
    }

    byte[] clientKey = hmacFunction.doFinal("Client Key".getBytes());
    byte[] serverKey = hmacFunction.doFinal("Server Key".getBytes());
    byte[] storedKey = hashFunction.digest(clientKey);

    try {
      hmacFunction.init(new SecretKeySpec(storedKey, "HmacSHA256"));
    } catch (InvalidKeyException e) {
      throw new IllegalStateException(e);
    }
    byte[] clientSignature = hmacFunction.doFinal(toStr(team + name, nonce));
    byte[] clientProof = new byte[32];
    for (int i = 0; i < clientProof.length; i++) {
      clientProof[i] = (byte) (clientSignature[i] ^ clientKey[i]);
    }

    try {
      hmacFunction.init(new SecretKeySpec(serverKey, "HmacSHA256"));
    } catch (InvalidKeyException e) {
      throw new IllegalStateException(e);
    }
    byte[] serverSignature = hmacFunction.doFinal(toStr(team + name, nonce));

    // Auth Request
    AuthRequest authRequest = new AuthRequest(1559, "xander", nonce, clientProof);
    response = client.send(HttpRequest.newBuilder(URI.create("http://localhost:8000/sessions/auth"))
                                      .POST(BodyPublishers.ofString(json.writeValueAsString(authRequest)))
                                      .build(),
                           BodyHandlers.ofString());
    assertNotNull(response);
    assertEquals(201, response.statusCode());
    responseBody = response.body();
    assertNotNull(responseBody);

    AuthResponse authResponse = json.readValue(responseBody, AuthResponse.class);
    assertNotNull(authResponse);
    assertEquals(User.AccessLevel.SUDO, authResponse.accessLevel());
    assertEquals("Xander Bhalla", authResponse.fullName());
    String sessionID = authResponse.sessionID();
    assertNotNull(sessionID);
    assertArrayEquals(serverSignature, authResponse.serverSignature());

    // Logout
    response = client.send(HttpRequest.newBuilder(URI.create("http://localhost:8000/sessions"))
                                      .DELETE()
                                      .header(Controller.SESSION_HEADER, sessionID)
                                      .build(),
                           BodyHandlers.ofString());
    assertEquals(200, response.statusCode());
    responseBody = response.body();
    assertTrue(responseBody == null || responseBody.length() == 0);

    // Second logout should fail
    response = client.send(response.request(), BodyHandlers.ofString());
    assertNotEquals(200, response.statusCode());
  }

  private static byte[] toStr(String username, byte[] nonce) {
    byte[] bytes = new byte[username.length() + nonce.length];
    byte[] userBytes = username.getBytes();
    System.arraycopy(userBytes, 0, bytes, 0, userBytes.length);
    System.arraycopy(nonce, 0, bytes, userBytes.length, nonce.length);
    return bytes;
  }
}
