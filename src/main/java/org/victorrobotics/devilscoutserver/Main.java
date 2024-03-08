package org.victorrobotics.devilscoutserver;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.devilscoutserver.analysis.AnalysisCache;
import org.victorrobotics.devilscoutserver.analysis.Analyzer;
import org.victorrobotics.devilscoutserver.controller.Controller;
import org.victorrobotics.devilscoutserver.database.Database;
import org.victorrobotics.devilscoutserver.database.EntryDatabase;
import org.victorrobotics.devilscoutserver.database.TeamDatabase;
import org.victorrobotics.devilscoutserver.database.UserDatabase;
import org.victorrobotics.devilscoutserver.questions.Questions;
import org.victorrobotics.devilscoutserver.session.SessionManager;
import org.victorrobotics.devilscoutserver.tba.EventCache;
import org.victorrobotics.devilscoutserver.tba.MatchScheduleCache;
import org.victorrobotics.devilscoutserver.tba.OprsCache;
import org.victorrobotics.devilscoutserver.tba.RankingsCache;
import org.victorrobotics.devilscoutserver.tba.TeamListCache;
import org.victorrobotics.devilscoutserver.years._2024.CrescendoAnalyzer;
import org.victorrobotics.devilscoutserver.years._2024.CrescendoQuestions;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
  private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

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
    Controller.setSessions(new SessionManager());
    Controller.setEventCache(new EventCache());

    // Hack to pass circular references on final members
    Map<Integer, Analyzer<?, ?>> analyzers = new HashMap<>();
    AnalysisCache analysisCache = new AnalysisCache(analyzers);
    Controller.setAnalysisCache(analysisCache);

    OprsCache oprsCache = new OprsCache(analysisCache);
    RankingsCache rankingsCache = new RankingsCache(analysisCache);
    Controller.setMatchScheduleCache(new MatchScheduleCache(oprsCache, rankingsCache,
                                                            analysisCache));
    Controller.setTeamListCache(new TeamListCache(oprsCache, rankingsCache, analysisCache));
    analyzers.put(2024,
                  new CrescendoAnalyzer(Controller.matchEntryDB(), Controller.pitEntryDB(),
                                        Controller.driveTeamEntryDB(),
                                        Controller.matchScheduleCache(), oprsCache, rankingsCache));

    LOGGER.info("Caches ready");

    LOGGER.info("Loading questions...");
    Map<Integer, Questions> questions = new HashMap<>();
    questions.put(2024, new CrescendoQuestions());
    Controller.setQuestions(questions);
    LOGGER.info("Questions loaded");

    LOGGER.info("Starting refresh services...");
    ThreadFactory blueAllianceThreads = Thread.ofVirtual()
                                              .name("BlueAlliance-", 0)
                                              .factory();
    Endpoint.setExecutor(Executors.newFixedThreadPool(16, blueAllianceThreads));

    ThreadFactory refreshThreads = Thread.ofPlatform()
                                         .name("Refresh-", 0)
                                         .factory();
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, refreshThreads);

    Thread.ofPlatform()
          .name("Analysis")
          .start(analysisCache::refreshLoop);
    executor.scheduleAtFixedRate(() -> {
      Controller.eventsCache()
                .refresh();
    }, 0, 60, TimeUnit.MINUTES);
    executor.scheduleAtFixedRate(() -> {
      Controller.teamListsCache()
                .refreshAll(getActiveEvents());
    }, 0, 60, TimeUnit.MINUTES);
    executor.scheduleAtFixedRate(() -> {
      Controller.matchScheduleCache()
                .refreshAll(getActiveEvents());
    }, 0, 1, TimeUnit.MINUTES);
    executor.scheduleAtFixedRate(() -> {
      Controller.sessions()
                .purgeExpired();
    }, 0, 5, TimeUnit.MINUTES);
    LOGGER.info("Refresh services running");

    LOGGER.info("Starting HTTP server...");
    HttpServer server = new HttpServer();
    executor.scheduleAtFixedRate(() -> {
      server.getInternal()
            .jettyServer()
            .server()
            .dump();
    }, 0, 1, TimeUnit.MINUTES);
    server.start();
    LOGGER.info("Listening for client connections");

    LOGGER.info("Startup complete, main thread exiting");
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
