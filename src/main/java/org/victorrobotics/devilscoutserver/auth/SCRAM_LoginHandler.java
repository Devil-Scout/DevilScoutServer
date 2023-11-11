package org.victorrobotics.devilscoutserver.auth;

import org.victorrobotics.devilscoutserver.RequestHandler;
import org.victorrobotics.devilscoutserver.database.UserDB;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.javalin.http.Context;

public class SCRAM_LoginHandler extends RequestHandler {
  private static final String HASH_ALGORITHM = "SHA-256";

  private final UserDB users;
  private final Random random;

  public SCRAM_LoginHandler(UserDB users) {
    this.users = users;
    this.random = new SecureRandom();
  }

  @Override
  public void handle(Context ctx) throws Exception {
    LoginRequest request = ctx.bodyAsClass(LoginRequest.class);
    if (request == null) {
      ctx.status(400);
      return;
    }

    byte[] salt = users.getSalt(request.team, request.username);
    if (salt == null) {
      ctx.status(404);
      return;
    }

    MessageDigest hashFunction;
    try {
      hashFunction = MessageDigest.getInstance(HASH_ALGORITHM);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }

    String user = request.team + request.username;
    byte[] userHash = hashFunction.digest(user.getBytes());
    byte[] nonce = new byte[16];
    random.nextBytes(nonce);
    System.arraycopy(request.clientNonce, 0, nonce, 0, 8);
    byte[] nonceHash = hashFunction.digest(nonce);
    users.putNonce(userHash, nonceHash);

    ctx.json(Map.of("salt", base64Encode(salt), "nonce", base64Encode(nonce)));
  }

  private static record LoginRequest(int team,
                                     String username,
                                     byte[] clientNonce) {
    @JsonCreator
    LoginRequest(@JsonProperty("team") int team, @JsonProperty("username") String username,
                 @JsonProperty("clientNonce") String clientNonce) {
      this(team, username, base64Decode(clientNonce));
    }
  }
}
