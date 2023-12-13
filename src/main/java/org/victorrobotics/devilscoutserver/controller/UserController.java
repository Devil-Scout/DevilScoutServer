package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.database.User;
import org.victorrobotics.devilscoutserver.database.User.AccessLevel;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.SQLException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.javalin.http.ConflictResponse;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiRequired;
import io.javalin.openapi.OpenApiResponse;
import io.javalin.openapi.OpenApiSecurity;

public final class UserController extends Controller {
  private UserController() {}

  @OpenApi(path = "/users", methods = HttpMethod.GET, tags = "Users", summary = "SUDO",
           description = "Get all registered users. Requires SUDO.",
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = User[].class)),
                         @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "403",
                                          content = @OpenApiContent(from = Error.class)) })
  public static void allUsers(Context ctx) throws SQLException {
    Session session = getValidSession(ctx);
    userDB().getAccessLevel(session.getUser())
            .verify(AccessLevel.SUDO);
    ctx.writeJsonStream(userDB().allUsers()
                                .stream());
  }

  @OpenApi(path = "/users", methods = HttpMethod.POST, tags = "Users", summary = "ADMIN, SUDO",
           description = "Register a new user. The new user's access level may not exceed client's. "
               + "Requires ADMIN if from the same team, or SUDO if from a different team.",
           security = @OpenApiSecurity(name = "Session"),
           requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = UserRegistration.class)),
           responses = { @OpenApiResponse(status = "201",
                                          content = @OpenApiContent(from = User.class)),
                         @OpenApiResponse(status = "400",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "403",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "404",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "409",
                                          content = @OpenApiContent(from = Error.class)) })
  public static void registerUser(Context ctx) throws SQLException {
    Session session = getValidSession(ctx);
    AccessLevel accessLevel = userDB().getAccessLevel(session.getUser());
    accessLevel.verify(AccessLevel.ADMIN);

    UserRegistration registration = jsonDecode(ctx, UserRegistration.class);
    int team = registration.team();
    checkTeamRange(team);

    if (session.getTeam() != team) {
      accessLevel.verify(AccessLevel.SUDO);
    }

    if (!teamDB().containsTeam(team)) {
      throwTeamNotFound(team);
      return;
    }

    String username = registration.username();
    if (userDB().getUser(team, username) != null) {
      throw new ConflictResponse("User " + username + "@" + team + " already exists");
    }

    accessLevel.verify(registration.accessLevel());

    byte[][] auth = computeAuthentication(registration.password());
    byte[] salt = auth[0];
    byte[] storedKey = auth[1];
    byte[] serverKey = auth[2];

    User user = userDB().registerUser(team, username, registration.fullName(),
                                      registration.accessLevel(), salt, storedKey, serverKey);
    ctx.json(user);
    throwCreated();
  }

  @OpenApi(path = "/users/{id}", methods = HttpMethod.GET, tags = "Users",
           pathParams = @OpenApiParam(name = "id", type = Long.class, required = true),
           summary = "USER, ADMIN, SUDO",
           description = "Get user information. USERs may access themselves. "
               + "Accessing other users requires ADMIN if from the same team, "
               + "or SUDO if from a different team.",
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = User.class)),
                         @OpenApiResponse(status = "400",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "403",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "404",
                                          content = @OpenApiContent(from = Error.class)) })
  @SuppressWarnings("java:S1941") // move session closer to code that uses it
  public static void getUser(Context ctx) throws SQLException {
    Session session = getValidSession(ctx);
    AccessLevel accessLevel = userDB().getAccessLevel(session.getUser());

    long userId = ctx.pathParamAsClass("id", Long.class)
                     .get();
    if (userId != session.getUser()) {
      accessLevel.verify(AccessLevel.ADMIN);
    }

    User user = userDB().getUser(userId);
    if (user == null) {
      throwUserNotFound(userId);
      return;
    }

    if (user.team() != session.getTeam()) {
      accessLevel.verify(AccessLevel.SUDO);
    }

    ctx.json(user);
  }

  @OpenApi(path = "/users/{id}", methods = HttpMethod.PATCH, tags = "Users",
           summary = "USER, ADMIN, SUDO",
           description = "Edit a user's information. The user's accessLevel may not be elevated beyond the client's. "
               + "USERs may edit themselves. Editing other users requires ADMIN if from the same team, "
               + "or SUDO if from a different team.",
           pathParams = @OpenApiParam(name = "id", type = Long.class, required = true),
           security = @OpenApiSecurity(name = "Session"),
           requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = UserEdits.class)),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = User.class)),
                         @OpenApiResponse(status = "400",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "403",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "409",
                                          content = @OpenApiContent(from = Error.class)) })
  @SuppressWarnings("java:S1941") // move session closer to code that uses it
  public static void editUser(Context ctx) throws SQLException {
    Session session = getValidSession(ctx);
    AccessLevel accessLevel = userDB().getAccessLevel(session.getUser());

    long userId = ctx.pathParamAsClass("id", Long.class)
                     .get();
    if (userId != session.getUser()) {
      accessLevel.verify(AccessLevel.ADMIN);
    }

    User user = userDB().getUser(userId);
    if (user == null) {
      throwUserNotFound(userId);
      return;
    }

    if (session.getTeam() != user.team()) {
      accessLevel.verify(AccessLevel.SUDO);
    } else {
      accessLevel.verify(user.accessLevel());
    }

    UserEdits edits = jsonDecode(ctx, UserEdits.class);
    if (edits.accessLevel() != null) {
      accessLevel.verify(edits.accessLevel());
    }

    byte[][] authInfo = null;
    if (edits.password() != null) {
      authInfo = computeAuthentication(edits.password());
    }

    user = userDB().editUser(userId, edits.username(), edits.fullName(), edits.accessLevel(),
                             authInfo);

    ctx.json(user);
  }

  @OpenApi(path = "/users/{id}", methods = HttpMethod.DELETE, tags = "Users",
           summary = "ADMIN, SUDO",
           pathParams = @OpenApiParam(name = "id", type = Long.class, required = true),
           description = "Delete a user. USERs may not delete themselves. "
               + "Requires ADMIN if from the same team, or SUDO if from a different team.",
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "204"),
                         @OpenApiResponse(status = "400",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "403",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "404",
                                          content = @OpenApiContent(from = Error.class)) })
  @SuppressWarnings("java:S1941") // move session closer to code that uses it
  public static void deleteUser(Context ctx) throws SQLException {
    Session session = getValidSession(ctx);
    AccessLevel accessLevel = userDB().getAccessLevel(session.getUser());
    accessLevel.verify(AccessLevel.ADMIN);

    long userId = ctx.pathParamAsClass("id", Long.class)
                     .get();
    User user = userDB().getUser(userId);
    if (user == null) {
      throwUserNotFound(userId);
      return;
    }

    if (session.getTeam() == user.team()) {
      accessLevel.verify(user.accessLevel());
    } else {
      accessLevel.verify(AccessLevel.SUDO);
    }

    userDB().deleteUser(user.id());
    throwNoContent();
  }

  private static byte[][] computeAuthentication(String password) {
    try {
      byte[] salt = new byte[8];
      SECURE_RANDOM.nextBytes(salt);

      SecretKeyFactory factory = SecretKeyFactory.getInstance(KEYGEN_ALGORITHM);
      KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 4096, 256);
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

  static record UserRegistration(@OpenApiRequired @OpenApiExample("1559")
  @JsonProperty(required = true) int team,
                                 @OpenApiRequired @OpenApiExample("xander")
                                 @JsonProperty(required = true) String username,
                                 @OpenApiRequired @OpenApiExample("Xander Bhalla")
                                 @JsonProperty(required = true) String fullName,
                                 @OpenApiRequired
                                 @JsonProperty(required = true) AccessLevel accessLevel,
                                 @OpenApiRequired @OpenApiExample("password")
                                 @JsonProperty(required = true) String password) {}

  static record UserEdits(@OpenApiExample("xander") String username,
                          @OpenApiExample("Xander Bhalla") String fullName,
                          AccessLevel accessLevel,
                          @OpenApiExample("password") String password) {}
}
