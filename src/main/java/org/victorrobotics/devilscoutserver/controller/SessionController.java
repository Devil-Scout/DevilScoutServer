package org.victorrobotics.devilscoutserver.controller;

import static org.victorrobotics.devilscoutserver.EncodingUtil.base64Encode;

import org.victorrobotics.devilscoutserver.database.Team;
import org.victorrobotics.devilscoutserver.database.User;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiRequired;
import io.javalin.openapi.OpenApiResponse;
import io.javalin.openapi.OpenApiSecurity;

@SuppressWarnings("java:S6218") // override equals for array content
public final class SessionController extends Controller {
  static final Set<String> NONCES = Collections.newSetFromMap(new ConcurrentHashMap<>());

  private SessionController() {}

  @OpenApi(path = "/login", methods = HttpMethod.POST, tags = "Authentication",
           summary = "unauthorized",
           description = "Requests a login challenge. Must be called before `/auth`.",
           requestBody = @OpenApiRequestBody(required = true,
                                             content = @OpenApiContent(from = LoginRequest.class)),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = LoginChallenge.class)),
                         @OpenApiResponse(status = "400",
                                          content = @OpenApiContent(from = ApiError.class)),
                         @OpenApiResponse(status = "404",
                                          content = @OpenApiContent(from = ApiError.class)) })
  public static void login(Context ctx) throws SQLException {
    LoginRequest request = jsonDecode(ctx, LoginRequest.class);
    int team = request.team();
    String username = request.username();

    byte[] salt = userDB().getSalt(team, username);
    if (salt == null) {
      throw new NotFoundResponse();
    }

    byte[] nonce = new byte[16];
    SECURE_RANDOM.nextBytes(nonce);
    System.arraycopy(request.clientNonce(), 0, nonce, 0, 8);

    String nonceId = username + "@" + team + ":" + base64Encode(nonce);
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
                                          content = @OpenApiContent(from = ApiError.class)),
                         @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = ApiError.class)),
                         @OpenApiResponse(status = "404",
                                          content = @OpenApiContent(from = ApiError.class)) })
  public static void auth(Context ctx)
      throws NoSuchAlgorithmException, InvalidKeyException, SQLException {
    AuthRequest request = jsonDecode(ctx, AuthRequest.class);
    String username = request.username();
    int teamNum = request.team();

    User user = userDB().getUser(teamNum, username);
    if (user == null) {
      throw new NotFoundResponse();
    }

    Team team = teamDB().getTeam(teamNum);
    if (team == null) {
      throw new NotFoundResponse();
    }

    String nonceId = username + "@" + teamNum + ":" + base64Encode(request.nonce());
    if (!NONCES.contains(nonceId)) {
      throw new NotFoundResponse("Invalid nonce");
    }

    MessageDigest hashFunction = MessageDigest.getInstance(HASH_ALGORITHM);
    Mac hmacFunction = Mac.getInstance(MAC_ALGORITHM);
    hmacFunction.init(new SecretKeySpec(user.storedKey(), MAC_ALGORITHM));

    byte[] userAndNonce = combine(teamNum + username, request.nonce());
    byte[] clientSignature = hmacFunction.doFinal(userAndNonce);
    byte[] clientKey = xor(request.clientProof(), clientSignature);
    byte[] storedKey = hashFunction.digest(clientKey);
    if (!MessageDigest.isEqual(user.storedKey(), storedKey)) {
      throw new UnauthorizedResponse("Incorrect credentials for user " + username + "@" + teamNum);
    }

    hmacFunction.init(new SecretKeySpec(user.serverKey(), MAC_ALGORITHM));
    byte[] serverSignature = hmacFunction.doFinal(userAndNonce);
    NONCES.remove(nonceId);

    String sessionKey = UUID.randomUUID()
                            .toString();
    Session session = new Session(sessionKey, user.id(), user.team());
    sessions().put(sessionKey, session);
    ctx.json(new AuthResponse(user, team, session, serverSignature));
  }

  @OpenApi(path = "/session/{session_id}", methods = HttpMethod.GET, tags = "Authentication",
           description = "Get the current status of your session.",
           pathParams = @OpenApiParam(name = "session_id", type = String.class, required = true),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = Session.class)),
                         @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = ApiError.class)) })
  public static void getSession(Context ctx) {
    String sessionId = ctx.pathParam("session_id");

    Session session = sessions().get(sessionId);
    if (session == null) {
      throw new NotFoundResponse("Session not found");
    }

    ctx.json(session);
  }

  @OpenApi(path = "/logout", methods = HttpMethod.DELETE, tags = "Authentication",
           description = "Invalidates a session, logging a client out.",
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "204"),
                         @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = ApiError.class)) })
  public static void logout(Context ctx) {
    Session session = getValidSession(ctx);
    sessions().remove(session.getKey());
    throw new NoContentResponse();
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

  public static record AuthRequest(@OpenApiExample("xander") @OpenApiRequired
  @JsonProperty(required = true) String username,
                                   @OpenApiExample("1559") @OpenApiRequired
                                   @JsonProperty(required = true) int team,
                                   @OpenApiExample("EjRWeJCrze8SNFZ4kKvN7w==") @OpenApiRequired
                                   @JsonProperty(required = true) byte[] nonce,
                                   @OpenApiExample("EjRWeJCrze8SNFZ4kKvN7xI0VniQq83vEjRWeJCrze8=")
                                   @OpenApiRequired
                                   @JsonProperty(required = true) byte[] clientProof) {}

  public static record AuthResponse(@OpenApiRequired User user,
                                    @OpenApiRequired Team team,
                                    @OpenApiRequired Session session,
                                    @OpenApiExample("m7squ/lkrdjWSAER1g84uxQm3yDAOYUtVfYEJeYR2Tw=")
                                    @OpenApiRequired byte[] serverSignature) {}
}
