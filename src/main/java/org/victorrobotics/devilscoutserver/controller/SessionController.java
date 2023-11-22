package org.victorrobotics.devilscoutserver.controller;

import static org.victorrobotics.devilscoutserver.Utils.base64Encode;

import org.victorrobotics.devilscoutserver.data.Session;
import org.victorrobotics.devilscoutserver.data.UserAccessLevel;
import org.victorrobotics.devilscoutserver.data.UserInfo;
import org.victorrobotics.devilscoutserver.database.User;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.javalin.http.Context;
import io.javalin.http.NoContentResponse;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiRequired;
import io.javalin.openapi.OpenApiResponse;
import io.javalin.openapi.OpenApiSecurity;

@SuppressWarnings("java:S6218") // override equals, hashCode, toString
public final class SessionController extends Controller {
  private SessionController() {}

  public static void generateDevSession(Context ctx) {
    Session session = new Session(-1, -1, 1559, UserAccessLevel.SUDO);
    sessionDB().registerSession(session);
    ctx.json(session);
  }

  @OpenApi(path = "/login", methods = HttpMethod.POST, tags = "Authentication",
           summary = "unauthorized",
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
    String nonceId = request.team() + "," + request.username() + "," + base64Encode(nonce);
    userDB().putNonce(nonceId);

    ctx.json(new LoginChallenge(salt, nonce));
  }

  @OpenApi(path = "/auth", methods = HttpMethod.POST, tags = "Authentication",
           summary = "unauthorized",
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

    String nonceId =
        request.team() + "," + request.username() + "," + base64Encode(request.nonce());
    if (!userDB().containsNonce(nonceId)) {
      throw new UnauthorizedResponse("Invalid Nonce");
    }

    MessageDigest hashFunction = MessageDigest.getInstance(HASH_ALGORITHM);
    Mac hmacFunction = Mac.getInstance(MAC_ALGORITHM);
    hmacFunction.init(new SecretKeySpec(user.storedKey(), MAC_ALGORITHM));

    byte[] userAndNonce = combine(request.team() + request.username(), request.nonce());
    byte[] clientSignature = hmacFunction.doFinal(userAndNonce);
    byte[] clientKey = xor(request.clientProof(), clientSignature);
    byte[] storedKey = hashFunction.digest(clientKey);
    if (!MessageDigest.isEqual(user.storedKey(), storedKey)) {
      throw new UnauthorizedResponse("Incorrect Proof");
    }

    hmacFunction.init(new SecretKeySpec(user.serverKey(), MAC_ALGORITHM));
    byte[] serverSignature = hmacFunction.doFinal(userAndNonce);
    userDB().removeNonce(nonceId);

    Session session = new Session(SECURE_RANDOM.nextLong(1L << 53), user);
    sessionDB().registerSession(session);
    ctx.json(new AuthResponse(user.info(), session, serverSignature));
  }

  @OpenApi(path = "/logout", methods = HttpMethod.DELETE, tags = "Authentication", summary = "USER",
           description = "Invalidates a session, logging a client out.",
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "204"), @OpenApiResponse(status = "401") })
  public static void logout(Context ctx) {
    Session session = getValidSession(ctx);
    sessionDB().deleteSession(session);
    throw new NoContentResponse();
  }

  private static byte[] generateNonce(byte[] clientNonce) {
    byte[] nonce = new byte[16];
    SECURE_RANDOM.nextBytes(nonce);
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

  public static record LoginRequest(@OpenApiExample("1559") @OpenApiRequired
  @JsonProperty(required = true) int team,
                                    @OpenApiExample("xander") @OpenApiRequired
                                    @JsonProperty(required = true) String username,
                                    @OpenApiExample("EjRWeJCrze8=") @OpenApiRequired
                                    @JsonProperty(required = true) byte[] clientNonce) {}

  public static record LoginChallenge(@OpenApiExample("mHZUMhCrze8=") @OpenApiRequired byte[] salt,
                                      @OpenApiExample("EjRWeJCrze8SNFZ4kKvN7w==")
                                      @OpenApiRequired byte[] nonce) {}

  public static record AuthRequest(@OpenApiExample("1559") @OpenApiRequired
  @JsonProperty(required = true) int team,
                                   @OpenApiExample("xander") @OpenApiRequired
                                   @JsonProperty(required = true) String username,
                                   @OpenApiExample("EjRWeJCrze8SNFZ4kKvN7w==") @OpenApiRequired
                                   @JsonProperty(required = true) byte[] nonce,
                                   @OpenApiExample("EjRWeJCrze8SNFZ4kKvN7xI0VniQq83vEjRWeJCrze8=")
                                   @OpenApiRequired
                                   @JsonProperty(required = true) byte[] clientProof) {}

  public static record AuthResponse(@OpenApiRequired UserInfo user,
                                    @OpenApiRequired Session session,
                                    @OpenApiExample("m7squ/lkrdjWSAER1g84uxQm3yDAOYUtVfYEJeYR2Tw=")
                                    @OpenApiRequired byte[] serverSignature) {}
}
