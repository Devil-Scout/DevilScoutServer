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
import io.javalin.http.ConflictResponse;
import io.javalin.http.Context;
import io.javalin.http.CreatedResponse;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.NoContentResponse;
import io.javalin.http.NotFoundResponse;

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
      throw new ForbiddenResponse();
    }

    if (!teamDB().containsTeam(teamNum)) {
      throw new NotFoundResponse();
    }

    UserRegistration registration = jsonDecode(ctx, UserRegistration.class);
    String username = registration.username();
    if (userDB().getUser(teamNum, username) != null) {
      throw new ConflictResponse("User " + username + "@" + teamNum + " already exists");
    }

    byte[][] auth = computeAuthentication(registration.password());
    byte[] salt = auth[0];
    byte[] storedKey = auth[1];
    byte[] serverKey = auth[2];

    User user = userDB().registerUser(teamNum, username, registration.fullName(),
                                      registration.admin(), salt, storedKey, serverKey);
    ctx.json(user);
    throw new CreatedResponse();
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
    checkTeamRange(team);

    if (team != session.getTeam()) {
      throw new ForbiddenResponse();
    }

    if (!teamDB().containsTeam(team)) {
      throw new NotFoundResponse();
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
      throw new NotFoundResponse();
    }

    if (user.team() != session.getTeam()) {
      throw new ForbiddenResponse();
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

    String userId = ctx.pathParam("id");
    if (!userId.equals(session.getUser())) {
      session.verifyAdmin();
    }

    User user = userDB().getUser(userId);
    if (user == null) {
      throw new NotFoundResponse();
    }

    if (session.getTeam() != user.team()) {
      throw new ForbiddenResponse();
    }

    UserEdits edits = jsonDecode(ctx, UserEdits.class);
    if (edits.admin() != null) {
      session.verifyAdmin();
    }

    byte[][] authInfo = null;
    if (edits.password() != null) {
      authInfo = computeAuthentication(edits.password());
    }

    user = userDB().editUser(userId, edits.username(), edits.fullName(), edits.admin(), authInfo);

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
      throw new NotFoundResponse();
    }

    if (session.getTeam() != user.team()) {
      throw new ForbiddenResponse();
    }

    userDB().deleteUser(user.id());
    throw new NoContentResponse();
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
