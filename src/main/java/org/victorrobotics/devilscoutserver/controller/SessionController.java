package org.victorrobotics.devilscoutserver.controller;

import static org.victorrobotics.devilscoutserver.Utils.base64Encode;

import org.victorrobotics.devilscoutserver.data.AuthRequest;
import org.victorrobotics.devilscoutserver.data.AuthResponse;
import org.victorrobotics.devilscoutserver.data.LoginChallenge;
import org.victorrobotics.devilscoutserver.data.LoginRequest;
import org.victorrobotics.devilscoutserver.data.Session;
import org.victorrobotics.devilscoutserver.data.User;

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
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;

public final class SessionController extends Controller {
  private static final String HASH_ALGORITHM = "SHA-256";
  private static final String HMAC_ALGORITHM = "HmacSHA256";

  private static final SecureRandom RANDOM = new SecureRandom();

  private SessionController() {}

  @OpenApi(path = "/login", methods = HttpMethod.POST, tags = "Authentication",
           description = "Requests a login challenge. Must be called before `/auth`.",
           requestBody = @OpenApiRequestBody(required = true,
                                             content = @OpenApiContent(from = LoginRequest.class)),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = LoginChallenge.class)),
                         @OpenApiResponse(status = "400"), @OpenApiResponse(status = "404") })
  public static void login(Context ctx) {
    LoginRequest request = jsonDecode(ctx, LoginRequest.class);
    byte[] salt = userDB().getSalt(request.team(), request.username());
    if (salt == null) {
      throw new NotFoundResponse("Unknown User");
    }

    byte[] nonce = generateNonce(request.clientNonce());
    String nonceID = request.team() + "," + request.username() + "," + base64Encode(nonce);
    userDB().putNonce(nonceID);

    ctx.json(new LoginChallenge(salt, nonce));
  }

  @OpenApi(path = "/auth", methods = HttpMethod.POST, tags = "Authentication",
           description = "Authenticates a client. Must have already called `/login` to compute clientProof.",
           requestBody = @OpenApiRequestBody(required = true,
                                             content = @OpenApiContent(from = AuthRequest.class)),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = AuthResponse.class)),
                         @OpenApiResponse(status = "400"), @OpenApiResponse(status = "401"),
                         @OpenApiResponse(status = "404") })
  public static void auth(Context ctx) throws NoSuchAlgorithmException, InvalidKeyException {
    AuthRequest request = jsonDecode(ctx, AuthRequest.class);
    User user = userDB().getUser(request.team(), request.username());
    if (user == null) {
      throw new NotFoundResponse("Unknown User");
    }

    String nonceID =
        request.team() + "," + request.username() + "," + base64Encode(request.nonce());
    if (!userDB().containsNonce(nonceID)) {
      throw new UnauthorizedResponse("Invalid Nonce");
    }

    MessageDigest hashFunction = MessageDigest.getInstance(HASH_ALGORITHM);
    Mac hmacFunction = Mac.getInstance(HMAC_ALGORITHM);
    hmacFunction.init(new SecretKeySpec(user.storedKey(), HMAC_ALGORITHM));

    byte[] userAndNonce = combine(request.team() + request.username(), request.nonce());
    byte[] clientSignature = hmacFunction.doFinal(userAndNonce);
    byte[] clientKey = xor(request.clientProof(), clientSignature);
    byte[] storedKey = hashFunction.digest(clientKey);
    if (!MessageDigest.isEqual(user.storedKey(), storedKey)) {
      throw new UnauthorizedResponse("Incorrect Proof");
    }

    hmacFunction.init(new SecretKeySpec(user.serverKey(), HMAC_ALGORITHM));
    byte[] serverSignature = hmacFunction.doFinal(userAndNonce);
    userDB().removeNonce(nonceID);

    Session session = generateSession(user);
    ctx.json(new AuthResponse(user, session, serverSignature));
  }

  @OpenApi(path = "/logout", methods = HttpMethod.DELETE, tags = "Authentication",
           description = "Invalidates a session, logging a client out.",
           responses = { @OpenApiResponse(status = "204"), @OpenApiResponse(status = "401") })
  public static void logout(Context ctx) {
    Session session = getValidSession(ctx);
    sessionDB().deleteSession(session);
    ctx.status(204);
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
    Session session =
        new Session(base64Encode(sessionID), user.userID(), user.team(), user.accessLevel());
    sessionDB().registerSession(session);
    return session;
  }
}
