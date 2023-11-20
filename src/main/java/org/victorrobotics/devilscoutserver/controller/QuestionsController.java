package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.data.DriveTeamQuestions;
import org.victorrobotics.devilscoutserver.data.MatchQuestions;
import org.victorrobotics.devilscoutserver.data.PitQuestions;
import org.victorrobotics.devilscoutserver.data.UserAccessLevel;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

public final class QuestionsController extends Controller {
  private static final MatchQuestions     MATCH_QUESTIONS;
  private static final PitQuestions       PIT_QUESTIONS;
  private static final DriveTeamQuestions DRIVE_TEAM_QUESTIONS;

  private static final String MATCH_QUESTIONS_JSON;
  private static final String PIT_QUESTIONS_JSON;
  private static final String DRIVE_TEAM_QUESTIONS_JSON;

  static {
    JsonMapper json = new JsonMapper();
    try {
      MATCH_QUESTIONS = json.readValue(openResource("/match_questions.json"), MatchQuestions.class);
      PIT_QUESTIONS = json.readValue(openResource("/pit_questions.json"), PitQuestions.class);
      DRIVE_TEAM_QUESTIONS =
          json.readValue(openResource("/drive_team_questions.json"), DriveTeamQuestions.class);

      MATCH_QUESTIONS_JSON = json.writeValueAsString(MATCH_QUESTIONS);
      PIT_QUESTIONS_JSON = json.writeValueAsString(PIT_QUESTIONS);
      DRIVE_TEAM_QUESTIONS_JSON = json.writeValueAsString(DRIVE_TEAM_QUESTIONS);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private QuestionsController() {}

  private static InputStream openResource(String name) {
    return QuestionsController.class.getResourceAsStream(name);
  }

  @OpenApi(path = "/questions/match", methods = HttpMethod.GET, tags = "Configuration",
           description = "Get the match scouting questions users should answer.",
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = MatchQuestions.class)),
                         @OpenApiResponse(status = "401") })
  public static void matchQuestions(Context ctx) {
    getValidSession(ctx);
    ctx.json(MATCH_QUESTIONS_JSON);
  }

  @OpenApi(path = "/questions/pit", methods = HttpMethod.GET, tags = "Configuration",
           description = "Get the pit scouting questions users should answer.",
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = MatchQuestions.class)),
                         @OpenApiResponse(status = "401") })
  public static void pitQuestions(Context ctx) {
    getValidSession(ctx);
    ctx.json(PIT_QUESTIONS_JSON);
  }

  @OpenApi(path = "/questions/drive_team", methods = HttpMethod.GET, tags = "Configuration",
           description = "Get the scouting questions drive teams should answer. Requires ADMIN access.",
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = MatchQuestions.class)),
                         @OpenApiResponse(status = "401"), @OpenApiResponse(status = "403") })
  public static void driveTeamQuestions(Context ctx) {
    getValidSession(ctx, UserAccessLevel.ADMIN);
    ctx.json(DRIVE_TEAM_QUESTIONS_JSON);
  }
}
