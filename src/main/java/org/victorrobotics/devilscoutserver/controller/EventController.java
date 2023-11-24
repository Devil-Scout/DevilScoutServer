package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.cache.CacheValue;
import org.victorrobotics.devilscoutserver.data.EventInfo;
import org.victorrobotics.devilscoutserver.data.MatchSchedule;
import org.victorrobotics.devilscoutserver.data.TeamInfo;
import org.victorrobotics.devilscoutserver.data.TeamList;

import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;
import io.javalin.openapi.OpenApiSecurity;

public final class EventController extends Controller {
  private EventController() {}

  @OpenApi(path = "/events", methods = HttpMethod.GET, tags = "Events", summary = "USER",
           description = "Get information on all current-year events.",
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = EventInfo[].class)),
                         @OpenApiResponse(status = "301"), @OpenApiResponse(status = "401"),
                         @OpenApiResponse(status = "404") })
  public static void getAllEvents(Context ctx) {
    getValidSession(ctx);

    checkIfNoneMatch(ctx, eventCache().timestamp());

    ctx.writeJsonStream(eventCache().values()
                                    .sorted());
  }

  @OpenApi(path = "/events/{event}", methods = HttpMethod.GET, tags = "Events", summary = "USER",
           description = "Get information on an event.",
           pathParams = @OpenApiParam(name = "event", type = String.class, required = true,
                                      example = "2023paca"),
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = EventInfo.class)),
                         @OpenApiResponse(status = "301"), @OpenApiResponse(status = "401"),
                         @OpenApiResponse(status = "404") })
  public static void getEvent(Context ctx) {
    getValidSession(ctx);

    String eventKey = ctx.pathParam("event");
    if (!eventCache().containsKey(eventKey)) {
      throw new NotFoundResponse();
    }

    CacheValue<?, EventInfo> entry = eventCache().get(eventKey);
    checkIfNoneMatch(ctx, entry.timestamp());

    ctx.json(entry.value());
  }

  @OpenApi(path = "/events/{event}/teams", methods = HttpMethod.GET, tags = "Events",
           summary = "USER", description = "Get the list of teams attending an event.",
           pathParams = @OpenApiParam(name = "event", type = String.class, required = true,
                                      example = "2023paca"),
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = TeamInfo[].class)),
                         @OpenApiResponse(status = "301"), @OpenApiResponse(status = "401"),
                         @OpenApiResponse(status = "404") })
  public static void getTeams(Context ctx) {
    getValidSession(ctx);

    String eventKey = ctx.pathParam("event");
    if (!eventCache().containsKey(eventKey)) {
      throw new NotFoundResponse();
    }

    CacheValue<?, TeamList> entry = eventTeamsCache().get(eventKey);
    checkIfNoneMatch(ctx, entry.timestamp());

    ctx.json(entry.value());
  }

  @OpenApi(path = "/events/{event}/match-schedule", methods = HttpMethod.GET, tags = "Events",
           summary = "USER", description = "Get the match schedule at an event.",
           pathParams = @OpenApiParam(name = "event", type = String.class, required = true,
                                      example = "2023paca"),
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = MatchSchedule.MatchInfo[].class)),
                         @OpenApiResponse(status = "301"), @OpenApiResponse(status = "401"),
                         @OpenApiResponse(status = "404") })
  public static void getMatchSchedule(Context ctx) {
    getValidSession(ctx);

    String eventKey = ctx.pathParam("event");
    if (!eventCache().containsKey(eventKey)) {
      throw new NotFoundResponse();
    }

    CacheValue<?, MatchSchedule> entry = matchScheduleCache().get(eventKey);
    checkIfNoneMatch(ctx, entry.timestamp());

    ctx.json(entry.value());
  }
}
