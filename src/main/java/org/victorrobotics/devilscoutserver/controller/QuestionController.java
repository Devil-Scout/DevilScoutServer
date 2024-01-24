package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.questions.Question;
import org.victorrobotics.devilscoutserver.questions.QuestionPage;
import org.victorrobotics.devilscoutserver.questions.Questions;

import io.javalin.http.Context;

public final class QuestionController extends Controller {
  private QuestionController() {}

  /**
   * GET /questions/match
   * <p>
   * Success: 200 {@link QuestionPage}[]
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
    checkIfNoneMatch(ctx, Questions.MATCH_QUESTIONS_HASH);

    ctx.json(Questions.MATCH_QUESTIONS_JSON);
    setResponseEtag(ctx, Questions.MATCH_QUESTIONS_HASH);
  }

  /**
   * GET /questions/pit
   * <p>
   * Success: 200 {@link QuestionPage}[]
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
    checkIfNoneMatch(ctx, Questions.PIT_QUESTIONS_HASH);

    ctx.json(Questions.PIT_QUESTIONS_JSON);
    setResponseEtag(ctx, Questions.PIT_QUESTIONS_HASH);
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
    checkIfNoneMatch(ctx, Questions.DRIVE_TEAM_QUESTIONS_HASH);

    ctx.json(Questions.DRIVE_TEAM_QUESTIONS_JSON);
    setResponseEtag(ctx, Questions.DRIVE_TEAM_QUESTIONS_HASH);
  }
}
