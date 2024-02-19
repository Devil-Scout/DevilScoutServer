package org.victorrobotics.devilscoutserver.controller;

import io.javalin.http.Context;

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
   * <li>404 NotFound</li>
   * </ul>
   */
  public static void teams(Context ctx) {
    Session session = getValidSession(ctx);

    String eventKey = ctx.pathParam(EVENT_KEY_PATH_PARAM);
    if (!eventsCache().containsKey(eventKey)) {
      throw eventNotFound(eventKey);
    }

    verifyAnalysisAccess(eventKey, session);
    ctx.json(analysisCache().getStatistics(eventKey));
  }

  private static void verifyAnalysisAccess(String eventKey, Session session) {
    // TODO: verify team is permitted to access event analysis
  }
}
