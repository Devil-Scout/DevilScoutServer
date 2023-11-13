package org.victorrobotics.devilscoutserver.controller;

import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequired;
import io.javalin.openapi.OpenApiResponse;

public class StatusController extends Controller {
  @OpenApi(path = "/status", methods = HttpMethod.GET, tags = "Status",
           description = "Gets the status of the server.",
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = Status.class)) })
  public static void status(Context ctx) {
    ctx.json(new Status("okay"));
  }

  static record Status(@OpenApiRequired @OpenApiExample("okay") String status) {}
}
