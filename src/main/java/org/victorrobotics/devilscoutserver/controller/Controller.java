package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.caches.EventInfoCache;
import org.victorrobotics.devilscoutserver.caches.EventTeamsCache;
import org.victorrobotics.devilscoutserver.caches.MatchScheduleCache;
import org.victorrobotics.devilscoutserver.caches.TeamInfoCache;
import org.victorrobotics.devilscoutserver.data.Session;
import org.victorrobotics.devilscoutserver.data.UserAccessLevel;
import org.victorrobotics.devilscoutserver.database.SessionDB;
import org.victorrobotics.devilscoutserver.database.TeamConfigDB;
import org.victorrobotics.devilscoutserver.database.UserDB;

import java.security.SecureRandom;
import java.util.Base64;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
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
  private static TeamConfigDB TEAMS;

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

  public static void setTeamDB(TeamConfigDB teams) {
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

  public static void refreshCaches() {
    EVENT_INFO_CACHE.refresh();
    TEAM_INFO_CACHE.refresh();
    EVENT_TEAMS_CACHE.refresh();
    MATCH_SCHEDULE_CACHE.refresh();
  }

  public static UserDB userDB() {
    return USERS;
  }

  public static SessionDB sessionDB() {
    return SESSIONS;
  }

  public static TeamConfigDB teamDB() {
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
    if (!session.hasAccess(accessLevel)) {
      throw new ForbiddenResponse();
    }
    return session;
  }

  protected static void checkIfNoneMatch(Context ctx, String latest) {
    String etag = ctx.header("If-None-Match");
    if (("\"" + latest + "\"").equals(etag)) {
      throw new NotModifiedResponse();
    }
  }

  protected static void checkIfNoneMatch(Context ctx, long timestamp) {
    try {
      String etag = ctx.header("If-None-Match");
      if (etag == null) return;

      long time = Long.parseLong(etag.substring(1, etag.length() - 1));
      if (time >= timestamp) {
        throw new NotModifiedResponse();
      }
    } catch (NumberFormatException e) {}
  }

  protected static void setResponseETag(Context ctx, String etag) {
    ctx.header("ETag", "\"" + etag + "\"");
  }

  public static String base64Encode(byte[] bytes) {
    return BASE64_ENCODER.encodeToString(bytes);
  }

  public static byte[] base64Decode(String base64) {
    return BASE64_DECODER.decode(base64);
  }
}
