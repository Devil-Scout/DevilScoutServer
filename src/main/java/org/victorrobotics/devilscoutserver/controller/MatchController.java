package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.data.MatchSchedule;
import org.victorrobotics.devilscoutserver.data.Session;
import org.victorrobotics.devilscoutserver.data.TeamConfig;

import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

public class MatchController extends Controller {
  @OpenApi(path = "/matches", methods = HttpMethod.GET, tags = "Downloads",
           description = "Gets the match schedule for the current event",
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = MatchSchedule.class)),
                         @OpenApiResponse(status = "401") })
  public static void matches(Context ctx) {
    Session session = getValidSession(ctx);
    TeamConfig config = teamDB().get(session.getTeam());
    String eventKey = config.getEventKey();
    MatchSchedule schedule = matchSchedules().get(eventKey);
    synchronized (schedule) {
      ctx.json(schedule);
    }
  }
}
