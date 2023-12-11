package org.victorrobotics.devilscoutserver.controller;

import static org.victorrobotics.devilscoutserver.Base64Util.base64Encode;

import org.victorrobotics.devilscoutserver.database.Session;
import org.victorrobotics.devilscoutserver.database.UserAccessLevel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequired;
import io.javalin.openapi.OpenApiResponse;
import io.javalin.openapi.OpenApiSecurity;

public final class QuestionController extends Controller {
  private static final MatchQuestions     MATCH_QUESTIONS;
  private static final PitQuestions       PIT_QUESTIONS;
  private static final DriveTeamQuestions DRIVE_TEAM_QUESTIONS;

  private static final String MATCH_QUESTIONS_JSON;
  private static final String PIT_QUESTIONS_JSON;
  private static final String DRIVE_TEAM_QUESTIONS_JSON;

  private static final String MATCH_QUESTIONS_HASH;
  private static final String PIT_QUESTIONS_HASH;
  private static final String DRIVE_TEAM_QUESTIONS_HASH;

  static {
    try {
      JsonMapper json = new JsonMapper();
      MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

      MATCH_QUESTIONS = json.readValue(openResource("/match_questions.json"), MatchQuestions.class);
      PIT_QUESTIONS = json.readValue(openResource("/pit_questions.json"), PitQuestions.class);
      DRIVE_TEAM_QUESTIONS =
          json.readValue(openResource("/drive_team_questions.json"), DriveTeamQuestions.class);

      MATCH_QUESTIONS_JSON = json.writeValueAsString(MATCH_QUESTIONS);
      PIT_QUESTIONS_JSON = json.writeValueAsString(PIT_QUESTIONS);
      DRIVE_TEAM_QUESTIONS_JSON = json.writeValueAsString(DRIVE_TEAM_QUESTIONS);

      MATCH_QUESTIONS_HASH =
          base64Encode(sha256.digest(MATCH_QUESTIONS_JSON.getBytes(StandardCharsets.UTF_8)));
      PIT_QUESTIONS_HASH =
          base64Encode(sha256.digest(PIT_QUESTIONS_JSON.getBytes(StandardCharsets.UTF_8)));
      DRIVE_TEAM_QUESTIONS_HASH =
          base64Encode(sha256.digest(DRIVE_TEAM_QUESTIONS_JSON.getBytes(StandardCharsets.UTF_8)));
    } catch (IOException | NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  private QuestionController() {}

  private static InputStream openResource(String name) {
    return QuestionController.class.getResourceAsStream(name);
  }

  @OpenApi(path = "/questions/match", methods = HttpMethod.GET, tags = "Questions",
           summary = "USER", description = "Get the match scouting questions users should answer.",
           headers = @OpenApiParam(name = "If-None-Match", type = String.class, required = false),
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = MatchQuestions.class)),
                         @OpenApiResponse(status = "304"),
                         @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = Error.class)) })
  public static void matchQuestions(Context ctx) {
    getValidSession(ctx);
    checkIfNoneMatch(ctx, MATCH_QUESTIONS_HASH);

    ctx.json(MATCH_QUESTIONS_JSON);
    setResponseEtag(ctx, MATCH_QUESTIONS_HASH);
  }

  @OpenApi(path = "/questions/pit", methods = HttpMethod.GET, tags = "Questions", summary = "USER",
           description = "Get the pit scouting questions users should answer.",
           headers = @OpenApiParam(name = "If-None-Match", type = String.class, required = false),
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = PitQuestions.class)),
                         @OpenApiResponse(status = "304"),
                         @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = Error.class)) })
  public static void pitQuestions(Context ctx) {
    getValidSession(ctx);
    checkIfNoneMatch(ctx, PIT_QUESTIONS_HASH);

    ctx.json(PIT_QUESTIONS_JSON);
    setResponseEtag(ctx, PIT_QUESTIONS_HASH);
  }

  @OpenApi(path = "/questions/drive-team", methods = HttpMethod.GET, tags = "Questions",
           summary = "ADMIN",
           description = "Get the scouting questions drive teams should answer. Requires ADMIN access.",
           headers = @OpenApiParam(name = "If-None-Match", type = String.class, required = false),
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = DriveTeamQuestions.class)),
                         @OpenApiResponse(status = "304"),
                         @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = Error.class)),
                         @OpenApiResponse(status = "403",
                                          content = @OpenApiContent(from = Error.class)) })
  public static void driveTeamQuestions(Context ctx) throws SQLException {
    Session session = getValidSession(ctx);
    userDB().getAccessLevel(session.getUser())
            .verifyAccess(UserAccessLevel.ADMIN);
    checkIfNoneMatch(ctx, DRIVE_TEAM_QUESTIONS_HASH);

    ctx.json(DRIVE_TEAM_QUESTIONS_JSON);
    setResponseEtag(ctx, DRIVE_TEAM_QUESTIONS_HASH);
  }

  enum QuestionType {
    BOOLEAN,
    COUNTER,
    GRID,
    MULTIPLE,
    NUMBER,
    RANGE,
    SEQUENCE,
    SINGLE;
  }

  public static record Question(@OpenApiRequired @OpenApiExample("Drivetrain Type") String prompt,
                                @OpenApiRequired QuestionType type,
                                @OpenApiExample("{}")
                                @JsonInclude(Include.NON_NULL) Map<String, Object> config) {}

  public static record MatchQuestions(@OpenApiRequired List<Question> auto,
                                      @OpenApiRequired @OpenApiExample("[]") List<Question> teleop,
                                      @OpenApiRequired @OpenApiExample("[]") List<Question> endgame,
                                      @OpenApiRequired @OpenApiExample("[]") List<Question> general,
                                      @OpenApiRequired
                                      @OpenApiExample("[]") List<Question> human) {}

  public static record PitQuestions(@OpenApiRequired List<Question> specs,
                                    @OpenApiRequired @OpenApiExample("[]") List<Question> auto,
                                    @OpenApiRequired @OpenApiExample("[]") List<Question> teleop,
                                    @OpenApiRequired @OpenApiExample("[]") List<Question> endgame,
                                    @OpenApiRequired
                                    @OpenApiExample("[]") List<Question> general) {}

  public static record DriveTeamQuestions(@OpenApiRequired List<Question> questions) {}
}
