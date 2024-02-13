package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.cache.Cache.Value;
import org.victorrobotics.devilscoutserver.tba.Event;
import org.victorrobotics.devilscoutserver.tba.TeamList.TeamInfo;
import org.victorrobotics.devilscoutserver.tba.MatchSchedule;
import org.victorrobotics.devilscoutserver.tba.MatchSchedule.MatchInfo;

import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;

public final class EventController extends Controller {
  private static final String EVENT_PATH_PARAM = "eventKey";

  private EventController() {}

  /**
   * GET /events
   * <p>
   * Success: 200 {@link Event}[]
   * <p>
   * Cached: 304 NotModified ({@code If-None-Match})
   * <p>
   * Errors:
   * <ul>
   * <li>401 Unauthorized</li>
   * </ul>
   */
  public static void getAllEvents(Context ctx) {
    getValidSession(ctx);

    long timestamp = eventInfoCache().lastModified();
    checkIfNoneMatch(ctx, timestamp);

    setResponseEtag(ctx, timestamp);
    ctx.writeJsonStream(eventInfoCache().values()
                                        .stream()
                                        .sorted());
  }

  /**
   * GET /events/{eventKey}
   * <p>
   * Success: 200 {@link Event}
   * <p>
   * Cached: 304 NotModified ({@code If-None-Match})
   * <p>
   * Errors:
   * <ul>
   * <li>401 Unauthorized</li>
   * <li>404 NotFound</li>
   * </ul>
   */
  public static void getEvent(Context ctx) {
    getValidSession(ctx);
    String eventKey = ctx.pathParam(EVENT_PATH_PARAM);

    Value<?, ?> entry = eventInfoCache().get(eventKey);
    if (entry == null) {
      throw new NotFoundResponse();
    }

    long timestamp = entry.lastModified();
    checkIfNoneMatch(ctx, timestamp);

    setResponseEtag(ctx, timestamp);
    ctx.json(entry);
  }

  /**
   * GET /events/{eventKey}/teams
   * <p>
   * Success: 200 {@link TeamInfo}[]
   * <p>
   * Cached: 304 NotModified ({@code If-None-Match})
   * <p>
   * Errors:
   * <ul>
   * <li>401 Unauthorized</li>
   * <li>404 NotFound</li>
   * </ul>
   */
  public static void getTeams(Context ctx) {
    getValidSession(ctx);

    String eventKey = ctx.pathParam(EVENT_PATH_PARAM);
    if (eventInfoCache().get(eventKey) == null) {
      throw new NotFoundResponse();
    }

    Value<?, ?> entry = eventTeamsCache().get(eventKey);
    long timestamp = entry.lastModified();
    checkIfNoneMatch(ctx, timestamp);

    setResponseEtag(ctx, timestamp);
    ctx.json(entry);
  }

  /**
   * GET /events/{eventKey}/matches
   * <p>
   * Success: 200 {@link MatchInfo}[]
   * <p>
   * Cached: 304 NotModified ({@code If-None-Match})
   * <p>
   * Errors:
   * <ul>
   * <li>401 Unauthorized</li>
   * <li>404 NotFound</li>
   * </ul>
   */
  public static void getMatchSchedule(Context ctx) {
    getValidSession(ctx);

    String eventKey = ctx.pathParam(EVENT_PATH_PARAM);
    if (eventInfoCache().get(eventKey) == null) {
      throw new NotFoundResponse();
    }

    Value<?, ? extends MatchSchedule> entry = matchScheduleCache().get(eventKey);
    long timestamp = entry.lastModified();
    checkIfNoneMatch(ctx, timestamp);

    setResponseEtag(ctx, timestamp);
    ctx.json(entry);
  }
}
