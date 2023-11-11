package org.victorrobotics.devilscoutserver;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import io.javalin.http.Handler;

public abstract class RequestHandler implements Handler {
  private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();
  private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();

  protected RequestHandler() {}

  protected static final byte[] base64Decode(String str) {
    return BASE64_DECODER.decode(str.getBytes(StandardCharsets.UTF_8));
  }

  protected static final String base64Encode(byte[] bytes) {
    return BASE64_ENCODER.encodeToString(bytes);
  }
}
