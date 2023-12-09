package org.victorrobotics.devilscoutserver.database;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SessionDB {
  private final ConcurrentMap<Long, Session> sessions;

  public SessionDB() {
    sessions = new ConcurrentHashMap<>();
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
