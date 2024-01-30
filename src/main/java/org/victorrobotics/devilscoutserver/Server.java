package org.victorrobotics.devilscoutserver;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.patch;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.devilscoutserver.analysis.Analyzer;
import org.victorrobotics.devilscoutserver.analysis.CrescendoAnalyzer;
import org.victorrobotics.devilscoutserver.analysis.TeamStatisticsCache;
import org.victorrobotics.devilscoutserver.cache.Cache;
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
import org.victorrobotics.devilscoutserver.tba.EventInfoCache;
import org.victorrobotics.devilscoutserver.tba.EventOprsCache;
import org.victorrobotics.devilscoutserver.tba.EventTeamListCache;
import org.victorrobotics.devilscoutserver.tba.MatchScheduleCache;
import org.victorrobotics.devilscoutserver.tba.TeamInfoCache;
import org.victorrobotics.devilscoutserver.tba.TeamOprsCache;

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

      config.bundledPlugins.enableSslRedirects();
      config.registerPlugin(new SslPlugin(sslConfig -> {
        sslConfig.pemFromPath(System.getenv("SSL_CERT_PATH"), System.getenv("SSL_KEY_PATH"));
        sslConfig.redirect = true;
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

  @SuppressWarnings("java:S2095") // close the executor
  public static void main(String... args) {
    LOGGER.info("Connecting to database...");
    Database.initConnectionPool();
    Controller.setUserDB(new UserDatabase());
    Controller.setTeamDB(new TeamDatabase());
    Controller.setMatchEntryDB(new EntryDatabase("match_entries", true));
    Controller.setPitEntryDB(new EntryDatabase("pit_entries", false));
    Controller.setDriveTeamEntryDB(new EntryDatabase("drive_team_entries", true));
    LOGGER.info("Database connected");

    LOGGER.info("Initializing memory caches...");
    Controller.setEventInfoCache(new EventInfoCache());
    Controller.setTeamCache(new TeamInfoCache());
    Controller.setEventTeamsCache(new EventTeamListCache(Controller.teamCache()));
    Controller.setMatchScheduleCache(new MatchScheduleCache());
    LOGGER.info("Memory caches ready");

    LOGGER.info("Initializing analysis...");
    EventOprsCache eventOprsCache = new EventOprsCache();
    TeamOprsCache teamOprsCache = new TeamOprsCache(eventOprsCache);
    Analyzer analyzer = new CrescendoAnalyzer(Controller.matchEntryDB(), Controller.pitEntryDB(),
                                              Controller.driveTeamEntryDB(), teamOprsCache);
    Controller.setTeamStatisticsCache(new TeamStatisticsCache(analyzer));
    LOGGER.info("Analysis ready");

    LOGGER.info("Starting daemon services...");
    ThreadFactory blueAllianceThreads = Thread.ofVirtual()
                                              .name("BlueAlliance-", 0)
                                              .factory();
    Endpoint.setExecutor(Executors.newFixedThreadPool(16, blueAllianceThreads));

    ThreadFactory refreshThreads = Thread.ofPlatform()
                                         .name("Refresh-", 0)
                                         .factory();
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, refreshThreads);
    executor.scheduleAtFixedRate(() -> refreshCache(Controller.eventInfoCache()), 0, 5,
                                 TimeUnit.MINUTES);
    executor.scheduleAtFixedRate(() -> refreshCache(Controller.matchScheduleCache()), 0, 1,
                                 TimeUnit.MINUTES);
    executor.scheduleAtFixedRate(() -> {
      refreshCache(Controller.teamCache());
      refreshCache(Controller.eventTeamsCache());
    }, 0, 5, TimeUnit.MINUTES);
    executor.scheduleAtFixedRate(() -> {
      ConcurrentMap<String, Session> sessions = Controller.sessions();
      long start = System.currentTimeMillis();
      int size = sessions.size();
      sessions.values()
              .removeIf(Session::isExpired);
      LOGGER.info("Purged {} expired sessions in {}ms", size - sessions.size(),
                  System.currentTimeMillis() - start);
    }, 0, 5, TimeUnit.MINUTES);
    executor.scheduleAtFixedRate(() -> {
      refreshCache(eventOprsCache);
      refreshCache(teamOprsCache);
      refreshCache(Controller.teamAnalysisCache());
    }, 0, 15, TimeUnit.MINUTES);
    LOGGER.info("Daemon services running");

    LOGGER.info("Starting HTTP server...");
    Server server = new Server();
    executor.scheduleAtFixedRate(() -> {
      server.javalin.jettyServer()
                    .server()
                    .dump();
    }, 0, 1, TimeUnit.MINUTES);
    server.start();
    LOGGER.info("HTTP server started");

    LOGGER.info("DevilScoutServer startup complete, main thread exiting");
  }

  private static void refreshCache(Cache<?, ?, ?> cache) {
    long start = System.currentTimeMillis();
    cache.refresh();
    LOGGER.info("Refreshed {} ({}) in {}ms", cache.getClass()
                                                  .getSimpleName(),
                cache.size(), System.currentTimeMillis() - start);
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

    path("analysis", () -> {
      get("teams", AnalysisController::teams);
      // post("simulation", null); // request match simulation
      // post("optimization", null); // request alliance optimization
    });
  }
}
