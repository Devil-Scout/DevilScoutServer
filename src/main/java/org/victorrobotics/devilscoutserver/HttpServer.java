package org.victorrobotics.devilscoutserver;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.patch;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

import org.victorrobotics.devilscoutserver.controller.AnalysisController;
import org.victorrobotics.devilscoutserver.controller.Controller;
import org.victorrobotics.devilscoutserver.controller.EventController;
import org.victorrobotics.devilscoutserver.controller.QuestionController;
import org.victorrobotics.devilscoutserver.controller.SessionController;
import org.victorrobotics.devilscoutserver.controller.SubmissionController;
import org.victorrobotics.devilscoutserver.controller.TeamController;
import org.victorrobotics.devilscoutserver.controller.UserController;

import io.javalin.Javalin;
import io.javalin.community.ssl.SslPlugin;
import io.javalin.http.HttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServer {
  private static final Logger LOGGER = LoggerFactory.getLogger(HttpServer.class);

  private final Javalin javalin;

  public HttpServer() {
    javalin = Javalin.create(config -> {
      config.http.prefer405over404 = true;
      config.useVirtualThreads = true;

      config.bundledPlugins.enableSslRedirects();
      config.registerPlugin(new SslPlugin(sslConfig -> {
        sslConfig.pemFromPath(System.getenv("SSL_CERT_PATH"), System.getenv("SSL_KEY_PATH"));
        sslConfig.sniHostCheck = false;
      }));

      config.router.apiBuilder(() -> {
        post("login", SessionController::login);
        post("auth", SessionController::auth);
        delete("logout", SessionController::logout);
        get("session", SessionController::getSession);

        path("teams/{teamNum}", () -> {
          get(TeamController::getTeam);
          patch(TeamController::editTeam);

          path("users", () -> {
            get(UserController::usersOnTeam);
            post(UserController::registerUser);
          });
        });

        path("users/{userId}", () -> {
          get(UserController::getUser);
          delete(UserController::deleteUser);
          patch(UserController::editUser);
        });

        path("events", () -> {
          get(EventController::getAllEvents);

          path("{eventKey}", () -> {
            get(EventController::getEvent);
            get("teams", EventController::getTeams);
            get("matches", EventController::getMatchSchedule);
          });
        });

        path("questions/{eventKey}", () -> {
          get("match", QuestionController::matchQuestions);
          get("pit", QuestionController::pitQuestions);
          get("drive-team", QuestionController::driveTeamQuestions);
        });

        path("submissions", () -> {
          post("match/{matchKey}/{teamNum}", SubmissionController::submitMatch);
          post("pit/{eventKey}/{teamNum}", SubmissionController::submitPit);
          post("drive-team/{matchKey}", SubmissionController::submitDriveTeam);
        });

        get("analysis/{eventKey}/teams", AnalysisController::teams);
      });
    });

    javalin.before(ctx -> LOGGER.info("Request {} {}", ctx.method(), ctx.path()));
    javalin.exception(HttpResponseException.class, (e, ctx) -> {
      int status = e.getStatus();
      ctx.status(status);

      if (status >= 500) {
        ctx.json(new Controller.ApiError(e.getMessage()));
        LOGGER.warn("Server error {} {} {}", ctx.method(), ctx.path(), ctx.status(), e);
      } else if (status >= 400) {
        ctx.json(new Controller.ApiError(e.getMessage()));
        LOGGER.warn("Client error {} {} {}", ctx.method(), ctx.path(), ctx.status());
      }
    });
    javalin.exception(Exception.class, (e, ctx) -> {
      ctx.status(500);
      ctx.json(new Controller.ApiError(e.getMessage()));
      LOGGER.error("Server exception {} {} {}", ctx.method(), ctx.path(), ctx.status(), e);
    });
  }

  public void start() {
    javalin.start();
  }

  public void stop() {
    javalin.stop();
  }

  public Javalin getInternal() {
    return javalin;
  }
}
