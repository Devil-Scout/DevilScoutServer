package org.victorrobotics.devilscoutserver.database;

import java.util.HashMap;
import java.util.Map;

public class MockSessionDB implements SessionDB {
  private final Map<String, Session> sessions;

  public MockSessionDB() {
    sessions = new HashMap<>();
  }

  @Override
  public void registerSession(Session session) {
    sessions.put(session.sessionID, session);
  }

  @Override
  public Session getSession(String sessionID) {
    return sessions.get(sessionID);
  }

  @Override
  public void deleteSession(Session session) {
    sessions.remove(session.sessionID);
  }
}
