package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.analysis.TeamStatistics;
import org.victorrobotics.devilscoutserver.tba.TeamInfo;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Objects;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;

public final class AnalysisController extends Controller {
  private AnalysisController() {}

  /**
   * GET /analysis/teams
   * <p>
   * Success: 200 {@link TeamStatistics}
   * <p>
   * Errors:
   * <ul>
   * <li>400 BadRequest</li>
   * <li>401 Unauthorized</li>
   * </ul>
   */
  public static void teams(Context ctx) throws SQLException {
    Session session = getValidSession(ctx);
    String eventKey = teamDB().getTeam(session.getTeam())
                              .eventKey();
    if ("".equals(eventKey)) {
      throw new BadRequestResponse("Team not attending event");
    }

    Collection<TeamInfo> teams = eventTeamsCache().get(eventKey)
                                                  .value()
                                                  .teams();
    ctx.writeJsonStream(teams.stream()
                             .map(team -> teamAnalysisCache().get(team.getNumber()))
                             .filter(Objects::nonNull));
  }
}
