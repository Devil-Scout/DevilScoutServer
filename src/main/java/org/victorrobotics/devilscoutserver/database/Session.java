package org.victorrobotics.devilscoutserver.database;

public class Session {
  private static final long DURATION_MILLIS = 8 * 60 * 60 * 1000;

  public final long sessionID;
  public final long userID;

  public final Permission permission;

  private long expireTime;

  public Session(long sessionID, long userID, Permission permission) {
    this.sessionID = sessionID;
    this.userID = userID;
    this.permission = permission;

    expireTime = System.currentTimeMillis() + DURATION_MILLIS;
  }

  public boolean isExpired() {
    return System.currentTimeMillis() >= expireTime;
  }

  public void refresh() {
    expireTime = System.currentTimeMillis() + DURATION_MILLIS;
  }

  public boolean hasPermission(Permission permission) {
    return permission != null && permission.ordinal() <= this.permission.ordinal();
  }
}
