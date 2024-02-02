package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.analysis.TeamStatisticsCache;
import org.victorrobotics.devilscoutserver.database.EntryDatabase;
import org.victorrobotics.devilscoutserver.database.TeamDatabase;
import org.victorrobotics.devilscoutserver.database.UserDatabase;
import org.victorrobotics.devilscoutserver.questions.Questions;
import org.victorrobotics.devilscoutserver.tba.EventInfoCache;
import org.victorrobotics.devilscoutserver.tba.EventOprsCache;
import org.victorrobotics.devilscoutserver.tba.EventTeamListCache;
import org.victorrobotics.devilscoutserver.tba.MatchScheduleCache;
import org.victorrobotics.devilscoutserver.tba.TeamOprsCache;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.NotModifiedResponse;
import io.javalin.http.UnauthorizedResponse;

public sealed class Controller
    permits EventController, QuestionController, SessionController, SubmissionController,
    TeamController, UserController, AnalysisController {
  public static final String SESSION_HEADER = "X-DS-SESSION-KEY";

  private static final ConcurrentMap<String, Session> SESSIONS = new ConcurrentHashMap<>();

  protected static final String HASH_ALGORITHM   = "SHA-256";
  protected static final String MAC_ALGORITHM    = "HmacSHA256";
  protected static final String KEYGEN_ALGORITHM = "PBKDF2WithHmacSHA256";

  protected static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private static UserDatabase USERS;
  private static TeamDatabase TEAMS;

  private static EventInfoCache     EVENT_INFO_CACHE;
  private static EventTeamListCache EVENT_TEAMS_CACHE;
  private static MatchScheduleCache MATCH_SCHEDULE_CACHE;

  private static EntryDatabase MATCH_ENTRIES;
  private static EntryDatabase PIT_ENTRIES;
  private static EntryDatabase DRIVE_TEAM_ENTRIES;

  private static TeamStatisticsCache TEAM_ANALYSIS_CACHE;
  private static EventOprsCache      EVENT_OPRS_CACHE;
  private static TeamOprsCache       TEAM_OPRS_CACHE;

  private static Questions QUESTIONS;

  protected Controller() {}

  public static void setUserDB(UserDatabase users) {
    USERS = users;
  }

  public static void setTeamDB(TeamDatabase teams) {
    TEAMS = teams;
  }

  public static void setEventInfoCache(EventInfoCache cache) {
    EVENT_INFO_CACHE = cache;
  }

  public static void setEventTeamsCache(EventTeamListCache cache) {
    EVENT_TEAMS_CACHE = cache;
  }

  public static void setMatchScheduleCache(MatchScheduleCache cache) {
    MATCH_SCHEDULE_CACHE = cache;
  }

  public static void setMatchEntryDB(EntryDatabase matchEntries) {
    MATCH_ENTRIES = matchEntries;
  }

  public static void setPitEntryDB(EntryDatabase pitEntries) {
    PIT_ENTRIES = pitEntries;
  }

  public static void setDriveTeamEntryDB(EntryDatabase driveTeamEntries) {
    DRIVE_TEAM_ENTRIES = driveTeamEntries;
  }

  public static void setTeamStatisticsCache(TeamStatisticsCache teamAnalysisCache) {
    TEAM_ANALYSIS_CACHE = teamAnalysisCache;
  }

  public static void setEventOprsCache(EventOprsCache eventOprsCache) {
    EVENT_OPRS_CACHE = eventOprsCache;
  }

  public static void setTeamStatisticsCache(TeamOprsCache teamOprsCache) {
    TEAM_OPRS_CACHE = teamOprsCache;
  }

  public static void setQuestions(Questions questions) {
    QUESTIONS = questions;
  }

  @SuppressWarnings("java:S2384") // copy map
  public static ConcurrentMap<String, Session> sessions() {
    return SESSIONS;
  }

  public static UserDatabase userDB() {
    return USERS;
  }

  public static TeamDatabase teamDB() {
    return TEAMS;
  }

  public static EventInfoCache eventInfoCache() {
    return EVENT_INFO_CACHE;
  }

  public static EventTeamListCache eventTeamsCache() {
    return EVENT_TEAMS_CACHE;
  }

  public static MatchScheduleCache matchScheduleCache() {
    return MATCH_SCHEDULE_CACHE;
  }

  public static EntryDatabase matchEntryDB() {
    return MATCH_ENTRIES;
  }

  public static EntryDatabase pitEntryDB() {
    return PIT_ENTRIES;
  }

  public static EntryDatabase driveTeamEntryDB() {
    return DRIVE_TEAM_ENTRIES;
  }

  public static TeamStatisticsCache teamAnalysisCache() {
    return TEAM_ANALYSIS_CACHE;
  }

  public static EventOprsCache eventOprs() {
    return EVENT_OPRS_CACHE;
  }

  public static TeamOprsCache teamOprs() {
    return TEAM_OPRS_CACHE;
  }

  public static Questions questions() {
    return QUESTIONS;
  }

  @SuppressWarnings({"java:S2221", "unchecked"}) // catch generic exception
  protected static <I, T> T jsonDecode(Context ctx, Class<I> clazz) {
    try {
      return (T) ctx.bodyAsClass(clazz);
    } catch (Exception e) {
      throw new BadRequestResponse("Failed to decode body as " + clazz.getSimpleName());
    }
  }

  protected static Session getValidSession(Context ctx) {
    String sessionKey = ctx.header(SESSION_HEADER);
    if (sessionKey == null) {
      throw new UnauthorizedResponse("Missing " + SESSION_HEADER + " header");
    }

    Session session = SESSIONS.get(sessionKey);
    if (session == null || session.isExpired()) {
      throw new UnauthorizedResponse("Invalid/Expired " + SESSION_HEADER + " header");
    }

    session.refresh();
    return session;
  }

  protected static void checkIfNoneMatch(Context ctx, String latest) {
    String etag = ctx.header("if-none-match");
    if (latest.equals(etag)) {
      setResponseEtag(ctx, latest);
      throw new NotModifiedResponse();
    }
  }

  protected static void checkIfNoneMatch(Context ctx, long timestamp) {
    try {
      long etag = Long.parseLong(ctx.header("if-none-match"));
      if (etag >= timestamp) {
        setResponseEtag(ctx, etag);
        throw new NotModifiedResponse();
      }
    } catch (NumberFormatException e) {}
  }

  protected static void setResponseEtag(Context ctx, String etag) {
    ctx.header("etag", etag);
  }

  protected static void setResponseEtag(Context ctx, long timestamp) {
    ctx.header("etag", Long.toString(timestamp));
  }

  protected static void checkTeamRange(int team) {
    if (team <= 0 || team > 9999) {
      throw new BadRequestResponse("Team [" + team + "] must be in range 1 to 9999");
    }
  }

  public static class Session {
    private static final long DURATION_MILLIS = 8 * 60 * 60 * 1000;

    private final String key;
    private final String user;
    private final int    team;

    private long expiration;

    public Session(String key, String userId, int team) {
      this.key = key;
      this.user = userId;
      this.team = team;

      expiration = System.currentTimeMillis() + DURATION_MILLIS;
    }

    @JsonIgnore
    public boolean isExpired() {
      return System.currentTimeMillis() >= expiration;
    }

    public String getKey() {
      return key;
    }

    public String getUser() {
      return user;
    }

    public int getTeam() {
      return team;
    }

    public void refresh() {
      expiration = System.currentTimeMillis() + DURATION_MILLIS;
    }

    public void verifyAdmin() throws SQLException {
      if (!userDB().isAdmin(getUser())) {
        throw new ForbiddenResponse();
      }
    }
  }

  public static record ApiError(String error) {}
}
