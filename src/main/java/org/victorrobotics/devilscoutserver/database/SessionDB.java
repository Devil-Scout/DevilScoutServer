package org.victorrobotics.devilscoutserver.database;

import org.victorrobotics.devilscoutserver.data.Session;

import java.util.HashMap;
import java.util.Map;

public class SessionDB {
  private final Map<Long, Session> sessions;

  public SessionDB() {
    sessions = new HashMap<>();
  }

  public void registerSession(Session session) {
    sessions.put(session.getId(), session);
  }

  public Session getSession(long sessionId) {
    return sessions.get(sessionId);
  }

  public void deleteSession(Session session) {
    sessions.remove(session.getId());
  }
}
