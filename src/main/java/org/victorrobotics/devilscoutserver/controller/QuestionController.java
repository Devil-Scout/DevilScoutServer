package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.questions.Question;

import io.javalin.http.Context;

public final class QuestionController extends Controller {
  private QuestionController() {}

  /**
   * GET /questions/match
   * <p>
   * Success: 200 {@link Question.Page}[]
   * <p>
   * Cached: 304 NotModified ({@code If-None-Match})
   * <p>
   * Errors:
   * <ul>
   * <li>401 Unauthorized</li>
   * </ul>
   */
  public static void matchQuestions(Context ctx) {
    getValidSession(ctx);
    checkIfNoneMatch(ctx, questions().getMatchQuestionsHash());

    ctx.json(questions().getMatchQuestionsJson());
    setResponseEtag(ctx, questions().getMatchQuestionsHash());
  }

  /**
   * GET /questions/pit
   * <p>
   * Success: 200 {@link Question.Page}[]
   * <p>
   * Cached: 304 NotModified ({@code If-None-Match})
   * <p>
   * Errors:
   * <ul>
   * <li>401 Unauthorized</li>
   * </ul>
   */
  public static void pitQuestions(Context ctx) {
    getValidSession(ctx);
    checkIfNoneMatch(ctx, questions().getPitQuestionsHash());

    ctx.json(questions().getPitQuestionsJson());
    setResponseEtag(ctx, questions().getPitQuestionsHash());
  }

  /**
   * GET /questions/drive-team
   * <p>
   * Success: 200 {@link Question}[]
   * <p>
   * Cached: 304 NotModified ({@code If-None-Match})
   * <p>
   * Errors:
   * <ul>
   * <li>401 Unauthorized</li>
   * </ul>
   */
  public static void driveTeamQuestions(Context ctx) {
    getValidSession(ctx);
    checkIfNoneMatch(ctx, questions().getDriveTeamQuestionsHash());

    ctx.json(questions().getDriveTeamQuestionsJson());
    setResponseEtag(ctx, questions().getDriveTeamQuestionsHash());
  }
}
