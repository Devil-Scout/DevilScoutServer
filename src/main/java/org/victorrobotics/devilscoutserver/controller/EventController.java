package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.cache.CacheValue;
import org.victorrobotics.devilscoutserver.tba.Event;
import org.victorrobotics.devilscoutserver.tba.EventTeam;
import org.victorrobotics.devilscoutserver.tba.EventTeamList;
import org.victorrobotics.devilscoutserver.tba.MatchSchedule;

import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;
import io.javalin.openapi.OpenApiSecurity;

public final class EventController extends Controller {
  private static final String EVENT_PATH_PARAM = "event";

  private EventController() {}

  @OpenApi(path = "/events", methods = HttpMethod.GET, tags = "Event Info",
           description = "Get information on all current-year events.",
           headers = @OpenApiParam(name = "If-None-Match", type = Long.class, required = false),
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = Event[].class)),
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

  @OpenApi(path = "/events/{event}", methods = HttpMethod.GET, tags = "Event Info",
           description = "Get information on an event.",
           pathParams = @OpenApiParam(name = EVENT_PATH_PARAM, type = String.class,
                                      required = true),
           headers = @OpenApiParam(name = "If-None-Match", type = Long.class, required = false),
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = Event.class)),
                         @OpenApiResponse(status = "304"),
                         @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "404",
                                          content = @OpenApiContent(from = Error.class)) })
  public static void getEvent(Context ctx) {
    getValidSession(ctx);
    String eventKey = ctx.pathParam(EVENT_PATH_PARAM);

    CacheValue<?, Event> entry = eventCache().get(eventKey);
    if (entry == null) {
      throw new NotFoundResponse();
    }

    long timestamp = entry.lastRefresh();
    checkIfNoneMatch(ctx, timestamp);

    setResponseEtag(ctx, timestamp);
    ctx.json(entry);
  }

  @OpenApi(path = "/events/{event}/teams", methods = HttpMethod.GET, tags = "Event Info",
           description = "Get the list of teams attending an event.",
           pathParams = @OpenApiParam(name = EVENT_PATH_PARAM, type = String.class,
                                      required = true),
           headers = @OpenApiParam(name = "If-None-Match", type = Long.class, required = false),
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = EventTeam[].class)),
                         @OpenApiResponse(status = "304"),
                         @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "404",
                                          content = @OpenApiContent(from = Error.class)) })
  public static void getTeams(Context ctx) {
    getValidSession(ctx);

    String eventKey = ctx.pathParam(EVENT_PATH_PARAM);
    if (!eventCache().containsKey(eventKey)) {
      throw new NotFoundResponse();
    }

    CacheValue<?, EventTeamList> entry = eventTeamsCache().get(eventKey);
    long timestamp = entry.lastRefresh();
    checkIfNoneMatch(ctx, timestamp);

    setResponseEtag(ctx, timestamp);
    ctx.json(entry);
  }

  @OpenApi(path = "/events/{event}/match-schedule", methods = HttpMethod.GET, tags = "Event Info",
           description = "Get the match schedule at an event.",
           pathParams = @OpenApiParam(name = EVENT_PATH_PARAM, type = String.class,
                                      required = true),
           headers = @OpenApiParam(name = "If-None-Match", type = Long.class, required = false),
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = MatchSchedule.MatchInfo[].class)),
                         @OpenApiResponse(status = "304"),
                         @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "404",
                                          content = @OpenApiContent(from = Error.class)) })
  public static void getMatchSchedule(Context ctx) {
    getValidSession(ctx);

    String eventKey = ctx.pathParam(EVENT_PATH_PARAM);
    if (!eventCache().containsKey(eventKey)) {
      throw new NotFoundResponse();
    }

    CacheValue<?, MatchSchedule> entry = matchScheduleCache().get(eventKey);
    long timestamp = entry.lastRefresh();
    checkIfNoneMatch(ctx, timestamp);

    setResponseEtag(ctx, timestamp);
    ctx.json(entry);
  }
}
