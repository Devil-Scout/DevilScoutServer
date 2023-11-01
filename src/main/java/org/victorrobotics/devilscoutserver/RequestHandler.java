package org.victorrobotics.devilscoutserver;

import java.io.IOException;
import java.util.HexFormat;
import java.util.Base64;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public abstract class RequestHandler implements HttpHandler {
  protected static final HexFormat HEX_FORMAT = HexFormat.of();
  protected static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();
  protected static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();

  protected RequestHandler() {}

  protected static void closeExchange(HttpExchange exchange, int responseCode, String response)
      throws IOException {
    System.out.println(responseCode + ": " + response);
    exchange.sendResponseHeaders(responseCode, response.length());
    exchange.getResponseBody()
            .write(response.getBytes());
    exchange.close();
  }

  protected static void closeExchange(HttpExchange exchange, int responseCode) throws IOException {
    System.out.println(responseCode);
    exchange.sendResponseHeaders(responseCode, -1);
    exchange.close();
  }
}
