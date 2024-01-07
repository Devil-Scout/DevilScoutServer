package org.victorrobotics.devilscoutserver.controller;

import static org.victorrobotics.devilscoutserver.EncodingUtil.jsonEncode;

import org.victorrobotics.devilscoutserver.controller.QuestionController.QuestionPage;
import org.victorrobotics.devilscoutserver.database.Team;
import org.victorrobotics.devilscoutserver.questions.Question;
import org.victorrobotics.devilscoutserver.questions.Questions;

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

  @OpenApi(path = "/submissions/match", methods = HttpMethod.POST, tags = "Uploads",
           summary = "USER",
           description = "Submit match data to the pool. "
               + "The current user's team must be attending the corresponding event.",
           requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = MatchSubmission.class)),
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "204"),
                         @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = Error.class)) })
  public static void submitMatch(Context ctx) throws SQLException {
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

    if (!matchScheduleCache().get(payload.event())
                             .value()
                             .containsMatch(payload.match())) {
      throw new BadRequestResponse("Match not found at event");
    }

    if (!matchesSchema(payload.data(), Questions.MATCH_QUESTIONS)) {
      throw new BadRequestResponse("Invalid/expired submission format");
    }

    matchEntryDB().createEntry(payload.event(), payload.match(), session.getUser(),
                               session.getTeam(), payload.team(), jsonEncode(payload.data()));
    throw new NoContentResponse();
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

  static record MatchSubmission(@OpenApiRequired @OpenApiExample("2023nyrr") String event,
                                @OpenApiRequired @OpenApiExample("2023nyrr_qm1") String match,
                                @OpenApiRequired @OpenApiExample("1559") int team,
                                @OpenApiRequired
                                @OpenApiExample("{}") Map<String, Map<String, Object>> data) {}

  static record PitSubmission(@OpenApiRequired @OpenApiExample("2023nyrr") String event,
                              @OpenApiRequired @OpenApiExample("1559") int team,
                              @OpenApiRequired
                              @OpenApiExample("{}") Map<String, Map<String, Object>> data) {}

  static record DriveTeamSubmission(@OpenApiRequired @OpenApiExample("2023nyrr") String event,
                                    @OpenApiRequired @OpenApiExample("2023nyrr_qm1") String match,
                                    @OpenApiRequired @OpenApiExample("5740") int partner1,
                                    @OpenApiRequired
                                    @OpenApiExample("{}") Map<String, Object> data1,
                                    @OpenApiRequired @OpenApiExample("9996") int partner2,
                                    @OpenApiRequired
                                    @OpenApiExample("{}") Map<String, Object> data2) {}
}
