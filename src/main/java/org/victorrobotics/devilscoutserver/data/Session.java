package org.victorrobotics.devilscoutserver.data;

public class Session {
  private static final long DURATION_MILLIS = 8 * 60 * 60 * 1000;

  private final String sessionID;
  private final long   userID;
  private final int    team;

  private final UserAccessLevel accessLevel;

  private long expireTime;

  public Session(String sessionID, long userID, int team, UserAccessLevel accessLevel) {
    this.sessionID = sessionID;
    this.userID = userID;
    this.team = team;
    this.accessLevel = accessLevel;

    expireTime = System.currentTimeMillis() + DURATION_MILLIS;
  }

  public boolean isExpired() {
    return System.currentTimeMillis() >= expireTime;
  }

  public void refresh() {
    expireTime = System.currentTimeMillis() + DURATION_MILLIS;
  }

  public boolean hasAccess(UserAccessLevel accessLevel) {
    return accessLevel != null && accessLevel.ordinal() <= this.accessLevel.ordinal();
  }

  public String getSessionID() {
    return sessionID;
  }

  public long getUserID() {
    return userID;
  }

  public int getTeam() {
    return team;
  }

  public UserAccessLevel getAccessLevel() {
    return accessLevel;
  }

  public long getExpireTime() {
    return expireTime;
  }
}
