package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.cache.EventInfoCache;
import org.victorrobotics.devilscoutserver.data.Session;
import org.victorrobotics.devilscoutserver.data.UserAccessLevel;
import org.victorrobotics.devilscoutserver.database.SessionDB;
import org.victorrobotics.devilscoutserver.database.TeamConfigDB;
import org.victorrobotics.devilscoutserver.database.UserDB;

import java.security.SecureRandom;

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

  protected static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private static final Class<?>[] CONTROLLERS =
      { SessionController.class, EventInfoController.class, UserController.class };

  private static UserDB       USERS;
  private static SessionDB    SESSIONS;
  private static TeamConfigDB TEAMS;

  private static EventInfoCache EVENT_INFO_CACHE;

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

  protected static UserDB userDB() {
    return USERS;
  }

  protected static SessionDB sessionDB() {
    return SESSIONS;
  }

  protected static TeamConfigDB teamDB() {
    return TEAMS;
  }

  protected static EventInfoCache eventInfoCache() {
    return EVENT_INFO_CACHE;
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

  protected static void setResponseETag(Context ctx, String etag) {
    ctx.header("ETag", "\"" + etag + "\"");
  }
}
