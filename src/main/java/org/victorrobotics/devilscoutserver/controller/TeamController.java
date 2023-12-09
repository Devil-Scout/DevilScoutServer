package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.database.Session;
import org.victorrobotics.devilscoutserver.database.Team;
import org.victorrobotics.devilscoutserver.database.User;
import org.victorrobotics.devilscoutserver.database.UserAccessLevel;
import org.victorrobotics.devilscoutserver.tba.data.TeamInfo;

import java.util.Collection;

import io.javalin.http.ConflictResponse;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;
import io.javalin.openapi.OpenApiSecurity;

public final class TeamController extends Controller {
  private TeamController() {}

  @OpenApi(path = "/teams", methods = HttpMethod.GET, tags = "Teams", summary = "SUDO",
           description = "Get all registered teams. Requires SUDO.",
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = Team[].class)),
                         @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "403",
                                          content = @OpenApiContent(from = Error.class)) })
  public static void teamList(Context ctx) {
    getValidSession(ctx, UserAccessLevel.SUDO);
    ctx.writeJsonStream(teamDB().teams());
  }

  @OpenApi(path = "/teams", methods = HttpMethod.POST, tags = "Teams", summary = "SUDO",
           description = "Register a new team. Requires SUDO.",
           requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = TeamRegistration.class)),
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "201",
                                          content = @OpenApiContent(from = Team.class)),
                         @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "403",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "409",
                                          content = @OpenApiContent(from = Error.class)) })
  public static void registerTeam(Context ctx) {
    getValidSession(ctx, UserAccessLevel.SUDO);
    TeamRegistration registration = jsonDecode(ctx, TeamRegistration.class);

    int teamNum = registration.number();
    checkTeamRange(teamNum);

    if (teamDB().get(teamNum) != null) {
      throw new ConflictResponse("Team with number " + teamNum + " already exists");
    }

    Team team = new Team(teamNum, registration.name());
    teamDB().put(team);
    ctx.json(team);
    throwCreated();
  }

  @OpenApi(path = "/teams/{team}", methods = HttpMethod.GET, tags = "Teams", summary = "USER, SUDO",
           description = "Get a registered team. Requires SUDO if from a different team.",
           pathParams = @OpenApiParam(name = "team", type = Integer.class, required = true),
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = TeamInfo.class)),
                         @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "403",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "404",
                                          content = @OpenApiContent(from = Error.class)) })
  public static void getTeam(Context ctx) {
    Session session = getValidSession(ctx);
    int teamNum = ctx.pathParamAsClass("team", Integer.class)
                     .get();
    checkTeamRange(teamNum);

    if (teamNum != session.getTeam()) {
      session.verifyAccess(UserAccessLevel.SUDO);
    }

    Team team = teamDB().get(teamNum);
    if (team == null) {
      throwTeamNotFound(teamNum);
    }

    ctx.json(team);
  }

  @OpenApi(path = "/teams/{team}", methods = HttpMethod.PATCH, tags = "Teams",
           summary = "ADMIN, SUDO",
           description = "Edit a registered team. "
               + "Requires ADMIN if from the same team, or SUDO if from a different team.",
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
  public static void editTeam(Context ctx) {
    Session session = getValidSession(ctx, UserAccessLevel.ADMIN);
    int teamNum = ctx.pathParamAsClass("team", Integer.class)
                     .get();
    checkTeamRange(teamNum);

    if (session.getTeam() != teamNum) {
      session.verifyAccess(UserAccessLevel.SUDO);
    }

    Team team = teamDB().get(teamNum);
    if (team == null) {
      throwTeamNotFound(teamNum);
    }

    TeamEdits edits = jsonDecode(ctx, TeamEdits.class);

    if (edits.name() != null) {
      team.setName(edits.name());
    }

    if (edits.eventKey() != null) {
      team.setEventKey(edits.eventKey());
    }

    ctx.json(team);
  }

  @OpenApi(path = "/teams/{team}", methods = HttpMethod.DELETE, tags = "Teams", summary = "SUDO",
           description = "Unregister a team. Requires SUDO.",
           pathParams = @OpenApiParam(name = "team", type = Integer.class, required = true),
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "204"),
                         @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "403",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "404",
                                          content = @OpenApiContent(from = Error.class)) })
  public static void unregisterTeam(Context ctx) {
    getValidSession(ctx, UserAccessLevel.SUDO);
    int teamNum = ctx.pathParamAsClass("team", Integer.class)
                     .get();
    checkTeamRange(teamNum);

    Team team = teamDB().get(teamNum);
    if (team == null) {
      throwTeamNotFound(teamNum);
    }

    teamDB().remove(team);
    throwNoContent();
  }

  @OpenApi(path = "/teams/{team}/users", methods = HttpMethod.GET, tags = "Users",
           summary = "ADMIN, SUDO",
           description = "Get all registered users on the specified team. "
               + "Requires ADMIN if from the same team, or SUDO if from a different team.",
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
  public static void usersOnTeam(Context ctx) {
    Session session = getValidSession(ctx, UserAccessLevel.ADMIN);
    int team = ctx.pathParamAsClass("team", Integer.class)
                  .get();
    checkTeamRange(team);

    if (team != session.getTeam()) {
      session.verifyAccess(UserAccessLevel.SUDO);
    }

    Collection<User> users = userDB().usersByTeam(team);
    if (users == null) {
      throwTeamNotFound(team);
    }

    ctx.writeJsonStream(users.stream());
  }

  static record TeamRegistration(@OpenApiExample("1559") int number,
                                 @OpenApiExample("Devil Tech") String name) {}

  static record TeamEdits(@OpenApiExample("Devil Tech") String name,
                          @OpenApiExample("2023nyrr") String eventKey) {}
}
