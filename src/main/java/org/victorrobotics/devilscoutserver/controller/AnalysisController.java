package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.analysis.TeamStatistics;
import org.victorrobotics.devilscoutserver.tba.EventTeam;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Objects;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;
import io.javalin.openapi.OpenApiSecurity;

public final class AnalysisController extends Controller {
  private AnalysisController() {}

  @OpenApi(path = "/analysis/teams", methods = HttpMethod.GET, tags = "Analysis",
           description = "Get the server-generated analysis of the teams at the current event.",
           headers = @OpenApiParam(name = "If-None-Match", type = String.class, required = false),
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = TeamStatistics[].class)),
                         @OpenApiResponse(status = "304"),
                         @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = Error.class)) })
  public static void teams(Context ctx) throws SQLException {
    Session session = getValidSession(ctx);
    String eventKey = teamDB().getTeam(session.getTeam())
                              .eventKey();
    if ("".equals(eventKey)) {
      throw new BadRequestResponse("Team not attending event");
    }

    Collection<EventTeam> teams = eventTeamsCache().get(eventKey)
                                                   .value()
                                                   .teams();
    ctx.writeJsonStream(teams.stream()
                             .map(team -> teamAnalysisCache().get(team.getNumber()))
                             .filter(Objects::nonNull));
  }
}
