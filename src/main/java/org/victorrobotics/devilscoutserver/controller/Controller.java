package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.database.TeamDatabase;
import org.victorrobotics.devilscoutserver.database.UserDatabase;
import org.victorrobotics.devilscoutserver.tba.data.EventInfoCache;
import org.victorrobotics.devilscoutserver.tba.data.EventTeamsCache;
import org.victorrobotics.devilscoutserver.tba.data.MatchScheduleCache;
import org.victorrobotics.devilscoutserver.tba.data.TeamInfoCache;

import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.CreatedResponse;
import io.javalin.http.NoContentResponse;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.NotModifiedResponse;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequired;

public sealed class Controller
    permits EventController, QuestionController, SessionController, TeamController, UserController {
  public static final String SESSION_HEADER = "X-DS-SESSION-KEY";

  private static final ConcurrentMap<Long, Session> SESSIONS = new ConcurrentHashMap<>();

  protected static final String HASH_ALGORITHM   = "SHA-256";
  protected static final String MAC_ALGORITHM    = "HmacSHA256";
  protected static final String KEYGEN_ALGORITHM = "PBKDF2WithHmacSHA256";

  protected static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private static UserDatabase USERS;
  private static TeamDatabase TEAMS;

  private static TeamInfoCache      TEAM_INFO_CACHE;
  private static EventInfoCache     EVENT_INFO_CACHE;
  private static EventTeamsCache    EVENT_TEAMS_CACHE;
  private static MatchScheduleCache MATCH_SCHEDULE_CACHE;

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

  public static void setTeamInfoCache(TeamInfoCache cache) {
    TEAM_INFO_CACHE = cache;
  }

  public static void setEventTeamsCache(EventTeamsCache cache) {
    EVENT_TEAMS_CACHE = cache;
  }

  public static void setMatchScheduleCache(MatchScheduleCache cache) {
    MATCH_SCHEDULE_CACHE = cache;
  }

  public static ConcurrentMap<Long, Session> sessions() {
    return SESSIONS;
  }

  public static UserDatabase userDB() {
    return USERS;
  }

  public static TeamDatabase teamDB() {
    return TEAMS;
  }

  public static TeamInfoCache teamInfoCache() {
    return TEAM_INFO_CACHE;
  }

  public static EventInfoCache eventCache() {
    return EVENT_INFO_CACHE;
  }

  public static EventTeamsCache eventTeamsCache() {
    return EVENT_TEAMS_CACHE;
  }

  public static MatchScheduleCache matchScheduleCache() {
    return MATCH_SCHEDULE_CACHE;
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
    String sessionStr = ctx.header(SESSION_HEADER);
    if (sessionStr == null) {
      throw new UnauthorizedResponse("Missing " + SESSION_HEADER + " header");
    }

    long sessionId;
    try {
      sessionId = Long.parseLong(sessionStr);
    } catch (NumberFormatException e) {
      throw new UnauthorizedResponse("Invalid " + SESSION_HEADER + " header");
    }

    Session session = SESSIONS.get(sessionId);

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

  protected static void throwUserNotFound(long userId) {
    throw new NotFoundResponse("User #" + userId + " not found");
  }

  protected static void throwUserNotFound(String username, int team) {
    throw new NotFoundResponse("User " + username + "@" + team + " not found");
  }

  protected static void throwEventNotFound(String eventKey) {
    throw new NotFoundResponse("Event " + eventKey + " not found");
  }

  public static record Error(@OpenApiRequired @OpenApiExample("Error message") String error) {}
}
