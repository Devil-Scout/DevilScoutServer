package org.victorrobotics.devilscoutserver.auth;

import org.victorrobotics.devilscoutserver.RequestHandler;
import org.victorrobotics.devilscoutserver.database.UserDB;
import org.victorrobotics.devilscoutserver.database.User;
import org.victorrobotics.devilscoutserver.database.Session;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;

@OpenApi(path = "/auth", methods = HttpMethod.POST, tags = "Login",
         description = "Authenticates a client. Must have already called `/login` to compute clientProof.",
         requestBody = @OpenApiRequestBody(required = true,
                                           content = @OpenApiContent(from = SCRAM_AuthHandler.AuthRequest.class)),
         responses = { @OpenApiResponse(status = "200", description = "Authentication successful"),
                       @OpenApiResponse(status = "400", description = "Bad request"),
                       @OpenApiResponse(status = "401", description = "Invalid credentials"),
                       @OpenApiResponse(status = "404", description = "Unknown user") })
public class SCRAM_AuthHandler extends RequestHandler {
  private static final String HASH_ALGORITHM = "SHA-256";
  private static final String HMAC_ALGORITHM = "HmacSHA256";

  private final UserDB       users;
  private final SecureRandom random;

  public SCRAM_AuthHandler(UserDB database) {
    this.users = database;
    this.random = new SecureRandom();
  }

  @Override
  public void handle(Context ctx) throws Exception {
    AuthRequest request = ctx.bodyAsClass(AuthRequest.class);
    if (request == null) {
      ctx.status(400);
      return;
    }

    User user = users.get(request.team, request.username);
    if (user == null) {
      ctx.status(404);
      return;
    }

    MessageDigest hashFunction;
    Mac hmacFunction;
    try {
      hashFunction = MessageDigest.getInstance(HASH_ALGORITHM);
      hmacFunction = Mac.getInstance(HMAC_ALGORITHM);
      hmacFunction.init(new SecretKeySpec(user.storedKey(), HMAC_ALGORITHM));
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new IllegalStateException(e);
    }

    String userID = request.team + request.username;
    byte[] userHash = hashFunction.digest(userID.getBytes());
    byte[] nonceHash = hashFunction.digest(request.nonce);
    if (!users.containsNonce(userHash, nonceHash)) {
      ctx.status(401);
      return;
    }

    byte[] userAndNonce = toStr(request.team + request.username, request.nonce);
    byte[] clientSignature = hmacFunction.doFinal(userAndNonce);
    byte[] clientKey = xor(request.clientProof, clientSignature);
    byte[] storedKey = hashFunction.digest(clientKey);
    if (!Arrays.equals(storedKey, user.storedKey())) {
      ctx.status(401);
      return;
    }

    try {
      hmacFunction.init(new SecretKeySpec(user.serverKey(), HMAC_ALGORITHM));
    } catch (InvalidKeyException e) {
      throw new IllegalStateException(e);
    }
    byte[] serverSignature = hmacFunction.doFinal(userAndNonce);

    Session session = generateSession(user);
    ctx.json(Map.of("serverSignature", base64Encode(serverSignature), "sessionID",
                    session.sessionID, "accessLevel", user.permissions(), "fullName",
                    user.fullName()));
  }

  private Session generateSession(User credentials) {
    long sessionID = random.nextLong();
    return new Session(sessionID, credentials.userID(), credentials.permissions());
  }

  private static byte[] xor(byte[] bytes1, byte[] bytes2) {
    assert bytes1.length == bytes2.length;
    byte[] bytes = new byte[bytes1.length];
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = (byte) (bytes1[i] ^ bytes2[i]);
    }
    return bytes;
  }

  private static byte[] toStr(String username, byte[] nonce) {
    byte[] bytes = new byte[username.length() + nonce.length];
    byte[] userBytes = username.getBytes();
    System.arraycopy(userBytes, 0, bytes, 0, userBytes.length);
    System.arraycopy(nonce, 0, bytes, userBytes.length, nonce.length);
    return bytes;
  }

  static record AuthRequest(@OpenApiExample("1559") int team,
                            @OpenApiExample("xander") String username,
                            @OpenApiExample("EjRWeJCrze8SNFZ4kKvN7w==") byte[] nonce,
                            @OpenApiExample("EjRWeJCrze8SNFZ4kKvN7xI0VniQq83vEjRWeJCrze8=") byte[] clientProof) {
    @JsonCreator
    AuthRequest(@JsonProperty("team") int team, @JsonProperty("username") String username,
                @JsonProperty("nonce") String nonce,
                @JsonProperty("clientProof") String clientProof) {
      this(team, username, base64Decode(nonce), base64Decode(clientProof));
    }
  }
}
