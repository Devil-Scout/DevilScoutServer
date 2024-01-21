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

public final class SubmissionController extends Controller {
  private SubmissionController() {}

  /**
   * POST /submissions/match-scouting
   * <p>
   * Request body: {@link MatchSubmission}
   * <p>
   * Success: 204 NoContent
   * <p>
   * Errors:
   * <ul>
   * <li>400 BadRequest</li>
   * <li>401 Unauthorized</li>
   * </ul>
   */
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

  /**
   * POST /submissions/pit-scouting
   * <p>
   * Request body: {@link PitSubmission}
   * <p>
   * Success: 204 NoContent
   * <p>
   * Errors:
   * <ul>
   * <li>400 BadRequest</li>
   * <li>401 Unauthorized</li>
   * </ul>
   */
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

  /**
   * POST /submissions/drive-team-scouting
   * <p>
   * Request body: {@link PitSubmission}
   * <p>
   * Success: 204 NoContent
   * <p>
   * Errors:
   * <ul>
   * <li>400 BadRequest</li>
   * <li>401 Unauthorized</li>
   * </ul>
   */
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

  static record MatchSubmission(String match,
                                int team,
                                Map<String, Map<String, Object>> data) {
    String event() {
      return match.substring(0, match.indexOf('_'));
    }
  }

  static record PitSubmission(String event,
                              int team,
                              Map<String, Map<String, Object>> data) {}

  static record DriveTeamSubmission(String match,
                                    Map<String, Map<String, Object>> partners) {
    String event() {
      return match.substring(0, match.indexOf('_'));
    }
  }
}
