package org.victorrobotics.devilscoutserver.database;

public class Session {
  private static final long DURATION_MILLIS = 8 * 60 * 60 * 1000;

  public final long sessionID;
  public final long userID;

  public final User.AccessLevel accessLevel;

  private long expireTime;

  public Session(long sessionID, long userID, User.AccessLevel accessLevel) {
    this.sessionID = sessionID;
    this.userID = userID;
    this.accessLevel = accessLevel;

    expireTime = System.currentTimeMillis() + DURATION_MILLIS;
  }

  public boolean isExpired() {
    return System.currentTimeMillis() >= expireTime;
  }

  public void refresh() {
    expireTime = System.currentTimeMillis() + DURATION_MILLIS;
  }

  public boolean hasAccess(User.AccessLevel accessLevel) {
    return accessLevel != null && accessLevel.ordinal() <= this.accessLevel.ordinal();
  }
}
