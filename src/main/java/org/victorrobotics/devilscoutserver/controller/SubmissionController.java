package org.victorrobotics.devilscoutserver.controller;

import static org.victorrobotics.devilscoutserver.EncodingUtil.jsonEncode;

import org.victorrobotics.bluealliance.Match.Alliance;
import org.victorrobotics.devilscoutserver.questions.Question;
import org.victorrobotics.devilscoutserver.session.Session;
import org.victorrobotics.devilscoutserver.tba.MatchScheduleCache.MatchInfo;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public final class SubmissionController extends Controller {
  private static final String MATCH_KEY_PATH_PARAM   = "matchKey";
  private static final String EVENT_KEY_PATH_PARAM   = "eventKey";
  private static final String TEAM_NUMBER_PATH_PARAM = "teamNum";

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
    String eventKey = teamDB().getTeam(session.getTeam())
                              .eventKey();
    if (eventKey.isEmpty() || !matchKey.startsWith(eventKey)) {
      throw wrongEvent(eventKey, session.getTeam());
    }

    MatchInfo match = matchScheduleCache().get(eventKey)
                                          .value()
                                          .get(matchKey);
    if (match == null) {
      throw matchNotFound(matchKey);
    }

    if (!teamOnAlliance(teamNum, match.getBlue()) && !teamOnAlliance(teamNum, match.getRed())) {
      throw teamNotInMatch(matchKey, teamNum);
    }

    Map<String, Map<String, Object>> payload = jsonDecode(ctx, Map.class);
    if (!matchesSchema(payload, questions(eventKey).getMatchQuestions())) {
      throw schemaMismatch(eventKey);
    }

    matchEntryDB().createEntry(eventKey, matchKey, session.getUser(), session.getTeam(), teamNum,
                               jsonEncode(payload));
    analysisCache().scheduleRefresh(eventKey, teamNum);
    ctx.status(HttpStatus.NO_CONTENT);
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
    String eventKey = teamDB().getTeam(session.getTeam())
                              .eventKey();
    if (!eventKey.equals(ctx.pathParam(EVENT_KEY_PATH_PARAM))) {
      throw wrongEvent(eventKey, session.getTeam());
    }

    Map<String, Map<String, Object>> payload = jsonDecode(ctx, Map.class);
    if (!matchesSchema(payload, questions(eventKey).getPitQuestions())) {
      throw schemaMismatch(eventKey);
    }

    pitEntryDB().createEntry(eventKey, null, session.getUser(), session.getTeam(), teamNum,
                             jsonEncode(payload));
    analysisCache().scheduleRefresh(eventKey, teamNum);
    ctx.status(HttpStatus.NO_CONTENT);
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
    String eventKey = teamDB().getTeam(session.getTeam())
                              .eventKey();
    if (eventKey.isEmpty() || !matchKey.startsWith(eventKey)) {
      throw wrongEvent(eventKey, session.getTeam());
    }

    MatchInfo match = matchScheduleCache().get(eventKey)
                                          .value()
                                          .get(matchKey);
    if (match == null) {
      throw matchNotFound(matchKey);
    }

    Alliance.Color alliance;
    if (teamOnAlliance(session.getTeam(), match.getBlue())) {
      alliance = Alliance.Color.BLUE;
    } else if (teamOnAlliance(session.getTeam(), match.getRed())) {
      alliance = Alliance.Color.RED;
    } else {
      throw teamNotInMatch(matchKey, session.getTeam());
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
        throw new BadRequestResponse("Team " + scoutedTeam + " not alliance partners with team "
            + session.getTeam() + " in match " + matchKey);
      }

      if (!matchesQuestions(entry.getValue(), questions(eventKey).getDriveTeamQuestions())) {
        throw schemaMismatch(eventKey);
      }
    }

    for (Map.Entry<String, Map<String, Object>> entry : payload.entrySet()) {
      int teamNum = Integer.parseInt(entry.getKey());
      driveTeamEntryDB().createEntry(eventKey, matchKey, session.getUser(), session.getTeam(),
                                     teamNum, jsonEncode(entry.getValue()));
      analysisCache().scheduleRefresh(eventKey, teamNum);
    }

    ctx.status(HttpStatus.NO_CONTENT);
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
