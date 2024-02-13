package org.victorrobotics.devilscoutserver.controller;

import static org.victorrobotics.devilscoutserver.EncodingUtil.jsonEncode;

import org.victorrobotics.bluealliance.Match.Alliance;
import org.victorrobotics.devilscoutserver.questions.Question;
import org.victorrobotics.devilscoutserver.tba.MatchScheduleCache.MatchInfo;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.NoContentResponse;

public final class SubmissionController extends Controller {
  private static final String MATCH_KEY_PATH_PARAM   = "matchKey";
  private static final String EVENT_KEY_PATH_PARAM   = "eventKey";
  private static final String TEAM_NUMBER_PATH_PARAM = "teamNum";

  private static final String BAD_SUBMISSION_MESSAGE = "Invalid/expired submission format";

  private SubmissionController() {}

  /**
   * POST /submissions/match/{matchKey}/{teamNum}
   * <p>
   * Request body: Map (as defined by /questions/match)
   * <p>
   * Success: 204 NoContent
   * <p>
   * Errors:
   * <ul>
   * <li>400 BadRequest</li>
   * <li>401 Unauthorized</li>
   * <li>403 Forbidden</li>
   * </ul>
   */
  public static void submitMatch(Context ctx) throws SQLException {
    Session session = getValidSession(ctx);

    String matchKey = ctx.pathParam(MATCH_KEY_PATH_PARAM);
    @SuppressWarnings("java:S1941") // move code later
    int teamNum = ctx.pathParamAsClass(TEAM_NUMBER_PATH_PARAM, Integer.class)
                     .get();
    String teamEvent = teamDB().getTeam(session.getTeam())
                               .eventKey();
    if (teamEvent.isEmpty() || !matchKey.startsWith(teamEvent)) {
      throw new ForbiddenResponse();
    }

    MatchInfo match = matchScheduleCache().get(teamEvent)
                                          .value()
                                          .get(matchKey);
    if (match == null) {
      throw new BadRequestResponse("Match not found at event");
    }

    if (!teamOnAlliance(teamNum, match.getBlue()) && !teamOnAlliance(teamNum, match.getRed())) {
      throw new BadRequestResponse("Scouted team not in match");
    }

    Map<String, Map<String, Object>> payload = jsonDecode(ctx, Map.class);
    if (!matchesSchema(payload, questions().getMatchQuestions())) {
      throw new BadRequestResponse(BAD_SUBMISSION_MESSAGE);
    }

    matchEntryDB().createEntry(teamEvent, matchKey, session.getUser(), session.getTeam(), teamNum,
                               jsonEncode(payload));
    throw new NoContentResponse();
  }

  /**
   * POST /submissions/pit/{eventKey}/{teamNum}
   * <p>
   * Request body: Map (as defined by /questions/pit)
   * <p>
   * Success: 204 NoContent
   * <p>
   * Errors:
   * <ul>
   * <li>400 BadRequest</li>
   * <li>401 Unauthorized</li>
   * <li>403 Forbidden</li>
   * </ul>
   */
  public static void submitPit(Context ctx) throws SQLException {
    Session session = getValidSession(ctx);

    @SuppressWarnings("java:S1941") // move code later
    int teamNum = ctx.pathParamAsClass(TEAM_NUMBER_PATH_PARAM, Integer.class)
                     .get();
    String teamEvent = teamDB().getTeam(session.getTeam())
                               .eventKey();
    if (!teamEvent.equals(ctx.pathParam(EVENT_KEY_PATH_PARAM))) {
      throw new ForbiddenResponse();
    }

    Map<String, Map<String, Object>> payload = jsonDecode(ctx, Map.class);
    if (!matchesSchema(payload, questions().getPitQuestions())) {
      throw new BadRequestResponse(BAD_SUBMISSION_MESSAGE);
    }

    pitEntryDB().createEntry(teamEvent, null, session.getUser(), session.getTeam(), teamNum,
                             jsonEncode(payload));
    throw new NoContentResponse();
  }

  /**
   * POST /submissions/drive-team/{matchKey}
   * <p>
   * Request body: Map (as defined by /questions/drive-team)
   * <p>
   * Success: 204 NoContent
   * <p>
   * Errors:
   * <ul>
   * <li>400 BadRequest</li>
   * <li>401 Unauthorized</li>
   * <li>403 Forbidden</li>
   * </ul>
   */
  @SuppressWarnings("java:S3047")
  public static void submitDriveTeam(Context ctx) throws SQLException {
    Session session = getValidSession(ctx);

    String matchKey = ctx.pathParam(MATCH_KEY_PATH_PARAM);
    String teamEvent = teamDB().getTeam(session.getTeam())
                               .eventKey();
    if (teamEvent.isEmpty() || !matchKey.startsWith(teamEvent)) {
      throw new ForbiddenResponse();
    }

    MatchInfo match = matchScheduleCache().get(teamEvent)
                                          .value()
                                          .get(matchKey);
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

    Map<String, Map<String, Object>> payload = jsonDecode(ctx, Map.class);
    for (Map.Entry<String, Map<String, Object>> entry : payload.entrySet()) {
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

      if (!matchesQuestions(entry.getValue(), questions().getDriveTeamQuestions())) {
        throw new BadRequestResponse(BAD_SUBMISSION_MESSAGE);
      }
    }

    for (Map.Entry<String, Map<String, Object>> entry : payload.entrySet()) {
      driveTeamEntryDB().createEntry(teamEvent, matchKey, session.getUser(), session.getTeam(),
                                     Integer.parseInt(entry.getKey()),
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
                                       List<Question.Page> schema) {
    if (data.size() != schema.size()) return false;

    for (Question.Page page : schema) {
      Map<String, Object> pageData = data.get(page.key());
      if (pageData == null) return false;
      if (!matchesQuestions(pageData, page.questions())) return false;
    }

    return true;
  }

  private static boolean matchesQuestions(Map<String, Object> data, List<Question> questions) {
    if (data.size() != questions.size()) return false;

    for (Question question : questions) {
      Object value = data.get(question.key);
      if (value == null) return false;
      if (!question.isValidResponse(value)) return false;
    }

    return true;
  }
}
