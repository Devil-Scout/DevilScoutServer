package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.database.Team;
import org.victorrobotics.devilscoutserver.database.User;

import java.sql.SQLException;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiRequired;
import io.javalin.openapi.OpenApiResponse;
import io.javalin.openapi.OpenApiSecurity;

public final class TeamController extends Controller {
  private TeamController() {}

  @OpenApi(path = "/teams/{team}", methods = HttpMethod.GET, tags = "Teams",
           description = "Get a registered team's information. Requesting user must be on the same team.",
           pathParams = @OpenApiParam(name = "team", type = Integer.class, required = true),
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = Team.class)),
                         @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "403",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "404",
                                          content = @OpenApiContent(from = Error.class)) })
  public static void getTeam(Context ctx) throws SQLException {
    Session session = getValidSession(ctx);
    int teamNum = ctx.pathParamAsClass("team", Integer.class)
                     .get();
    checkTeamRange(teamNum);

    if (teamNum != session.getTeam()) {
      throwForbidden();
      return;
    }

    Team team = teamDB().getTeam(teamNum);
    if (team == null) {
      throwTeamNotFound(teamNum);
      return;
    }

    ctx.json(team);
  }

  @OpenApi(path = "/teams/{team}", methods = HttpMethod.PATCH, tags = "Teams", summary = "ADMIN",
           description = "Edit your team. " + "Requires ADMIN, who must be from the same team.",
           pathParams = @OpenApiParam(name = "team", type = Integer.class, required = true),
           requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = TeamEdits.class)),
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = Team.class)),
                         @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "403",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "404",
                                          content = @OpenApiContent(from = Error.class)) })
  public static void editTeam(Context ctx) throws SQLException {
    Session session = getValidSession(ctx);
    session.verifyAdmin();
    int teamNum = ctx.pathParamAsClass("team", Integer.class)
                     .get();

    if (session.getTeam() != teamNum) {
      throwForbidden();
      return;
    }

    Team team = teamDB().getTeam(teamNum);
    if (team == null) {
      throwTeamNotFound(teamNum);
      return;
    }

    TeamEdits edits = jsonDecode(ctx, TeamEdits.class);

    String eventKey = edits.eventKey();
    if (eventKey != null && !"".equals(eventKey) && !eventCache().containsKey(eventKey)) {
      throwEventNotFound(eventKey);
      return;
    }

    team = teamDB().editTeam(teamNum, edits.name(), eventKey);
    ctx.json(team);
  }

  @OpenApi(path = "/teams/{team}/users", methods = HttpMethod.GET, tags = "Teams",
           summary = "ADMIN",
           description = "Get all registered users on the specified team. "
               + "Requires ADMIN, who must be from the same team.",
           pathParams = @OpenApiParam(name = "team", type = Integer.class, required = true),
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = User[].class)),
                         @OpenApiResponse(status = "400",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "403",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "404",
                                          content = @OpenApiContent(from = Error.class)) })
  public static void usersOnTeam(Context ctx) throws SQLException {
    Session session = getValidSession(ctx);
    session.verifyAdmin();

    int team = ctx.pathParamAsClass("team", Integer.class)
                  .get();
    checkTeamRange(team);

    if (team != session.getTeam()) {
      throwForbidden();
      return;
    }

    if (!teamDB().containsTeam(team)) {
      throwTeamNotFound(team);
      return;
    }

    Collection<User> users = userDB().usersOnTeam(team);
    ctx.writeJsonStream(users.stream());
  }

  static record TeamRegistration(@OpenApiRequired @OpenApiExample("1559")
  @JsonProperty(required = true) int number,
                                 @OpenApiRequired @OpenApiExample("Devil Tech")
                                 @JsonProperty(required = true) String name) {}

  static record TeamEdits(@OpenApiExample("Devil Tech") String name,
                          @OpenApiExample("2023nyrr") String eventKey) {}
}
