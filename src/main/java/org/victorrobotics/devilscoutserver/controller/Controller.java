package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.analysis.TeamAnalysisCache;
import org.victorrobotics.devilscoutserver.database.DriveTeamEntryDatabase;
import org.victorrobotics.devilscoutserver.database.MatchEntryDatabase;
import org.victorrobotics.devilscoutserver.database.PitEntryDatabase;
import org.victorrobotics.devilscoutserver.database.TeamDatabase;
import org.victorrobotics.devilscoutserver.database.UserDatabase;
import org.victorrobotics.devilscoutserver.tba.EventCache;
import org.victorrobotics.devilscoutserver.tba.EventTeamCache;
import org.victorrobotics.devilscoutserver.tba.EventTeamListCache;
import org.victorrobotics.devilscoutserver.tba.MatchScheduleCache;

import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.CreatedResponse;
import io.javalin.http.NoContentResponse;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.NotModifiedResponse;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiIgnore;
import io.javalin.openapi.OpenApiRequired;

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

  private static EventTeamCache     TEAM_CACHE;
  private static EventCache         EVENT_CACHE;
  private static EventTeamListCache EVENT_TEAMS_CACHE;
  private static MatchScheduleCache MATCH_SCHEDULE_CACHE;

  private static MatchEntryDatabase     MATCH_ENTRIES;
  private static PitEntryDatabase       PIT_ENTRIES;
  private static DriveTeamEntryDatabase DRIVE_TEAM_ENTRIES;

  private static TeamAnalysisCache TEAM_ANALYSIS_CACHE;

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

  public static void setTeamCache(EventTeamCache cache) {
    TEAM_CACHE = cache;
  }

  public static void setEventTeamsCache(EventTeamListCache cache) {
    EVENT_TEAMS_CACHE = cache;
  }

  public static void setMatchScheduleCache(MatchScheduleCache cache) {
    MATCH_SCHEDULE_CACHE = cache;
  }

  public static void setMatchEntryDB(MatchEntryDatabase matchEntries) {
    MATCH_ENTRIES = matchEntries;
  }

  public static void setPitEntryDB(PitEntryDatabase pitEntries) {
    PIT_ENTRIES = pitEntries;
  }

  public static void setDriveTeamEntryDB(DriveTeamEntryDatabase driveTeamEntries) {
    DRIVE_TEAM_ENTRIES = driveTeamEntries;
  }

  public static void setTeamAnalysisCache(TeamAnalysisCache teamAnalysisCache) {
    TEAM_ANALYSIS_CACHE = teamAnalysisCache;
  }

  public static ConcurrentMap<String, Session> sessions() {
    return SESSIONS;
  }

  public static UserDatabase userDB() {
    return USERS;
  }

  public static TeamDatabase teamDB() {
    return TEAMS;
  }

  public static EventTeamCache teamCache() {
    return TEAM_CACHE;
  }

  public static EventCache eventCache() {
    return EVENT_CACHE;
  }

  public static EventTeamListCache eventTeamsCache() {
    return EVENT_TEAMS_CACHE;
  }

  public static MatchScheduleCache matchScheduleCache() {
    return MATCH_SCHEDULE_CACHE;
  }

  public static MatchEntryDatabase matchEntryDB() {
    return MATCH_ENTRIES;
  }

  public static PitEntryDatabase pitEntryDB() {
    return PIT_ENTRIES;
  }

  public static DriveTeamEntryDatabase driveTeamEntryDB() {
    return DRIVE_TEAM_ENTRIES;
  }

  public static TeamAnalysisCache teamAnalysisCache() {
    return TEAM_ANALYSIS_CACHE;
  }

  @SuppressWarnings("java:S2221") // catch generic exception
  protected static <T> T jsonDecode(Context ctx, Class<T> clazz) {
    try {
      return ctx.bodyAsClass(clazz);
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

  protected static void throwNoContent() {
    throw new NoContentResponse();
  }

  protected static void throwCreated() {
    throw new CreatedResponse();
  }

  protected static void throwTeamNotFound(int team) {
    throw new NotFoundResponse("Team " + team + " not found");
  }

  protected static void throwUserNotFound(String userId) {
    throw new NotFoundResponse("User #" + userId + " not found");
  }

  protected static void throwUserNotFound(String username, int team) {
    throw new NotFoundResponse("User " + username + "@" + team + " not found");
  }

  protected static void throwEventNotFound(String eventKey) {
    throw new NotFoundResponse("Event " + eventKey + " not found");
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
    @OpenApiIgnore
    public boolean isExpired() {
      return System.currentTimeMillis() >= expiration;
    }

    @OpenApiExample("ffffffff-ffff-ffff-ffff-ffffffffffff")
    public String getKey() {
      return key;
    }

    @OpenApiExample("ffffffff-ffff-ffff-ffff-ffffffffffff")
    public String getUser() {
      return user;
    }

    @OpenApiExample("1559")
    public int getTeam() {
      return team;
    }

    public void refresh() {
      expiration = System.currentTimeMillis() + DURATION_MILLIS;
    }
  }

  public static record Error(@OpenApiRequired @OpenApiExample("Error message") String error) {}
}
