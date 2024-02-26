package org.victorrobotics.devilscoutserver.session;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(SessionManager.class);

  private final ConcurrentMap<String, Session> sessions;

  public SessionManager() {
    sessions = new ConcurrentHashMap<>();
  }

  public Session get(String key) {
    Session session = sessions.get(key);
    if (session == null || session.isExpired()) return null;

    session.refresh();
    return session;
  }

  public Session create(String userId, int teamNum) {
    String sessionKey = UUID.randomUUID()
                            .toString();
    Session session = new Session(sessionKey, userId, teamNum);
    return add(session);
  }

  public Session add(Session session) {
    sessions.put(session.getKey(), session);
    return session;
  }

  public boolean logout(String key) {
    return sessions.remove(key) != null;
  }

  public boolean logoutTeam(int teamNum) {
    return sessions.values()
                   .removeIf(s -> s.getTeam() == teamNum);
  }

  public boolean logoutUser(String userId) {
    return sessions.values()
                   .removeIf(s -> s.getUser()
                                   .equals(userId));
  }

  public void purgeExpired() {
    long start = System.currentTimeMillis();
    int size = sessions.size();
    sessions.values()
            .removeIf(Session::isExpired);
    LOGGER.info("Purged {} expired sessions in {}ms", size - sessions.size(),
                System.currentTimeMillis() - start);
  }
}
