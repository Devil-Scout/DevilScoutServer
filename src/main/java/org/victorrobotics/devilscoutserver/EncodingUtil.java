package org.victorrobotics.devilscoutserver;

import java.util.Base64;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class EncodingUtil {
  private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
  private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();
  private static final ObjectMapper JSON  = new ObjectMapper();

  private EncodingUtil() {}

  public static String base64Encode(byte[] bytes) {
    return BASE64_ENCODER.encodeToString(bytes);
  }

  public static byte[] base64Decode(String base64) {
    return BASE64_DECODER.decode(base64);
  }

  public static String jsonEncode(Object json) {
    try {
      return JSON.writeValueAsString(json);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
