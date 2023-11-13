package org.victorrobotics.devilscoutserver.database;

public interface SessionDB {
  void registerSession(Session session);

  Session getSession(String sessionID);

  void deleteSession(Session session);
}
