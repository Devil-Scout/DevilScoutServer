package org.victorrobotics.devilscoutserver.auth;

import org.victorrobotics.devilscoutserver.RequestHandler;
import org.victorrobotics.devilscoutserver.database.CredentialDB;
import org.victorrobotics.devilscoutserver.database.Credentials;
import org.victorrobotics.devilscoutserver.database.Session;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.sun.net.httpserver.HttpExchange;

public class SCRAM_AuthHandler extends RequestHandler {
  private static final String HASH_ALGORITHM = "SHA-256";
  private static final String HMAC_ALGORITHM = "HmacSHA256";

  private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9\\s]{1,32}");

  private final CredentialDB database;
  private final SecureRandom random;

  public SCRAM_AuthHandler(CredentialDB database) {
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
    String requestBody = new String(requestStream.readNBytes(192));
    if (requestStream.read() != -1) {
      closeExchange(exchange, 413);
      return;
    }
    requestStream.close();

    AuthRequest request = parse(requestBody);
    if (request == null) {
      closeExchange(exchange, 400);
      return;
    }

    Credentials credentials = database.get(request.team, request.name);
    if (credentials == null) {
      closeExchange(exchange, 404);
      return;
    }

    MessageDigest hashFunction;
    Mac hmacFunction;
    try {
      hashFunction = MessageDigest.getInstance(HASH_ALGORITHM);
      hmacFunction = Mac.getInstance(HMAC_ALGORITHM);
      hmacFunction.init(new SecretKeySpec(credentials.storedKey(), HMAC_ALGORITHM));
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new IllegalStateException(e);
    }

    String user = request.team + request.name;
    byte[] userHash = hashFunction.digest(user.getBytes());
    byte[] storedNonce = database.getNonce(userHash);
    if (!Arrays.equals(request.nonce, storedNonce)) {
      closeExchange(exchange, 400);
      return;
    }

    byte[] userAndNonce = toStr(request.team + request.name, storedNonce);
    byte[] clientSignature = hmacFunction.doFinal(userAndNonce);
    byte[] clientKey = xor(request.clientProof, clientSignature);
    byte[] storedKey = hashFunction.digest(clientKey);
    if (!Arrays.equals(storedKey, credentials.storedKey())) {
      closeExchange(exchange, 401);
      return;
    }

    try {
      hmacFunction.init(new SecretKeySpec(credentials.serverKey(), HMAC_ALGORITHM));
    } catch (InvalidKeyException e) {
      throw new IllegalStateException(e);
    }
    byte[] serverSignature = hmacFunction.doFinal(userAndNonce);

    Session session = generateSession(credentials);
    String response = "v=" + HEX_FORMAT.formatHex(serverSignature) + ",i="
        + HEX_FORMAT.toHexDigits(session.sessionID) + ",p=" + credentials.permissions();
    closeExchange(exchange, 200, BASE64_ENCODER.encodeToString(response.getBytes()));
  }

  private Session generateSession(Credentials credentials) {
    long sessionID = random.nextLong();
    return new Session(sessionID, credentials.userID(), credentials.permissions());
  }

  private static AuthRequest parse(String requestBody) {
    // request format: "t={team},n={name},r={nonce},p={clientProof}"
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
      commaIndex = request.indexOf(',');
      if (!request.startsWith("r=") || commaIndex != 34) return null;

      byte[] nonce = HEX_FORMAT.parseHex(request.substring(2, 34));

      request = request.substring(35);
      if (!request.startsWith("p=") || request.length() != 66) return null;

      byte[] clientProof = HEX_FORMAT.parseHex(request.substring(2));

      return new AuthRequest(team, name, nonce, clientProof);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private static byte[] xor(byte[] bytes1, byte[] bytes2) {
    assert bytes1.length == bytes2.length;
    byte[] bytes = new byte[bytes1.length];
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = (byte) (bytes1[i] ^ bytes2[i]);
    }
    return bytes;
  }

  private static byte[] toStr(String username, byte[] nonce) {
    byte[] bytes = new byte[username.length() + nonce.length];
    byte[] userBytes = username.getBytes();
    System.arraycopy(userBytes, 0, bytes, 0, userBytes.length);
    System.arraycopy(nonce, 0, bytes, userBytes.length, nonce.length);
    return bytes;
  }

  private static record AuthRequest(int team,
                                    String name,
                                    byte[] nonce,
                                    byte[] clientProof) {}
}
