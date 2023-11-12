package org.victorrobotics.devilscoutserver.controller;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;

public class Controller {
  private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();
  private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();

  protected Controller() {}

  protected static final byte[] base64Decode(String str) {
    return BASE64_DECODER.decode(str.getBytes(StandardCharsets.UTF_8));
  }

  protected static final String base64Encode(byte[] bytes) {
    return BASE64_ENCODER.encodeToString(bytes);
  }

  @SuppressWarnings("java:S2221") // catch generic exception
  protected static final <T> T jsonDecode(Context ctx, Class<T> clazz) {
    try {
      return ctx.bodyAsClass(clazz);
    } catch (Exception e) {
      throw new BadRequestResponse();
    }
  }
}
