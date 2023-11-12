package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.database.Session;
import org.victorrobotics.devilscoutserver.database.User;
import org.victorrobotics.devilscoutserver.database.UserDB;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiRequired;
import io.javalin.openapi.OpenApiResponse;

public final class LoginController extends Controller {
  private static final String HASH_ALGORITHM = "SHA-256";
  private static final String HMAC_ALGORITHM = "HmacSHA256";

  private static final SecureRandom RANDOM = new SecureRandom();

  private LoginController() {}

  @OpenApi(path = "/login", methods = HttpMethod.POST, tags = "Login",
           description = "Requests a login challenge. Must be called before `/auth`.",
           requestBody = @OpenApiRequestBody(required = true,
                                             content = @OpenApiContent(from = LoginController.LoginRequest.class)),
           responses = { @OpenApiResponse(status = "200", description = "Authentication successful",
                                          content = @OpenApiContent(from = LoginController.LoginResponse.class)),
                         @OpenApiResponse(status = "400"), @OpenApiResponse(status = "404") })
  public static void login(Context ctx) {
    LoginRequest request = jsonDecode(ctx, LoginRequest.class);
    byte[] salt = UserDB.INSTANCE.getSalt(request.team, request.username);
    if (salt == null) {
      throw new NotFoundResponse();
    }

    byte[] nonce = new byte[16];
    RANDOM.nextBytes(nonce);
    System.arraycopy(request.clientNonce, 0, nonce, 0, 8);
    String nonceID = request.team + "," + request.username + "," + base64Encode(nonce);
    UserDB.INSTANCE.putNonce(nonceID);

    ctx.json(new LoginResponse(salt, nonce));
  }

  @OpenApi(path = "/auth", methods = HttpMethod.POST, tags = "Login",
           description = "Authenticates a client. Must have already called `/login` to compute clientProof.",
           requestBody = @OpenApiRequestBody(required = true,
                                             content = @OpenApiContent(from = LoginController.AuthRequest.class)),
           responses = { @OpenApiResponse(status = "200", description = "Authentication successful",
                                          content = @OpenApiContent(from = LoginController.AuthResponse.class)),
                         @OpenApiResponse(status = "400"), @OpenApiResponse(status = "401"),
                         @OpenApiResponse(status = "404") })
  public static void auth(Context ctx) throws NoSuchAlgorithmException, InvalidKeyException {
    AuthRequest request = jsonDecode(ctx, AuthRequest.class);
    User user = UserDB.INSTANCE.getUser(request.team, request.username);
    if (user == null) {
      throw new NotFoundResponse();
    }

    MessageDigest hashFunction = MessageDigest.getInstance(HASH_ALGORITHM);
    Mac hmacFunction = Mac.getInstance(HMAC_ALGORITHM);
    hmacFunction.init(new SecretKeySpec(user.storedKey(), HMAC_ALGORITHM));

    String nonceID = request.team + "," + request.username + "," + base64Encode(request.nonce);
    if (!UserDB.INSTANCE.containsNonce(nonceID)) {
      throw new UnauthorizedResponse();
    }

    byte[] userAndNonce = toStr(request.team + request.username, request.nonce);
    byte[] clientSignature = hmacFunction.doFinal(userAndNonce);
    byte[] clientKey = xor(request.clientProof, clientSignature);
    byte[] storedKey = hashFunction.digest(clientKey);
    if (!Arrays.equals(storedKey, user.storedKey())) {
      throw new UnauthorizedResponse();
    }

    try {
      hmacFunction.init(new SecretKeySpec(user.serverKey(), HMAC_ALGORITHM));
    } catch (InvalidKeyException e) {
      throw new IllegalStateException(e);
    }
    byte[] serverSignature = hmacFunction.doFinal(userAndNonce);

    Session session =
        new Session(RANDOM.nextLong(Long.MAX_VALUE), user.userID(), user.permissions());
    ctx.json(new AuthResponse(user.fullName(), user.permissions(), session.sessionID,
                              serverSignature));
  }

  private static byte[] xor(byte[] bytes1, byte[] bytes2) {
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

  static record LoginRequest(@OpenApiRequired @OpenApiExample("1559") int team,
                             @OpenApiRequired @OpenApiExample("xander") String username,
                             @OpenApiRequired @OpenApiExample("EjRWeJCrze8=") byte[] clientNonce) {}

  static record LoginResponse(@OpenApiRequired @OpenApiExample("mHZUMhCrze8=") byte[] salt,
                              @OpenApiRequired
                              @OpenApiExample("EjRWeJCrze8SNFZ4kKvN7w==") byte[] nonce) {}

  static record AuthRequest(@OpenApiRequired @OpenApiExample("1559") int team,
                            @OpenApiRequired @OpenApiExample("xander") String username,
                            @OpenApiRequired
                            @OpenApiExample("EjRWeJCrze8SNFZ4kKvN7w==") byte[] nonce,
                            @OpenApiRequired
                            @OpenApiExample("EjRWeJCrze8SNFZ4kKvN7xI0VniQq83vEjRWeJCrze8=") byte[] clientProof) {}

  static record AuthResponse(@OpenApiRequired @OpenApiExample("Xander Bhalla") String fullName,
                             @OpenApiRequired @OpenApiExample("USER") User.AccessLevel accessLevel,
                             @OpenApiRequired @OpenApiExample("3123658432553584166") long sessionID,
                             @OpenApiRequired
                             @OpenApiExample("m7squ/lkrdjWSAER1g84uxQm3yDAOYUtVfYEJeYR2Tw=") byte[] serverSignature) {}
}
