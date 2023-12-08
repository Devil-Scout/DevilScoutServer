package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.tba.cache.CacheValue;
import org.victorrobotics.devilscoutserver.tba.data.EventInfo;
import org.victorrobotics.devilscoutserver.tba.data.EventTeams;
import org.victorrobotics.devilscoutserver.tba.data.MatchSchedule;
import org.victorrobotics.devilscoutserver.tba.data.TeamInfo;

import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;
import io.javalin.openapi.OpenApiSecurity;

public final class EventController extends Controller {
  private static final String EVENT_PATH_PARAM = "event";

  private EventController() {}

  @OpenApi(path = "/events", methods = HttpMethod.GET, tags = "Event Data", summary = "USER",
           description = "Get information on all current-year events.",
           headers = @OpenApiParam(name = "If-None-Match", type = Long.class, required = false),
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = EventInfo[].class)),
                         @OpenApiResponse(status = "304"),
                         @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = Error.class)) })
  public static void getAllEvents(Context ctx) {
    getValidSession(ctx);

    long timestamp = eventCache().timestamp();
    checkIfNoneMatch(ctx, timestamp);

    setResponseEtag(ctx, timestamp);
    ctx.writeJsonStream(eventCache().values());
  }

  @OpenApi(path = "/events/{event}", methods = HttpMethod.GET, tags = "Event Data",
           summary = "USER", description = "Get information on an event.",
           pathParams = @OpenApiParam(name = EVENT_PATH_PARAM, type = String.class,
                                      required = true),
           headers = @OpenApiParam(name = "If-None-Match", type = Long.class, required = false),
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = EventInfo.class)),
                         @OpenApiResponse(status = "304"), @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "404",
                                          content = @OpenApiContent(from = Error.class)) })
  public static void getEvent(Context ctx) {
    getValidSession(ctx);
    String eventKey = ctx.pathParam(EVENT_PATH_PARAM);

    CacheValue<?, EventInfo> entry = eventCache().get(eventKey);
    if (entry == null) {
      throwEventNotFound(eventKey);
    }

    long timestamp = entry.lastRefresh();
    checkIfNoneMatch(ctx, timestamp);

    setResponseEtag(ctx, timestamp);
    ctx.json(entry);
  }

  @OpenApi(path = "/events/{event}/teams", methods = HttpMethod.GET, tags = "Event Data",
           summary = "USER", description = "Get the list of teams attending an event.",
           pathParams = @OpenApiParam(name = EVENT_PATH_PARAM, type = String.class,
                                      required = true),
           headers = @OpenApiParam(name = "If-None-Match", type = Long.class, required = false),
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = TeamInfo[].class)),
                         @OpenApiResponse(status = "304"), @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "404",
                                          content = @OpenApiContent(from = Error.class)) })
  public static void getTeams(Context ctx) {
    getValidSession(ctx);

    String eventKey = ctx.pathParam(EVENT_PATH_PARAM);
    if (!eventCache().containsKey(eventKey)) {
      throwEventNotFound(eventKey);
    }

    CacheValue<?, EventTeams> entry = eventTeamsCache().get(eventKey);
    long timestamp = entry.lastRefresh();
    checkIfNoneMatch(ctx, timestamp);

    setResponseEtag(ctx, timestamp);
    ctx.json(entry);
  }

  @OpenApi(path = "/events/{event}/match-schedule", methods = HttpMethod.GET, tags = "Event Data",
           summary = "USER", description = "Get the match schedule at an event.",
           pathParams = @OpenApiParam(name = EVENT_PATH_PARAM, type = String.class,
                                      required = true),
           headers = @OpenApiParam(name = "If-None-Match", type = Long.class, required = false),
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = MatchSchedule.MatchInfo[].class)),
                         @OpenApiResponse(status = "304"), @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "404",
                                          content = @OpenApiContent(from = Error.class)) })
  public static void getMatchSchedule(Context ctx) {
    getValidSession(ctx);

    String eventKey = ctx.pathParam(EVENT_PATH_PARAM);
    if (!eventCache().containsKey(eventKey)) {
      throwEventNotFound(eventKey);
    }

    CacheValue<?, MatchSchedule> entry = matchScheduleCache().get(eventKey);
    long timestamp = entry.lastRefresh();
    checkIfNoneMatch(ctx, timestamp);

    setResponseEtag(ctx, timestamp);
    ctx.json(entry);
  }
}
