package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.database.Team;

import java.sql.SQLException;

import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.NotFoundResponse;

public final class TeamController extends Controller {
  private static final String TEAM_PATH_PARAM = "teamNum";

  private TeamController() {}

  /**
   * GET /teams/{teamNum}
   * <p>
   * Success: 200 {@link Team}
   * <p>
   * Errors:
   * <ul>
   * <li>401 Unauthorized</li>
   * <li>403 Forbidden</li>
   * <li>404 NotFound</li>
   * </ul>
   */
  public static void getTeam(Context ctx) throws SQLException {
    Session session = getValidSession(ctx);
    int teamNum = ctx.pathParamAsClass(TEAM_PATH_PARAM, Integer.class)
                     .get();
    checkTeamRange(teamNum);

    if (teamNum != session.getTeam()) {
      throw new ForbiddenResponse();
    }

    Team team = teamDB().getTeam(teamNum);
    if (team == null) {
      throw new NotFoundResponse();
    }

    ctx.json(team);
  }

  /**
   * PATCH /teams/{teamNum}
   * <p>
   * Request body: {@link TeamEdits}
   * <p>
   * Success: 200 {@link Team}
   * <p>
   * Errors:
   * <ul>
   * <li>400 BadRequest</li>
   * <li>401 Unauthorized</li>
   * <li>403 Forbidden</li>
   * <li>404 NotFound</li>
   * </ul>
   */
  public static void editTeam(Context ctx) throws SQLException {
    Session session = getValidSession(ctx);
    session.verifyAdmin();
    int teamNum = ctx.pathParamAsClass(TEAM_PATH_PARAM, Integer.class)
                     .get();

    if (session.getTeam() != teamNum) {
      throw new ForbiddenResponse();
    }

    Team team = teamDB().getTeam(teamNum);
    if (team == null) {
      throw new NotFoundResponse();
    }

    TeamEdits edits = jsonDecode(ctx, TeamEdits.class);

    String eventKey = edits.eventKey();
    if (eventKey != null && !"".equals(eventKey) && eventInfoCache().get(eventKey) == null) {
      throw new NotFoundResponse();
    }

    team = teamDB().editTeam(teamNum, edits.name(), eventKey);
    ctx.json(team);
  }

  static record TeamEdits(String name,
                          String eventKey) {}
}
