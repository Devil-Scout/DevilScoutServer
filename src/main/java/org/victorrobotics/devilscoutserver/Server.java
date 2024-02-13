package org.victorrobotics.devilscoutserver;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.patch;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.devilscoutserver.analysis.Analyzer;
import org.victorrobotics.devilscoutserver.controller.AnalysisController;
import org.victorrobotics.devilscoutserver.controller.Controller;
import org.victorrobotics.devilscoutserver.controller.Controller.Session;
import org.victorrobotics.devilscoutserver.controller.EventController;
import org.victorrobotics.devilscoutserver.controller.QuestionController;
import org.victorrobotics.devilscoutserver.controller.SessionController;
import org.victorrobotics.devilscoutserver.controller.SubmissionController;
import org.victorrobotics.devilscoutserver.controller.TeamController;
import org.victorrobotics.devilscoutserver.controller.UserController;
import org.victorrobotics.devilscoutserver.database.Database;
import org.victorrobotics.devilscoutserver.database.EntryDatabase;
import org.victorrobotics.devilscoutserver.database.TeamDatabase;
import org.victorrobotics.devilscoutserver.database.UserDatabase;
import org.victorrobotics.devilscoutserver.questions.Questions;
import org.victorrobotics.devilscoutserver.tba.EventCache;
import org.victorrobotics.devilscoutserver.tba.MatchScheduleCache;
import org.victorrobotics.devilscoutserver.tba.TeamListCache;
import org.victorrobotics.devilscoutserver.years._2024.CrescendoAnalyzer;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import io.javalin.Javalin;
import io.javalin.community.ssl.SslPlugin;
import io.javalin.http.HttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {
  private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

  private final Javalin javalin;

  public Server() {
    javalin = Javalin.create(config -> {
      config.http.prefer405over404 = true;
      config.useVirtualThreads = true;

      config.bundledPlugins.enableSslRedirects();
      config.registerPlugin(new SslPlugin(sslConfig -> {
        sslConfig.pemFromPath(System.getenv("SSL_CERT_PATH"), System.getenv("SSL_KEY_PATH"));
        sslConfig.sniHostCheck = false;
      }));

      config.router.apiBuilder(Server::endpoints);
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

  // close the executor, single-line lambda bodies
  @SuppressWarnings({ "java:S2095", "java:S1602" })
  public static void main(String... args) {
    LOGGER.info("Connecting to database...");
    Database.initConnectionPool();
    Controller.setUserDB(new UserDatabase());
    Controller.setTeamDB(new TeamDatabase());
    Controller.setMatchEntryDB(new EntryDatabase("match_entries", true));
    Controller.setPitEntryDB(new EntryDatabase("pit_entries", false));
    Controller.setDriveTeamEntryDB(new EntryDatabase("drive_team_entries", true));
    LOGGER.info("Database connected");

    LOGGER.info("Initializing caches...");
    Controller.setEventInfoCache(new EventCache());
    Controller.setEventTeamsCache(new TeamListCache());
    Controller.setMatchScheduleCache(new MatchScheduleCache());
    LOGGER.info("Caches ready");

    LOGGER.info("Loading questions from disk...");
    Controller.setQuestions(new Questions());
    LOGGER.info("Questions loaded");

    LOGGER.info("Initializing analysis...");
    registerAnalyzers();
    LOGGER.info("Analysis ready");

    LOGGER.info("Starting refresh services...");
    ThreadFactory blueAllianceThreads = Thread.ofVirtual()
                                              .name("BlueAlliance-", 0)
                                              .factory();
    Endpoint.setExecutor(Executors.newFixedThreadPool(16, blueAllianceThreads));

    ThreadFactory refreshThreads = Thread.ofPlatform()
                                         .name("Refresh-", 0)
                                         .factory();
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, refreshThreads);

    // TODO: implement refreshers
    // refresh list of all events (all supported years) (every hour) DONE
    // get list of active events (from TeamDB)
    // for each active event:
    // - refresh match schedule - every minute
    // -

    executor.scheduleAtFixedRate(() -> {
      try {
        long start = System.currentTimeMillis();
        EventCache cache = Controller.eventInfoCache();
        cache.refresh();
        LOGGER.info("Refreshed event list ({}) in {}ms", cache.size(),
                    System.currentTimeMillis() - start);
      } catch (Exception e) {
        LOGGER.warn("Exception throw while refreshing event list:", e);
      }
    }, 0, 60, TimeUnit.MINUTES);
    executor.scheduleAtFixedRate(() -> {
      try {
        long start = System.currentTimeMillis();
        MatchScheduleCache cache = Controller.matchScheduleCache();
        cache.refreshAll(getActiveEvents());
        LOGGER.info("Refreshed match schedules for ({}) active events in {}ms", cache.size(),
                    System.currentTimeMillis() - start);
      } catch (Exception e) {
        LOGGER.warn("Exception throw while refreshing match schedules:", e);
      }
    }, 0, 1, TimeUnit.MINUTES);
    executor.scheduleAtFixedRate(() -> {
      ConcurrentMap<String, Session> sessions = Controller.sessions();
      long start = System.currentTimeMillis();
      int size = sessions.size();
      sessions.values()
              .removeIf(Session::isExpired);
      LOGGER.info("Purged {} expired sessions in {}ms", size - sessions.size(),
                  System.currentTimeMillis() - start);
    }, 0, 5, TimeUnit.MINUTES);
    LOGGER.info("Refresh services running");

    Server server = new Server();
    executor.scheduleAtFixedRate(() -> {
      server.javalin.jettyServer()
                    .server()
                    .dump();
    }, 0, 1, TimeUnit.MINUTES);
    server.start();

    LOGGER.info("DevilScoutServer startup complete, main thread exiting");
  }

  private static void registerAnalyzers() {
    Map<Integer, Analyzer<?>> analyzers = new HashMap<>();
    // TODO: TeamOPRs
    analyzers.put(2024,
                  new CrescendoAnalyzer(Controller.matchEntryDB(), Controller.pitEntryDB(),
                                        Controller.driveTeamEntryDB(),
                                        Controller.matchScheduleCache(), null));
    // TODO: TESTING ONLY
    analyzers.put(2023, analyzers.get(2024));
  }

  private static void endpoints() {
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

    path("questions", () -> {
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
  }

  private static Set<String> getActiveEvents() {
    try {
      return Controller.teamDB()
                       .getActiveEvents();
    } catch (SQLException e) {
      LOGGER.warn("");
      return Set.of();
    }
  }
}
