package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.cache.Cache.Value;
import org.victorrobotics.devilscoutserver.tba.EventCache.Event;
import org.victorrobotics.devilscoutserver.tba.MatchScheduleCache.MatchInfo;
import org.victorrobotics.devilscoutserver.tba.MatchScheduleCache.MatchSchedule;
import org.victorrobotics.devilscoutserver.tba.TeamListCache.TeamInfo;

import io.javalin.http.Context;

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

    long timestamp = eventsCache().lastModified();
    checkIfNoneMatch(ctx, timestamp);

    setResponseEtag(ctx, timestamp);
    ctx.writeJsonStream(eventsCache().values()
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

    Value<?, ?> entry = eventsCache().get(eventKey);
    if (entry == null) {
      throw eventNotFound(eventKey);
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
    if (eventsCache().get(eventKey) == null) {
      throw eventNotFound(eventKey);
    }

    Value<?, ?> entry = teamListsCache().get(eventKey);
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
    if (eventsCache().get(eventKey) == null) {
      throw eventNotFound(eventKey);
    }

    Value<?, ? extends MatchSchedule> entry = matchScheduleCache().get(eventKey);
    long timestamp = entry.lastModified();
    checkIfNoneMatch(ctx, timestamp);

    setResponseEtag(ctx, timestamp);
    ctx.json(entry);
  }
}
