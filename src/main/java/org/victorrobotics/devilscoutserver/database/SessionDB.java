package org.victorrobotics.devilscoutserver.database;

import org.victorrobotics.devilscoutserver.data.Session;

import java.util.HashMap;
import java.util.Map;

public class SessionDB {
  private final Map<String, Session> sessions;

  public SessionDB() {
    sessions = new HashMap<>();
  }

  public void registerSession(Session session) {
    sessions.put(session.getSessionID(), session);
  }

  public Session getSession(String sessionID) {
    return sessions.get(sessionID);
  }

  public void deleteSession(Session session) {
    sessions.remove(session.getSessionID());
  }
}
