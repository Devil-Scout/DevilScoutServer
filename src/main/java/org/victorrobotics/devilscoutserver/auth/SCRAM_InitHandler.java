package org.victorrobotics.devilscoutserver.auth;

import org.victorrobotics.devilscoutserver.RequestHandler;
import org.victorrobotics.devilscoutserver.database.CredentialDB;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;

public class SCRAM_InitHandler extends RequestHandler {
  private static final String HASH_ALGORITHM = "SHA-256";

  private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9\\s]{1,32}");

  private final CredentialDB database;
  private final Random random;

  public SCRAM_InitHandler(CredentialDB database) {
    this.database = database;
    this.random = new SecureRandom();
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    if (!"POST".equals(exchange.getRequestMethod())) {
      closeExchange(exchange, 405);
      return;
    }

    InputStream requestStream = exchange.getRequestBody();
    String requestBody = new String(requestStream.readNBytes(80));
    if (requestStream.read() != -1) {
      closeExchange(exchange, 413);
      return;
    }
    requestStream.close();

    InitRequest request = parse(requestBody);
    if (request == null) {
      closeExchange(exchange, 400);
      return;
    }

    byte[] salt = database.getSalt(request.team, request.name);
    if (salt == null) {
      closeExchange(exchange, 404);
      return;
    }

    MessageDigest hashFunction;
    try {
      hashFunction = MessageDigest.getInstance(HASH_ALGORITHM);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }

    String user = request.team + request.name;
    byte[] userHash = hashFunction.digest(user.getBytes());
    byte[] nonce = new byte[16];
    random.nextBytes(nonce);
    System.arraycopy(request.clientNonce, 0, nonce, 0, 8);
    database.putNonce(userHash, nonce);

    String saltHex = HEX_FORMAT.formatHex(salt);
    String nonceHex = HEX_FORMAT.formatHex(nonce);

    String responseRaw = "s=" + saltHex + ",r=" + nonceHex;
    String response = new String(BASE64_ENCODER.encode(responseRaw.getBytes()));
    closeExchange(exchange, 200, response);
  }

  private static InitRequest parse(String requestBody) {
    // request format: "t={team},n={name},r={client_nonce}"
    try {
      String request = new String(BASE64_DECODER.decode(requestBody));
      if (!request.startsWith("t=")) return null;
      int commaIndex = request.indexOf(',');
      if (commaIndex < 3 || commaIndex > 6) return null;

      int team = Integer.parseInt(request.substring(2, commaIndex));
      if (team < 0 || team > 9999) return null;

      request = request.substring(commaIndex + 1);
      if (!request.startsWith("n=")) return null;
      commaIndex = request.indexOf(',');
      if (commaIndex < 3 || commaIndex > 34) return null;

      String name = request.substring(2, commaIndex);
      if (!VALID_NAME.matcher(name)
                     .matches()) {
        return null;
      }

      request = request.substring(commaIndex + 1);
      if (!request.startsWith("r=") || request.length() != 18) return null;

      byte[] clientNonce = HEX_FORMAT.parseHex(request.substring(2));

      return new InitRequest(team, name, clientNonce);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private static record InitRequest(int team,
                                    String name,
                                    byte[] clientNonce) {}
}
