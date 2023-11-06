package org.victorrobotics.devilscoutserver;

import java.util.Base64;
import java.util.HexFormat;

import io.javalin.http.Handler;

public abstract class RequestHandler implements Handler {
  protected static final HexFormat      HEX_FORMAT     = HexFormat.of();
  protected static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();
  protected static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();

  protected RequestHandler() {}
}
