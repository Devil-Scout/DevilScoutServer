package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.questions.Question;
import org.victorrobotics.devilscoutserver.questions.QuestionPage;
import org.victorrobotics.devilscoutserver.questions.Questions;

import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;
import io.javalin.openapi.OpenApiSecurity;

public final class QuestionController extends Controller {
  private QuestionController() {}

  @OpenApi(path = "/questions/match", methods = HttpMethod.GET, tags = "Questions",
           description = "Get the match scouting questions.",
           headers = @OpenApiParam(name = "If-None-Match", type = String.class, required = false),
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = QuestionPage[].class)),
                         @OpenApiResponse(status = "304"),
                         @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = ApiError.class)) })
  public static void matchQuestions(Context ctx) {
    getValidSession(ctx);
    checkIfNoneMatch(ctx, Questions.MATCH_QUESTIONS_HASH);

    ctx.json(Questions.MATCH_QUESTIONS_JSON);
    setResponseEtag(ctx, Questions.MATCH_QUESTIONS_HASH);
  }

  @OpenApi(path = "/questions/pit", methods = HttpMethod.GET, tags = "Questions",
           description = "Get the pit scouting questions.",
           headers = @OpenApiParam(name = "If-None-Match", type = String.class, required = false),
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = QuestionPage[].class)),
                         @OpenApiResponse(status = "304"),
                         @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = ApiError.class)) })
  public static void pitQuestions(Context ctx) {
    getValidSession(ctx);
    checkIfNoneMatch(ctx, Questions.PIT_QUESTIONS_HASH);

    ctx.json(Questions.PIT_QUESTIONS_JSON);
    setResponseEtag(ctx, Questions.PIT_QUESTIONS_HASH);
  }

  @OpenApi(path = "/questions/drive-team", methods = HttpMethod.GET, tags = "Questions",
           description = "Get the drive team feedback questions.",
           headers = @OpenApiParam(name = "If-None-Match", type = String.class, required = false),
           security = @OpenApiSecurity(name = "Session"),
           responses = { @OpenApiResponse(status = "200",
                                          content = @OpenApiContent(from = Question[].class)),
                         @OpenApiResponse(status = "304"),
                         @OpenApiResponse(status = "401",
                                          content = @OpenApiContent(from = ApiError.class)),
                         @OpenApiResponse(status = "403",
                                          content = @OpenApiContent(from = ApiError.class)) })
  public static void driveTeamQuestions(Context ctx) {
    getValidSession(ctx);
    checkIfNoneMatch(ctx, Questions.DRIVE_TEAM_QUESTIONS_HASH);

    ctx.json(Questions.DRIVE_TEAM_QUESTIONS_JSON);
    setResponseEtag(ctx, Questions.DRIVE_TEAM_QUESTIONS_HASH);
  }
}
