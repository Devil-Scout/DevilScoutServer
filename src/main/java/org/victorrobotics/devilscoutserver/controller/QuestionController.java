package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.questions.Question;
import org.victorrobotics.devilscoutserver.questions.Questions;

import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;

public final class QuestionController extends Controller {
  private static final String EVENT_KEY_PATH_PARAM = "eventKey";

  private QuestionController() {}

  /**
   * GET /questions/{eventKey}/match
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

    String eventKey = ctx.pathParam(EVENT_KEY_PATH_PARAM);
    if (!eventsCache().containsKey(eventKey)) {
      throw new NotFoundResponse();
    }

    Questions questions = questions(eventKey);
    checkIfNoneMatch(ctx, questions.getMatchQuestionsHash());

    ctx.json(questions.getMatchQuestionsJson());
    setResponseEtag(ctx, questions.getMatchQuestionsHash());
  }

  /**
   * GET /questions/{eventKey}/pit
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

    String eventKey = ctx.pathParam(EVENT_KEY_PATH_PARAM);
    if (!eventsCache().containsKey(eventKey)) {
      throw new NotFoundResponse();
    }

    Questions questions = questions(eventKey);
    checkIfNoneMatch(ctx, questions.getPitQuestionsHash());

    ctx.json(questions.getPitQuestionsJson());
    setResponseEtag(ctx, questions.getPitQuestionsHash());
  }

  /**
   * GET /questions/{eventKey}/drive-team
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

    String eventKey = ctx.pathParam(EVENT_KEY_PATH_PARAM);
    if (!eventsCache().containsKey(eventKey)) {
      throw new NotFoundResponse();
    }

    Questions questions = questions(eventKey);
    checkIfNoneMatch(ctx, questions.getDriveTeamQuestionsHash());

    ctx.json(questions.getDriveTeamQuestionsJson());
    setResponseEtag(ctx, questions.getDriveTeamQuestionsHash());
  }
}
