package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.database.Session;
import org.victorrobotics.devilscoutserver.database.SessionDB;
import org.victorrobotics.devilscoutserver.database.TeamDB;
import org.victorrobotics.devilscoutserver.database.UserAccessLevel;
import org.victorrobotics.devilscoutserver.database.UserDB;
import org.victorrobotics.devilscoutserver.tba.data.EventInfoCache;
import org.victorrobotics.devilscoutserver.tba.data.EventTeamsCache;
import org.victorrobotics.devilscoutserver.tba.data.MatchScheduleCache;
import org.victorrobotics.devilscoutserver.tba.data.TeamInfoCache;

import java.security.SecureRandom;
import java.util.Base64;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.NotModifiedResponse;
import io.javalin.http.UnauthorizedResponse;

public class Controller {
  public static final String SESSION_HEADER = "X-DS-SESSION-KEY";

  protected static final String HASH_ALGORITHM   = "SHA-256";
  protected static final String MAC_ALGORITHM    = "HmacSHA256";
  protected static final String KEYGEN_ALGORITHM = "PBKDF2WithHmacSHA256";

  protected static final SecureRandom SECURE_RANDOM  = new SecureRandom();
  private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
  private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();

  private static final Class<?>[] CONTROLLERS = { SessionController.class, UserController.class,
                                                  QuestionController.class, EventController.class };

  private static UserDB       USERS;
  private static SessionDB    SESSIONS;
  private static TeamDB TEAMS;

  private static TeamInfoCache      TEAM_INFO_CACHE;
  private static EventInfoCache     EVENT_INFO_CACHE;
  private static EventTeamsCache    EVENT_TEAMS_CACHE;
  private static MatchScheduleCache MATCH_SCHEDULE_CACHE;

  protected Controller() {}

  @SuppressWarnings("java:S2658") // dynamic class loading
  public static void loadAll() {
    try {
      for (Class<?> controllerClazz : CONTROLLERS) {
        Class.forName(controllerClazz.getName());
      }
    } catch (ClassNotFoundException e) {}
  }

  public static void setUserDB(UserDB users) {
    USERS = users;
  }

  public static void setSessionDB(SessionDB sessions) {
    SESSIONS = sessions;
  }

  public static void setTeamDB(TeamDB teams) {
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

  public static UserDB userDB() {
    return USERS;
  }

  public static SessionDB sessionDB() {
    return SESSIONS;
  }

  public static TeamDB teamDB() {
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
      throw new BadRequestResponse();
    }
  }

  protected static Session getValidSession(Context ctx) {
    String sessionStr = ctx.header(SESSION_HEADER);
    if (sessionStr == null) {
      throw new UnauthorizedResponse("Missing " + SESSION_HEADER + " Header");
    }

    try {
      long sessionId = Long.parseLong(sessionStr);
      Session session = SESSIONS.getSession(sessionId);
      if (session != null && !session.isExpired()) {
        return session;
      }
    } catch (NumberFormatException e) {}

    throw new UnauthorizedResponse("Invalid/Expired " + SESSION_HEADER + " Header");
  }

  protected static Session getValidSession(Context ctx, UserAccessLevel accessLevel) {
    Session session = getValidSession(ctx);
    session.verifyAccess(accessLevel);
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

  public static String base64Encode(byte[] bytes) {
    return BASE64_ENCODER.encodeToString(bytes);
  }

  public static byte[] base64Decode(String base64) {
    return BASE64_DECODER.decode(base64);
  }
}
