package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.data.ServerStatus;

import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

public class StatusController extends Controller {
  @OpenApi(path = "/status", methods = HttpMethod.GET, tags = "Status",
           description = "Gets the status of the server.",
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = ServerStatus.class)),
                         @OpenApiResponse(status = "410") })
  public static void status(Context ctx) {
    getValidSession(ctx);
    ctx.json(new ServerStatus("okay"));
  }
}
