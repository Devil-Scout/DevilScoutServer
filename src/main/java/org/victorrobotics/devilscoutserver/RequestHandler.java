package org.victorrobotics.devilscoutserver;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HexFormat;

import io.javalin.http.Handler;

public abstract class RequestHandler implements Handler {
  private static final HexFormat      HEX_FORMAT     = HexFormat.of();
  private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();
  private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();

  protected RequestHandler() {}

  protected static final String base64Decode(String str) {
    return base64Decode(str.getBytes(StandardCharsets.UTF_8));
  }

  protected static final String base64Decode(byte[] bytes) {
    return new String(BASE64_DECODER.decode(bytes));
  }

  protected static final byte[] base64Encode(String str) {
    return base64Encode(str.getBytes(StandardCharsets.UTF_8));
  }

  protected static final byte[] base64Encode(byte[] bytes) {
    return BASE64_ENCODER.encode(bytes);
  }

  protected static final byte[] parseHex(String hex) {
    return HEX_FORMAT.parseHex(hex);
  }

  protected static final String formatHex(byte[] bytes) {
    return HEX_FORMAT.formatHex(bytes);
  }

  protected static final String formatHex(long l) {
    return HEX_FORMAT.toHexDigits(l);
  }
}
