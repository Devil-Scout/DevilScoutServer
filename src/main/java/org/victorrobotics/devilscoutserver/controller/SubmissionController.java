package org.victorrobotics.devilscoutserver.controller;

import static org.victorrobotics.devilscoutserver.EncodingUtil.jsonEncode;

import org.victorrobotics.bluealliance.Match.Alliance;
import org.victorrobotics.devilscoutserver.database.Team;
import org.victorrobotics.devilscoutserver.questions.Question;
import org.victorrobotics.devilscoutserver.questions.QuestionPage;
import org.victorrobotics.devilscoutserver.questions.Questions;
import org.victorrobotics.devilscoutserver.tba.MatchSchedule.MatchInfo;

import java.sql.SQLException;
import java.util.Map;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.NoContentResponse;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiRequired;
import io.javalin.openapi.OpenApiResponse;
import io.javalin.openapi.OpenApiSecurity;

public final class SubmissionController extends Controller {
  private SubmissionController() {}

  @OpenApi(path = "/submissions/match_scouting", methods = HttpMethod.POST, tags = "Submissions",
           description = "Submit match scouting data to the pool. "
               + "The current user's team must be attending the corresponding event.",
           requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = MatchSubmission.class)),
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "204"),
                         @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = ApiError.class)) })
  public static void submitMatchScouting(Context ctx) throws SQLException {
    Session session = getValidSession(ctx);
    Team team = teamDB().getTeam(session.getTeam());

    MatchSubmission payload = jsonDecode(ctx, MatchSubmission.class);

    if (!team.eventKey()
             .equals(payload.event())) {
      throw new BadRequestResponse("Cannot submit match if not attending event");
    }

    if (!payload.match()
                .startsWith(payload.event())) {
      throw new BadRequestResponse("Invalid match key for event");
    }

    MatchInfo match = matchScheduleCache().get(payload.event())
                                          .value()
                                          .getMatch(payload.match());
    if (match == null) {
      throw new BadRequestResponse("Match not found at event");
    } else if (!teamOnAlliance(payload.team(), match.getBlue())
        && !teamOnAlliance(payload.team(), match.getRed())) {
          throw new BadRequestResponse("Scouted team not in match");
        }

    if (!matchesSchema(payload.data(), Questions.MATCH_QUESTIONS)) {
      throw new BadRequestResponse("Invalid/expired submission format");
    }

    matchEntryDB().createEntry(payload.event(), payload.match(), session.getUser(),
                               session.getTeam(), payload.team(), jsonEncode(payload.data()));
    throw new NoContentResponse();
  }

  @OpenApi(path = "/submissions/pit_scouting", methods = HttpMethod.POST, tags = "Submissions",
           description = "Submit pit scouting data to the pool. "
               + "The current user's team must be attending the corresponding event.",
           requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = PitSubmission.class)),
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "204"),
                         @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = ApiError.class)) })
  public static void submitPitScouting(Context ctx) throws SQLException {
    Session session = getValidSession(ctx);
    Team team = teamDB().getTeam(session.getTeam());

    PitSubmission payload = jsonDecode(ctx, PitSubmission.class);

    if (!team.eventKey()
             .equals(payload.event())) {
      throw new BadRequestResponse("Cannot submit match if not attending event");
    }

    if (!matchesSchema(payload.data(), Questions.PIT_QUESTIONS)) {
      throw new BadRequestResponse("Invalid/expired submission format");
    }

    pitEntryDB().createEntry(payload.event(), null, session.getUser(), session.getTeam(),
                             payload.team(), jsonEncode(payload.data()));
    throw new NoContentResponse();
  }

  @OpenApi(path = "/submissions/drive_team_scouting", methods = HttpMethod.POST,
           tags = "Submissions",
           description = "Submit drive team scouting data to the pool. "
               + "The current user's team must be attending the corresponding event.",
           requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = DriveTeamSubmission.class)),
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "204"),
                         @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = ApiError.class)) })
  @SuppressWarnings("java:S3047")
  public static void submitDriveTeamScouting(Context ctx) throws SQLException {
    Session session = getValidSession(ctx);
    Team team = teamDB().getTeam(session.getTeam());

    DriveTeamSubmission payload = jsonDecode(ctx, DriveTeamSubmission.class);

    if (!team.eventKey()
             .equals(payload.event())) {
      throw new BadRequestResponse("Cannot submit match if not attending event");
    }

    if (!payload.match()
                .startsWith(payload.event())) {
      throw new BadRequestResponse("Invalid match key for event");
    }

    MatchInfo match = matchScheduleCache().get(payload.event())
                                          .value()
                                          .getMatch(payload.match());
    if (match == null) {
      throw new BadRequestResponse("Match not found at event");
    }

    Alliance.Color alliance;
    if (teamOnAlliance(session.getTeam(), match.getBlue())) {
      alliance = Alliance.Color.BLUE;
    } else if (teamOnAlliance(session.getTeam(), match.getRed())) {
      alliance = Alliance.Color.RED;
    } else {
      throw new BadRequestResponse("Scouting team not in match");
    }

    if (!teamOnAlliance(session.getTeam(), match.getBlue())
        && !teamOnAlliance(session.getTeam(), match.getRed())) {
      throw new BadRequestResponse("Scouting team not in match");
    }

    for (Map.Entry<String, Map<String, Object>> entry : payload.partners()
                                                               .entrySet()) {
      int scoutedTeam;
      try {
        scoutedTeam = Integer.parseInt(entry.getKey());
      } catch (NumberFormatException e) {
        throw new BadRequestResponse("Invalid team number " + entry.getKey());
      }

      if ((alliance == Alliance.Color.BLUE && !teamOnAlliance(scoutedTeam, match.getBlue()))
          || (alliance == Alliance.Color.RED && !teamOnAlliance(scoutedTeam, match.getRed()))) {
        throw new BadRequestResponse("Team " + scoutedTeam + " not on same alliance in match");
      }

      if (!matchesQuestions(entry.getValue(), Questions.DRIVE_TEAM_QUESTIONS)) {
        throw new BadRequestResponse("Invalid/expired submission format");
      }
    }

    for (Map.Entry<String, Map<String, Object>> entry : payload.partners()
                                                               .entrySet()) {
      driveTeamEntryDB().createEntry(payload.event(), payload.match(), session.getUser(),
                                     session.getTeam(), Integer.parseInt(entry.getKey()),
                                     jsonEncode(entry.getValue()));
    }

    throw new NoContentResponse();
  }

  private static boolean teamOnAlliance(int team, int[] alliance) {
    for (int t : alliance) {
      if (team == t) return true;
    }
    return false;
  }

  private static boolean matchesSchema(Map<String, Map<String, Object>> data,
                                       QuestionPage[] schema) {
    if (data.size() != schema.length) return false;

    for (QuestionPage page : schema) {
      Map<String, Object> pageData = data.get(page.key());
      if (pageData == null) return false;
      if (!matchesQuestions(pageData, page.questions())) return false;
    }

    return true;
  }

  private static boolean matchesQuestions(Map<String, Object> data, Question[] questions) {
    if (data.size() != questions.length) return false;

    for (Question question : questions) {
      Object value = data.get(question.getKey());
      if (value == null) return false;
      if (!question.isValidResponse(value)) return false;
    }

    return true;
  }

  static record MatchSubmission(@OpenApiRequired @OpenApiExample("2023nyrr_qm1") String match,
                                @OpenApiRequired @OpenApiExample("1559") int team,
                                @OpenApiRequired
                                @OpenApiExample("{}") Map<String, Map<String, Object>> data) {
    String event() {
      return match.substring(0, match.indexOf('_'));
    }
  }

  static record PitSubmission(@OpenApiRequired @OpenApiExample("2023nyrr") String event,
                              @OpenApiRequired @OpenApiExample("1559") int team,
                              @OpenApiRequired
                              @OpenApiExample("{}") Map<String, Map<String, Object>> data) {}

  static record DriveTeamSubmission(@OpenApiRequired @OpenApiExample("2023nyrr_qm1") String match,
                                    @OpenApiRequired
                                    @OpenApiExample("{}") Map<String, Map<String, Object>> partners) {
    String event() {
      return match.substring(0, match.indexOf('_'));
    }
  }
}
