package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.analysis.TeamStatistics;

import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;

public final class AnalysisController extends Controller {
  private static final String EVENT_KEY_PATH_PARAM = "eventKey";

  private AnalysisController() {}

  /**
   * GET /analysis/{eventKey}/teams
   * <p>
   * Success: 200 {@link TeamStatistics}
   * <p>
   * Errors:
   * <ul>
   * <li>400 BadRequest</li>
   * <li>401 Unauthorized</li>
   * </ul>
   */
  public static void teams(Context ctx) {
    Session session = getValidSession(ctx);

    String eventKey = ctx.pathParam(EVENT_KEY_PATH_PARAM);
    if (!eventInfoCache().containsKey(eventKey)) {
      throw new NotFoundResponse();
    }

    // TODO: verify team is permitted to access event analysis

    ctx.json(teamAnalysisCache().getEvent(eventKey));
  }
}
