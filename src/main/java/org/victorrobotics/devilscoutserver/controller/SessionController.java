package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.database.Session;
import org.victorrobotics.devilscoutserver.database.User;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Arrays;

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

public final class SessionController extends Controller {
  private static final String HASH_ALGORITHM = "SHA-256";
  private static final String HMAC_ALGORITHM = "HmacSHA256";

  private static final SecureRandom RANDOM = new SecureRandom();

  private SessionController() {}

  @OpenApi(path = "/sessions/login", methods = HttpMethod.POST, tags = "Session",
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
      throw new NotFoundResponse();
    }

    byte[] nonce = generateNonce(request.clientNonce());
    String nonceID = request.team() + "," + request.username() + "," + base64Encode(nonce);
    userDB().putNonce(nonceID);

    ctx.json(new LoginChallenge(salt, nonce));
  }

  @OpenApi(path = "/sessions/auth", methods = HttpMethod.POST, tags = "Session",
           description = "Authenticates a client. Must have already called `/login` to compute clientProof.",
           requestBody = @OpenApiRequestBody(required = true,
                                             content = @OpenApiContent(from = AuthRequest.class)),
           responses = { @OpenApiResponse(status = "201",
                                          content = @OpenApiContent(from = AuthResponse.class)),
                         @OpenApiResponse(status = "400"), @OpenApiResponse(status = "401"),
                         @OpenApiResponse(status = "404") })
  public static void auth(Context ctx) throws NoSuchAlgorithmException, InvalidKeyException {
    AuthRequest request = jsonDecode(ctx, AuthRequest.class);
    User user = userDB().getUser(request.team(), request.username());
    if (user == null) {
      throw new NotFoundResponse();
    }

    String nonceID =
        request.team() + "," + request.username() + "," + base64Encode(request.nonce());
    if (!userDB().containsNonce(nonceID)) {
      throw new UnauthorizedResponse();
    }

    MessageDigest hashFunction = MessageDigest.getInstance(HASH_ALGORITHM);
    Mac hmacFunction = Mac.getInstance(HMAC_ALGORITHM);
    hmacFunction.init(new SecretKeySpec(user.storedKey(), HMAC_ALGORITHM));

    byte[] userAndNonce = combine(request.team() + request.username(), request.nonce());
    byte[] clientSignature = hmacFunction.doFinal(userAndNonce);
    byte[] clientKey = xor(request.clientProof(), clientSignature);
    byte[] storedKey = hashFunction.digest(clientKey);
    if (!MessageDigest.isEqual(user.storedKey(), storedKey)) {
      throw new UnauthorizedResponse();
    }

    hmacFunction.init(new SecretKeySpec(user.serverKey(), HMAC_ALGORITHM));
    byte[] serverSignature = hmacFunction.doFinal(userAndNonce);
    userDB().removeNonce(nonceID);

    Session session = generateSession(user);
    ctx.status(201);
    ctx.json(new AuthResponse(user, session, serverSignature));
  }

  @OpenApi(path = "/sessions", methods = HttpMethod.DELETE, tags = "Session",
           description = "Invalidates a session, logging a client out.",
           responses = { @OpenApiResponse(status = "200"), @OpenApiResponse(status = "401") })
  public static void logout(Context ctx) {
    Session session = getValidSession(ctx);
    sessionDB().deleteSession(session);
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
    Session session = new Session(base64Encode(sessionID), user.userID(), user.accessLevel());
    sessionDB().registerSession(session);
    return session;
  }

  static record LoginRequest(@OpenApiRequired @OpenApiExample("1559") int team,
                             @OpenApiRequired @OpenApiExample("xander") String username,
                             @OpenApiRequired @OpenApiExample("EjRWeJCrze8=") byte[] clientNonce) {
    @Override
    public boolean equals(Object obj) {
      return this == obj || (obj instanceof LoginRequest other && team() == other.team()
          && Objects.equals(username(), other.username())
          && Arrays.equals(clientNonce(), other.clientNonce()));
    }

    @Override
    public int hashCode() {
      return Objects.hash(team(), username(), Arrays.hashCode(clientNonce()));
    }

    @Override
    public String toString() {
      StringBuilder b = new StringBuilder();
      return b.append("LoginRequest[")
              .append("team=")
              .append(team())
              .append(",username=")
              .append(username())
              .append(",clientNonce=")
              .append(base64Encode(clientNonce()))
              .append("]")
              .toString();
    }
  }

  static record LoginChallenge(@OpenApiRequired @OpenApiExample("mHZUMhCrze8=") byte[] salt,
                               @OpenApiRequired
                               @OpenApiExample("EjRWeJCrze8SNFZ4kKvN7w==") byte[] nonce) {
    @Override
    public boolean equals(Object obj) {
      return this == obj || (obj instanceof LoginChallenge other
          && Arrays.equals(salt(), other.salt()) && Arrays.equals(nonce(), other.nonce()));
    }

    @Override
    public int hashCode() {
      return Objects.hash(Arrays.hashCode(salt()), Arrays.hashCode(nonce()));
    }

    @Override
    public String toString() {
      StringBuilder b = new StringBuilder();
      return b.append("LoginChallenge[")
              .append("salt=")
              .append(base64Encode(salt()))
              .append(",nonce=")
              .append(base64Encode(nonce()))
              .append("]")
              .toString();
    }
  }

  static record AuthRequest(@OpenApiRequired @OpenApiExample("1559") int team,
                            @OpenApiRequired @OpenApiExample("xander") String username,
                            @OpenApiRequired
                            @OpenApiExample("EjRWeJCrze8SNFZ4kKvN7w==") byte[] nonce,
                            @OpenApiRequired
                            @OpenApiExample("EjRWeJCrze8SNFZ4kKvN7xI0VniQq83vEjRWeJCrze8=") byte[] clientProof) {
    @Override
    public boolean equals(Object obj) {
      return this == obj || (obj instanceof AuthRequest other && team() == other.team()
          && Objects.equals(username(), other.username()) && Arrays.equals(nonce(), other.nonce())
          && Arrays.equals(clientProof(), other.clientProof()));
    }

    @Override
    public int hashCode() {
      return Objects.hash(team(), username(), Arrays.hashCode(nonce()),
                          Arrays.hashCode(clientProof()));
    }

    @Override
    public String toString() {
      StringBuilder b = new StringBuilder();
      return b.append("AuthRequest[")
              .append("team=")
              .append(team())
              .append(",username=")
              .append(username())
              .append(",nonce=")
              .append(base64Encode(nonce()))
              .append(",clientProof=")
              .append(base64Encode(clientProof()))
              .append("]")
              .toString();
    }
  }

  static record AuthResponse(@OpenApiRequired @OpenApiExample("Xander Bhalla") String fullName,
                             @OpenApiRequired @OpenApiExample("USER") User.AccessLevel accessLevel,
                             @OpenApiRequired @OpenApiExample("K9UoTnrEY94=") String sessionID,
                             @OpenApiRequired
                             @OpenApiExample("m7squ/lkrdjWSAER1g84uxQm3yDAOYUtVfYEJeYR2Tw=") byte[] serverSignature) {
    AuthResponse(User user, Session session, byte[] serverSignature) {
      this(user.fullName(), user.accessLevel(), session.getSessionID(), serverSignature);
    }

    @Override
    public boolean equals(Object obj) {
      return this == obj || (obj instanceof AuthResponse other
          && Objects.equals(fullName(), other.fullName()) && accessLevel() == other.accessLevel()
          && Objects.equals(sessionID(), other.sessionID())
          && Arrays.equals(serverSignature(), other.serverSignature()));
    }

    @Override
    public int hashCode() {
      return Objects.hash(fullName(), accessLevel(), sessionID(),
                          Arrays.hashCode(serverSignature()));
    }

    @Override
    public String toString() {
      StringBuilder b = new StringBuilder();
      return b.append("AuthResponse[")
              .append("fullName=")
              .append(fullName())
              .append(",accessLevel=")
              .append(accessLevel())
              .append(",sessionID=")
              .append(sessionID())
              .append(",serverSignature=")
              .append(base64Encode(serverSignature()))
              .append("]")
              .toString();
    }
  }
}
