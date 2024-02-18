package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.database.User;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.SQLException;
import java.util.Collection;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public final class UserController extends Controller {
  private static final String TEAM_PATH_PARAM = "teamNum";
  private static final String USER_PATH_PARAM = "userId";

  private UserController() {}

  /**
   * POST /teams/{teamNum}/users
   * <p>
   * Request body: {@link TeamRegistration}
   * <p>
   * Success: 201 {@link User}
   * <p>
   * Errors:
   * <ul>
   * <li>400 BadRequest</li>
   * <li>401 Unauthorized</li>
   * <li>403 Forbidden</li>
   * <li>404 NotFound</li>
   * <li>409 Conflict</li>
   * </ul>
   */
  public static void registerUser(Context ctx) throws SQLException {
    Session session = getValidSession(ctx);
    session.verifyAdmin();

    int teamNum = ctx.pathParamAsClass(TEAM_PATH_PARAM, Integer.class)
                     .get();
    if (session.getTeam() != teamNum) {
      throw forbiddenTeam(session.getTeam());
    }

    if (!teamDB().containsTeam(teamNum)) {
      throw teamNotFound(teamNum);
    }

    UserRegistration registration = jsonDecode(ctx, UserRegistration.class);
    String username = registration.username();
    if (userDB().getUser(teamNum, username) != null) {
      throw userConflict(teamNum, username);
    }

    byte[][] auth = computeAuthentication(registration.password());
    byte[] salt = auth[0];
    byte[] storedKey = auth[1];
    byte[] serverKey = auth[2];

    User user = userDB().registerUser(teamNum, username, registration.fullName(),
                                      registration.admin(), salt, storedKey, serverKey);
    ctx.json(user);
    ctx.status(HttpStatus.CREATED);
  }

  /**
   * GET /teams/{teamNum}/users
   * <p>
   * Success: 200 {@link User}[]
   * <p>
   * Errors:
   * <ul>
   * <li>401 Unauthorized</li>
   * <li>403 Forbidden</li>
   * <li>404 NotFound</li>
   * </ul>
   */
  public static void usersOnTeam(Context ctx) throws SQLException {
    Session session = getValidSession(ctx);
    session.verifyAdmin();

    int team = ctx.pathParamAsClass(TEAM_PATH_PARAM, Integer.class)
                  .get();

    if (team != session.getTeam()) {
      throw forbiddenTeam(session.getTeam());
    }

    if (!teamDB().containsTeam(team)) {
      throw teamNotFound(team);
    }

    Collection<User> users = userDB().usersOnTeam(team);
    ctx.writeJsonStream(users.stream());
  }

  /**
   * GET /users/{userId}
   * <p>
   * Success: 200 {@link User}
   * <p>
   * Errors:
   * <ul>
   * <li>401 Unauthorized</li>
   * <li>403 Forbidden</li>
   * <li>404 NotFound</li>
   * </ul>
   */
  @SuppressWarnings("java:S1941") // move session closer to code that uses it
  public static void getUser(Context ctx) throws SQLException {
    Session session = getValidSession(ctx);
    session.verifyAdmin();

    String userId = ctx.pathParam(USER_PATH_PARAM);
    if (!userId.equals(session.getUser())) {
      session.verifyAdmin();
    }

    User user = userDB().getUser(userId);
    if (user == null) {
      throw userNotFound(userId);
    }

    if (user.team() != session.getTeam()) {
      throw forbiddenTeam(session.getTeam());
    }

    ctx.json(user);
  }

  /**
   * PATCH /users/{userId}
   * <p>
   * Request body: {@link UserEdits}
   * <p>
   * Success: 200 {@link User}
   * <p>
   * Errors:
   * <ul>
   * <li>400 BadRequest</li>
   * <li>401 Unauthorized</li>
   * <li>403 Forbidden</li>
   * <li>404 NotFound</li>
   * <li>409 Conflict</li>
   * </ul>
   */
  @SuppressWarnings("java:S1941") // move session closer to code that uses it
  public static void editUser(Context ctx) throws SQLException {
    Session session = getValidSession(ctx);

    String userId = ctx.pathParam(USER_PATH_PARAM);
    if (!userId.equals(session.getUser())) {
      session.verifyAdmin();
    }

    User user = userDB().getUser(userId);
    if (user == null) {
      throw userNotFound(userId);
    }

    if (session.getTeam() != user.team()) {
      throw forbiddenTeam(session.getTeam());
    }

    UserEdits edits = jsonDecode(ctx, UserEdits.class);
    if (edits.admin() == Boolean.TRUE) {
      session.verifyAdmin();
    }

    if (edits.username() != null && userDB().getUser(user.team(), edits.username()) != null) {
      throw userConflict(user.team(), edits.username());
    }

    byte[][] authInfo = null;
    if (edits.password() != null) {
      authInfo = computeAuthentication(edits.password());
    }

    user = userDB().editUser(userId, edits.username(), edits.fullName(), edits.admin(), authInfo);

    // If permissions are reduced OR password was changed, log out this user
    // EXCEPT the current session
    if (Boolean.FALSE.equals(edits.admin()) || authInfo != null) {
      sessions().values()
                .removeIf(s -> s.getUser()
                                .equals(userId)
                    && s != session);
    }

    ctx.json(user);
  }

  /**
   * DELETE /users/{userId}
   * <p>
   * Success: 204 NoContent
   * <p>
   * Errors:
   * <ul>
   * <li>401 Unauthorized</li>
   * <li>403 Forbidden</li>
   * <li>404 NotFound</li>
   * </ul>
   */
  @SuppressWarnings("java:S1941") // move session closer to code that uses it
  public static void deleteUser(Context ctx) throws SQLException {
    Session session = getValidSession(ctx);

    String userId = ctx.pathParam(USER_PATH_PARAM);
    if (!userId.equals(session.getUser())) {
      session.verifyAdmin();
    }

    User user = userDB().getUser(userId);
    if (user == null) {
      throw userNotFound(userId);
    }

    if (session.getTeam() != user.team()) {
      throw forbiddenTeam(session.getTeam());
    }

    userDB().deleteUser(user.id());
    sessions().values()
              .removeIf(s -> s.getUser()
                              .equals(user.id()));
    ctx.status(HttpStatus.NO_CONTENT);
  }

  private static byte[][] computeAuthentication(String password) {
    try {
      byte[] salt = new byte[8];
      SECURE_RANDOM.nextBytes(salt);

      SecretKeyFactory factory = SecretKeyFactory.getInstance(KEYGEN_ALGORITHM);
      KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
      SecretKey saltedPassword = factory.generateSecret(keySpec);

      MessageDigest sha256 = MessageDigest.getInstance(HASH_ALGORITHM);
      Mac hmacSha256 = Mac.getInstance(MAC_ALGORITHM);
      hmacSha256.init(saltedPassword);

      byte[] clientKey = hmacSha256.doFinal("Client Key".getBytes());
      byte[] storedKey = sha256.digest(clientKey);
      byte[] serverKey = hmacSha256.doFinal("Server Key".getBytes());
      return new byte[][] { salt, storedKey, serverKey };
    } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException e) {
      throw new IllegalStateException(e);
    }
  }

  static record UserRegistration(@JsonProperty(required = true) String username,
                                 @JsonProperty(required = true) String fullName,
                                 @JsonProperty(required = true) boolean admin,
                                 @JsonProperty(required = true) String password) {}

  static record UserEdits(String username,
                          String fullName,
                          Boolean admin,
                          String password) {}
}
