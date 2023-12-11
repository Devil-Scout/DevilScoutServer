package org.victorrobotics.devilscoutserver;

import java.util.Base64;

public class Base64Util {
  private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
  private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();

  public static String base64Encode(byte[] bytes) {
    return BASE64_ENCODER.encodeToString(bytes);
  }

  public static byte[] base64Decode(String base64) {
    return BASE64_DECODER.decode(base64);
  }
}
