package org.victorrobotics.devilscoutserver.controller;

import org.victorrobotics.devilscoutserver.database.Session;
import org.victorrobotics.devilscoutserver.database.SessionDB;
import org.victorrobotics.devilscoutserver.database.UserDB;

import java.util.Base64;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;

public class Controller {
  private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();

  private static UserDB    USERS;
  private static SessionDB SESSIONS;

  protected Controller() {}

  protected static String base64Encode(byte[] bytes) {
    return BASE64_ENCODER.encodeToString(bytes);
  }

  @SuppressWarnings("java:S2221") // catch generic exception
  protected static <T> T jsonDecode(Context ctx, Class<T> clazz) {
    try {
      return ctx.bodyAsClass(clazz);
    } catch (Exception e) {
      throw new BadRequestResponse();
    }
  }

  public static void setUserDB(UserDB users) {
    USERS = users;
  }

  public static void setSessionDB(SessionDB sessions) {
    SESSIONS = sessions;
  }

  protected static UserDB userDB() {
    return USERS;
  }

  protected static SessionDB sessionDB() {
    return SESSIONS;
  }

  protected static Session getValidSession(Context ctx) {
    String sessionID = ctx.header("X-DS-SESSION-KEY");
    if (sessionID == null) {
      throw new UnauthorizedResponse();
    }

    Session session = SESSIONS.getSession(sessionID);
    if (session == null || session.isExpired()) {
      throw new UnauthorizedResponse();
    }

    return session;
  }
}
