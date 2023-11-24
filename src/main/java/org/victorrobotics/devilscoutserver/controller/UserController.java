package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.data.Session;
import org.victorrobotics.devilscoutserver.data.UserAccessLevel;
import org.victorrobotics.devilscoutserver.data.UserInfo;
import org.victorrobotics.devilscoutserver.database.User;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Collection;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.javalin.http.ConflictResponse;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.NoContentResponse;
import io.javalin.http.NotFoundResponse;
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
                                          content = @OpenApiContent(from = UserInfo[].class)),
                         @OpenApiResponse(status = "401"), @OpenApiResponse(status = "403") })
  public static void allUsers(Context ctx) {
    getValidSession(ctx, UserAccessLevel.SUDO);
    ctx.writeJsonStream(userDB().allUsers()
                                .stream()
                                .map(User::info));
  }

  @OpenApi(path = "/teams/{team}/users", methods = HttpMethod.GET, tags = "Users",
           summary = "ADMIN, SUDO",
           description = "Get all registered users on the specified team. "
               + "Requires ADMIN if from the same team, or SUDO if from a different team.",
           pathParams = @OpenApiParam(name = "team", type = Integer.class, required = true,
                                      example = "1559"),
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = UserInfo[].class)),
                         @OpenApiResponse(status = "400"), @OpenApiResponse(status = "401"),
                         @OpenApiResponse(status = "403"), @OpenApiResponse(status = "404") })
  public static void usersOnTeam(Context ctx) {
    Session session = getValidSession(ctx, UserAccessLevel.ADMIN);
    int team = ctx.pathParamAsClass("team", Integer.class)
                  .get();

    if (team != session.getTeam() && !session.hasAccess(UserAccessLevel.SUDO)) {
      throw new ForbiddenResponse();
    }

    Collection<User> users = userDB().usersByTeam(team);
    if (users == null) {
      throw new NotFoundResponse();
    }

    ctx.writeJsonStream(users.stream()
                             .map(User::info));
  }

  @OpenApi(path = "/users", methods = HttpMethod.PUT, tags = "Users", summary = "ADMIN, SUDO",
           description = "Register a new user. The new user's access level may not exceed client's. "
               + "Requires ADMIN if from the same team, or SUDO if from a different team.",
           security = @OpenApiSecurity(name = "Session"),
           requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = UserRegistration.class)),
           responses = { @OpenApiResponse(status = "201",
                                          content = @OpenApiContent(from = UserInfo.class)),
                         @OpenApiResponse(status = "400"), @OpenApiResponse(status = "401"),
                         @OpenApiResponse(status = "403"), @OpenApiResponse(status = "409") })
  public static void registerUser(Context ctx) {
    Session session = getValidSession(ctx, UserAccessLevel.ADMIN);
    UserRegistration registration = jsonDecode(ctx, UserRegistration.class);

    if (session.getTeam() != registration.team() && !session.hasAccess(UserAccessLevel.SUDO)) {
      throw new ForbiddenResponse();
    }

    if (userDB().getUser(registration.team(), registration.username()) != null) {
      throw new ConflictResponse();
    }

    if (!session.hasAccess(registration.accessLevel())) {
      throw new ForbiddenResponse();
    }

    byte[] salt = new byte[8];
    SECURE_RANDOM.nextBytes(salt);

    byte[][] keys = computeKeys(registration.password(), salt);
    byte[] storedKey = keys[0];
    byte[] serverKey = keys[1];

    long id;
    do {
      id = SECURE_RANDOM.nextLong(1L << 53);
    } while (userDB().getUser(id) != null);

    User user = new User(id, registration.team(), registration.username(), registration.fullName(),
                         registration.accessLevel(), salt, storedKey, serverKey);
    userDB().addUser(user);

    ctx.json(user.info());
    ctx.status(201);
  }

  @OpenApi(path = "/users/{id}", methods = HttpMethod.GET, tags = "Users",
           pathParams = @OpenApiParam(name = "id", type = Long.class, required = true),
           summary = "USER, ADMIN, SUDO",
           description = "Get user information. USERs may access themselves. "
               + "Accessing other users requires ADMIN if from the same team, "
               + "or SUDO if from a different team.",
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = UserInfo.class)),
                         @OpenApiResponse(status = "400"), @OpenApiResponse(status = "401"),
                         @OpenApiResponse(status = "403"), @OpenApiResponse(status = "404") })
  @SuppressWarnings("java:S1941") // move session closer to code that uses it
  public static void getUser(Context ctx) {
    Session session = getValidSession(ctx);

    long userId = ctx.pathParamAsClass("id", Long.class)
                     .get();
    if (userId != session.getUserId() && !session.hasAccess(UserAccessLevel.ADMIN)) {
      throw new ForbiddenResponse();
    }

    User user = userDB().getUser(userId);
    if (user == null) {
      throw new NotFoundResponse();
    }

    if (user.team() != session.getTeam() && !session.hasAccess(UserAccessLevel.SUDO)) {
      throw new ForbiddenResponse();
    }

    ctx.json(user.info());
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
                                          content = @OpenApiContent(from = UserInfo.class)),
                         @OpenApiResponse(status = "400"), @OpenApiResponse(status = "401"),
                         @OpenApiResponse(status = "403"), @OpenApiResponse(status = "409") })
  @SuppressWarnings("java:S1941") // move session closer to code that uses it
  public static void editUser(Context ctx) {
    Session session = getValidSession(ctx, UserAccessLevel.ADMIN);

    long userId = ctx.pathParamAsClass("id", Long.class)
                     .get();
    User oldUser = userDB().getUser(userId);
    if (oldUser == null) {
      throw new NotFoundResponse();
    }

    if (session.getTeam() != oldUser.team() && !session.hasAccess(UserAccessLevel.SUDO)) {
      throw new ForbiddenResponse();
    }

    UserEdits edits = jsonDecode(ctx, UserEdits.class);

    if (edits.team() != null && edits.team() != session.getTeam()
        && !session.hasAccess(UserAccessLevel.SUDO)) {
      throw new ForbiddenResponse();
    }

    if (!session.hasAccess(edits.accessLevel())) {
      throw new ForbiddenResponse();
    }

    byte[] storedKey;
    byte[] serverKey;
    if (edits.password() == null) {
      storedKey = oldUser.storedKey();
      serverKey = oldUser.serverKey();
    } else {
      byte[][] keys = computeKeys(edits.password(), oldUser.salt());
      storedKey = keys[0];
      serverKey = keys[1];
    }

    int team = edits.team() == null ? oldUser.team() : edits.team();
    String username = edits.username() == null ? oldUser.username() : edits.username();
    String fullName = edits.fullName() == null ? oldUser.fullName() : edits.fullName();
    UserAccessLevel accessLevel =
        edits.accessLevel() == null ? oldUser.accessLevel() : edits.accessLevel();

    User newUser = new User(oldUser.id(), team, username, fullName, accessLevel, oldUser.salt(),
                            storedKey, serverKey);
    userDB().editUser(oldUser, newUser);

    User editedUser = newUser;
    ctx.json(editedUser.info());
  }

  @OpenApi(path = "/users/{id}", methods = HttpMethod.DELETE, tags = "Users",
           summary = "ADMIN, SUDO",
           pathParams = @OpenApiParam(name = "id", type = Long.class, required = true),
           description = "Delete a user. USERs may not delete themselves. "
               + "Requires ADMIN if from the same team, or SUDO if from a different team.",
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "204"), @OpenApiResponse(status = "400"),
                         @OpenApiResponse(status = "401"), @OpenApiResponse(status = "403"),
                         @OpenApiResponse(status = "404") })
  @SuppressWarnings("java:S1941") // move session closer to code that uses it
  public static void deleteUser(Context ctx) {
    Session session = getValidSession(ctx, UserAccessLevel.ADMIN);

    long userId = ctx.pathParamAsClass("id", Long.class)
                     .get();
    User user = userDB().getUser(userId);
    if (user == null) {
      throw new NotFoundResponse();
    }

    if (session.getTeam() != user.team() && !session.hasAccess(UserAccessLevel.SUDO)) {
      throw new ForbiddenResponse();
    }

    if (!session.hasAccess(user.accessLevel())) {
      throw new ForbiddenResponse();
    }

    userDB().removeUser(user);
    throw new NoContentResponse();
  }

  private static byte[][] computeKeys(String password, byte[] salt) {
    try {
      SecretKeyFactory factory = SecretKeyFactory.getInstance(KEYGEN_ALGORITHM);
      KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 4096, 256);
      SecretKey saltedPassword = factory.generateSecret(keySpec);

      MessageDigest sha256 = MessageDigest.getInstance(HASH_ALGORITHM);
      Mac hmacSha256 = Mac.getInstance(MAC_ALGORITHM);
      hmacSha256.init(saltedPassword);

      byte[] clientKey = hmacSha256.doFinal("Client Key".getBytes());
      byte[] storedKey = sha256.digest(clientKey);
      byte[] serverKey = hmacSha256.doFinal("Server Key".getBytes());
      return new byte[][] { storedKey, serverKey };
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
                                 @JsonProperty(required = true) UserAccessLevel accessLevel,
                                 @OpenApiRequired @OpenApiExample("verybadpassword")
                                 @JsonProperty(required = true) String password) {}

  static record UserEdits(Integer team,
                          @OpenApiExample("xbhalla") String username,
                          @OpenApiExample("Alexander Bhalla") String fullName,
                          UserAccessLevel accessLevel,
                          @OpenApiExample("verybadpassword") String password) {}
}
