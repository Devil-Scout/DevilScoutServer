package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.database.Session;
import org.victorrobotics.devilscoutserver.database.User;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

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
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = LoginController.LoginChallenge.class)),
                         @OpenApiResponse(status = "400"), @OpenApiResponse(status = "404") })
  public static void login(Context ctx) {
    LoginRequest request = jsonDecode(ctx, LoginRequest.class);
    byte[] salt = userDB().getSalt(request.team, request.username);
    if (salt == null) {
      throw new NotFoundResponse();
    }

    byte[] nonce = generateNonce(request.clientNonce);
    String nonceID = request.team + "," + request.username + "," + base64Encode(nonce);
    userDB().putNonce(nonceID);

    ctx.json(new LoginChallenge(salt, nonce));
  }

  @OpenApi(path = "/auth", methods = HttpMethod.POST, tags = "Login",
           description = "Authenticates a client. Must have already called `/login` to compute clientProof.",
           requestBody = @OpenApiRequestBody(required = true,
                                             content = @OpenApiContent(from = LoginController.AuthRequest.class)),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = LoginController.AuthResponse.class)),
                         @OpenApiResponse(status = "400"), @OpenApiResponse(status = "401"),
                         @OpenApiResponse(status = "404") })
  public static void auth(Context ctx) throws NoSuchAlgorithmException, InvalidKeyException {
    AuthRequest request = jsonDecode(ctx, AuthRequest.class);
    User user = userDB().getUser(request.team, request.username);
    if (user == null) {
      throw new NotFoundResponse();
    }

    String nonceID = request.team + "," + request.username + "," + base64Encode(request.nonce);
    if (!userDB().containsNonce(nonceID)) {
      throw new UnauthorizedResponse();
    }

    MessageDigest hashFunction = MessageDigest.getInstance(HASH_ALGORITHM);
    Mac hmacFunction = Mac.getInstance(HMAC_ALGORITHM);
    hmacFunction.init(new SecretKeySpec(user.storedKey(), HMAC_ALGORITHM));

    byte[] userAndNonce = combine(request.team + request.username, request.nonce);
    byte[] clientSignature = hmacFunction.doFinal(userAndNonce);
    byte[] clientKey = xor(request.clientProof, clientSignature);
    byte[] storedKey = hashFunction.digest(clientKey);
    if (!MessageDigest.isEqual(user.storedKey(), storedKey)) {
      throw new UnauthorizedResponse();
    }

    hmacFunction.init(new SecretKeySpec(user.serverKey(), HMAC_ALGORITHM));
    byte[] serverSignature = hmacFunction.doFinal(userAndNonce);
    userDB().removeNonce(nonceID);

    Session session = generateSession(user);
    ctx.json(new AuthResponse(user, session, serverSignature));
  }

  private static byte[] generateNonce(byte[] clientNonce) {
    byte[] nonce = new byte[16];
    RANDOM.nextBytes(nonce);
    System.arraycopy(clientNonce, 0, nonce, 0, 8);
    return nonce;
  }

  private static byte[] xor(byte[] bytes1, byte[] bytes2) {
    byte[] bytes = new byte[bytes1.length];
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = (byte) (bytes1[i] ^ bytes2[i]);
    }
    return bytes;
  }

  private static byte[] combine(String username, byte[] nonce) {
    byte[] bytes = new byte[username.length() + nonce.length];
    byte[] userBytes = username.getBytes();
    System.arraycopy(userBytes, 0, bytes, 0, userBytes.length);
    System.arraycopy(nonce, 0, bytes, userBytes.length, nonce.length);
    return bytes;
  }

  private static Session generateSession(User user) {
    byte[] sessionID = new byte[8];
    RANDOM.nextBytes(sessionID);
    return new Session(sessionID, user.userID(), user.accessLevel());
  }

  static record LoginRequest(@OpenApiRequired @OpenApiExample("1559") int team,
                             @OpenApiRequired @OpenApiExample("xander") String username,
                             @OpenApiRequired @OpenApiExample("EjRWeJCrze8=") byte[] clientNonce) {}

  static record LoginChallenge(@OpenApiRequired @OpenApiExample("mHZUMhCrze8=") byte[] salt,
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
                             @OpenApiRequired @OpenApiExample("K9UoTnrEY94=") byte[] sessionID,
                             @OpenApiRequired
                             @OpenApiExample("m7squ/lkrdjWSAER1g84uxQm3yDAOYUtVfYEJeYR2Tw=") byte[] serverSignature) {
    AuthResponse(User user, Session session, byte[] serverSignature) {
      this(user.fullName(), user.accessLevel(), session.sessionID, serverSignature);
    }
  }
}
