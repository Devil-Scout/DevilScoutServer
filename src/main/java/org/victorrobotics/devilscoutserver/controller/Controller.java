package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.analysis.AnalysisCache;
import org.victorrobotics.devilscoutserver.database.EntryDatabase;
import org.victorrobotics.devilscoutserver.database.TeamDatabase;
import org.victorrobotics.devilscoutserver.database.UserDatabase;
import org.victorrobotics.devilscoutserver.questions.Questions;
import org.victorrobotics.devilscoutserver.tba.EventCache;
import org.victorrobotics.devilscoutserver.tba.MatchScheduleCache;
import org.victorrobotics.devilscoutserver.tba.OprsCache;
import org.victorrobotics.devilscoutserver.tba.TeamListCache;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.ConflictResponse;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.HttpResponseException;
import io.javalin.http.NotFoundResponse;
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

  private static EventCache         EVENT_CACHE;
  private static TeamListCache      TEAM_LIST_CACHE;
  private static MatchScheduleCache MATCH_SCHEDULE_CACHE;
  private static OprsCache          OPRS_CACHE;

  private static Map<Integer, Questions> QUESTIONS;

  private static EntryDatabase MATCH_ENTRIES;
  private static EntryDatabase PIT_ENTRIES;
  private static EntryDatabase DRIVE_TEAM_ENTRIES;

  private static AnalysisCache ANALYSIS_CACHE;

  protected Controller() {}

  public static void setUserDB(UserDatabase users) {
    USERS = users;
  }

  public static void setTeamDB(TeamDatabase teams) {
    TEAMS = teams;
  }

  public static void setEventCache(EventCache cache) {
    EVENT_CACHE = cache;
  }

  public static void setTeamListCache(TeamListCache cache) {
    TEAM_LIST_CACHE = cache;
  }

  public static void setMatchScheduleCache(MatchScheduleCache cache) {
    MATCH_SCHEDULE_CACHE = cache;
  }

  public static void setOprsCache(OprsCache eventOprsCache) {
    OPRS_CACHE = eventOprsCache;
  }

  public static void setQuestions(Map<Integer, Questions> questions) {
    QUESTIONS = questions;
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

  public static void setAnalysisCache(AnalysisCache analysisCache) {
    ANALYSIS_CACHE = analysisCache;
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

  public static EventCache eventsCache() {
    return EVENT_CACHE;
  }

  public static TeamListCache teamListsCache() {
    return TEAM_LIST_CACHE;
  }

  @SuppressWarnings("java:S1452") // wildcard in return type
  public static MatchScheduleCache matchScheduleCache() {
    return MATCH_SCHEDULE_CACHE;
  }

  public static OprsCache oprsCache() {
    return OPRS_CACHE;
  }

  public static Questions questions(String eventKey) {
    int year = Integer.parseInt(eventKey.substring(0, 4));
    return QUESTIONS.get(year);
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

  public static AnalysisCache analysisCache() {
    return ANALYSIS_CACHE;
  }

  @SuppressWarnings({ "java:S2221", "unchecked" }) // catch generic exception
  protected static <I, T> T jsonDecode(Context ctx, Class<I> clazz) {
    try {
      return (T) ctx.bodyAsClass(clazz);
    } catch (Exception e) {
      throw new BadRequestResponse("Invalid request body");
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

  protected static HttpResponseException eventNotFound(String eventKey) {
    return new NotFoundResponse("Event " + eventKey + " not found");
  }

  protected static HttpResponseException wrongEvent(String eventKey, int teamNum) {
    return new ForbiddenResponse("Team " + teamNum + " is attending event " + eventKey);
  }

  protected static HttpResponseException matchNotFound(String matchKey) {
    return new NotFoundResponse("Match " + matchKey + " not found");
  }

  protected static HttpResponseException teamNotInMatch(String matchKey, int teamNum) {
    return new BadRequestResponse("Team " + teamNum + " not in match " + matchKey);
  }

  protected static HttpResponseException schemaMismatch(String eventKey) {
    return new BadRequestResponse("Invalid submission schema for event " + eventKey);
  }

  protected static HttpResponseException forbiddenTeam(int teamNum) {
    return new ForbiddenResponse("Team " + teamNum + " cannot access another team");
  }

  protected static HttpResponseException teamNotFound(int teamNum) {
    return new NotFoundResponse("Team " + teamNum + " not found");
  }

  protected static HttpResponseException userConflict(int teamNum, String username) {
    return new ConflictResponse("User " + username + " already exists on team " + teamNum);
  }

  protected static HttpResponseException userNotFound(String userId) {
    return new NotFoundResponse("User with id " + userId + " not found");
  }

  protected static HttpResponseException userNotFound(int teamNum, String username) {
    return new NotFoundResponse("User " + username + " on team " + teamNum + " not found");
  }

  protected static HttpResponseException incorrectCredentials(int teamNum, String username) {
    return new UnauthorizedResponse("Incorrect credentials for user " + username + " on team "
        + teamNum);
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
        throw new ForbiddenResponse("Access to resource requires admin privileges");
      }
    }
  }

  public static record ApiError(String error) {}
}
