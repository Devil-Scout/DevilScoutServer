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

@SuppressWarnings("java:S6218") // override equals for array content
public final class SessionController extends Controller {
  static final Set<String> NONCES = Collections.newSetFromMap(new ConcurrentHashMap<>());

  private SessionController() {}

  /**
   * POST /login
   * <p>
   * Request body: {@link LoginRequest}
   * <p>
   * Success: 200 {@link LoginChallenge}
   * <p>
   * Errors:
   * <ul>
   * <li>400 BadRequest</li>
   * <li>404 NotFound</li>
   * </ul>
   */
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

  /**
   * POST /auth
   * <p>
   * Request body: {@link AuthRequest}
   * <p>
   * Success: 200 {@link AuthResponse}
   * <p>
   * Errors:
   * <ul>
   * <li>400 BadRequest</li>
   * <li>401 Unauthorized</li>
   * <li>404 NotFound</li>
   * </ul>
   */
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

  /**
   * GET /session
   * <p>
   * Success: 200 {@link Session}
   * <p>
   * Errors:
   * <ul>
   * <li>401 Unauthorized</li>
   * </ul>
   */
  public static void getSession(Context ctx) {
    Session session = getValidSession(ctx);
    ctx.json(session);
  }

  /**
   * DELETE /logout
   * <p>
   * Success: 204 NoContent
   * <p>
   * Errors:
   * <ul>
   * <li>401 Unauthorized</li>
   * </ul>
   */
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

  public static record LoginRequest(@JsonProperty(required = true) int team,
                                    @JsonProperty(required = true) String username,
                                    @JsonProperty(required = true) byte[] clientNonce) {}

  public static record LoginChallenge(byte[] salt,
                                      byte[] nonce) {}

  public static record AuthRequest(@JsonProperty(required = true) String username,
                                   @JsonProperty(required = true) int team,
                                   @JsonProperty(required = true) byte[] nonce,
                                   @JsonProperty(required = true) byte[] clientProof) {}

  public static record AuthResponse(User user,
                                    Team team,
                                    Session session,
                                    byte[] serverSignature) {}
}
