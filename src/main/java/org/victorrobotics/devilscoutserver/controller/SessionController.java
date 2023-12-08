package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.database.Session;
import org.victorrobotics.devilscoutserver.database.User;
import org.victorrobotics.devilscoutserver.database.UserAccessLevel;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiRequired;
import io.javalin.openapi.OpenApiResponse;
import io.javalin.openapi.OpenApiSecurity;
import io.javalin.validation.JavalinValidation;

@SuppressWarnings("java:S6218") // override equals for array content
public final class SessionController extends Controller {
  static final Set<String> NONCES = Collections.newSetFromMap(new ConcurrentHashMap<>());

  private SessionController() {}

  @OpenApi(path = "/dev_session/{accessLevel}", methods = HttpMethod.GET,
           summary = "unauthorized ( REMOVE FOR PRODUCTION )",
           description = "Starts a development session with id -1 and the specified accessLevel. "
               + "The session is not specific to a particular user, but it is a member of team 1559.",
           pathParams = @OpenApiParam(name = "accessLevel", type = UserAccessLevel.class,
                                      required = true),
           responses = @OpenApiResponse(status = "200",
                                        content = @OpenApiContent(from = Session.class)))
  public static void generateDevSession(Context ctx) {
    JavalinValidation.register(UserAccessLevel.class, UserAccessLevel::valueOf);
    UserAccessLevel accessLevel = ctx.pathParamAsClass("accessLevel", UserAccessLevel.class)
                                     .get();
    Session session = new Session(-1, -1, 1559, accessLevel);
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
                         @OpenApiResponse(status = "400",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "404",
                                          content = @OpenApiContent(from = Error.class)) })
  public static void login(Context ctx) {
    LoginRequest request = jsonDecode(ctx, LoginRequest.class);
    int team = request.team();
    String username = request.username();

    byte[] salt = userDB().getSalt(team, username);
    if (salt == null) {
      throwUserNotFound(username, team);
    }

    byte[] nonce = new byte[16];
    SECURE_RANDOM.nextBytes(nonce);
    System.arraycopy(request.clientNonce(), 0, nonce, 0, 8);

    String nonceId = team + "," + username + "," + base64Encode(nonce);
    NONCES.add(nonceId);

    ctx.json(new LoginChallenge(salt, nonce));
  }

  @OpenApi(path = "/auth", methods = HttpMethod.POST, tags = "Authentication",
           summary = "unauthorized",
           description = "Authenticates a client. Must have already called `/login` to compute clientProof.",
           requestBody = @OpenApiRequestBody(required = true,
                                             content = @OpenApiContent(from = AuthRequest.class)),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = AuthResponse.class)),
                         @OpenApiResponse(status = "400",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "404",
                                          content = @OpenApiContent(from = Error.class)) })
  public static void auth(Context ctx) throws NoSuchAlgorithmException, InvalidKeyException {
    AuthRequest request = jsonDecode(ctx, AuthRequest.class);
    String username = request.username();
    int team = request.team();

    User user = userDB().getUser(team, username);
    if (user == null) {
      throwUserNotFound(username, team);
    }

    String nonceId = team + "," + username + "," + base64Encode(request.nonce());
    if (!NONCES.contains(nonceId)) {
      throw new NotFoundResponse("Invalid nonce");
    }

    MessageDigest hashFunction = MessageDigest.getInstance(HASH_ALGORITHM);
    Mac hmacFunction = Mac.getInstance(MAC_ALGORITHM);
    hmacFunction.init(new SecretKeySpec(user.getStoredKey(), MAC_ALGORITHM));

    byte[] userAndNonce = combine(team + username, request.nonce());
    byte[] clientSignature = hmacFunction.doFinal(userAndNonce);
    byte[] clientKey = xor(request.clientProof(), clientSignature);
    byte[] storedKey = hashFunction.digest(clientKey);
    if (!MessageDigest.isEqual(user.getStoredKey(), storedKey)) {
      throw new UnauthorizedResponse("Incorrect credentials for user " + username + "@" + team);
    }

    hmacFunction.init(new SecretKeySpec(user.getServerKey(), MAC_ALGORITHM));
    byte[] serverSignature = hmacFunction.doFinal(userAndNonce);
    NONCES.remove(nonceId);

    Session session = new Session(SECURE_RANDOM.nextLong(1L << 53), user);
    sessionDB().registerSession(session);
    ctx.json(new AuthResponse(user, session, serverSignature));
  }

  @OpenApi(path = "/session", methods = HttpMethod.GET, tags = "Authentication", summary = "USER",
           description = "Get the current status of your session.",
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = Session.class)),
                         @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = Error.class)) })
  public static void getSession(Context ctx) {
    Session session = getValidSession(ctx);
    ctx.json(session);
  }

  @OpenApi(path = "/logout", methods = HttpMethod.DELETE, tags = "Authentication", summary = "USER",
           description = "Invalidates a session, logging a client out.",
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "204"),
                         @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = Error.class)) })
  public static void logout(Context ctx) {
    Session session = getValidSession(ctx);
    sessionDB().deleteSession(session);
    throwNoContent();
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

  public static record AuthResponse(@OpenApiRequired User user,
                                    @OpenApiRequired Session session,
                                    @OpenApiExample("m7squ/lkrdjWSAER1g84uxQm3yDAOYUtVfYEJeYR2Tw=")
                                    @OpenApiRequired byte[] serverSignature) {}
}
