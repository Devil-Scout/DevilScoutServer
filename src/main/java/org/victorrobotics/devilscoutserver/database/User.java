package org.victorrobotics.devilscoutserver.database;

import org.victorrobotics.devilscoutserver.data.UserAccessLevel;
import org.victorrobotics.devilscoutserver.data.UserInfo;

@SuppressWarnings("java:S6218") // consider arrays in equals, hashCode, toString
public record User(UserInfo info,
                   byte[] salt,
                   byte[] storedKey,
                   byte[] serverKey) {
  public User(long id, int team, String username, String fullName, UserAccessLevel accessLevel,
              byte[] salt, byte[] storedKey, byte[] serverKey) {
    this(new UserInfo(id, team, username, fullName, accessLevel), salt, storedKey, serverKey);
  }

  public int team() {
    return info().team();
  }

  public String username() {
    return info().username();
  }

  public String fullName() {
    return info().fullName();
  }

  public UserAccessLevel accessLevel() {
    return info().accessLevel();
  }

  public long id() {
    return info().id();
  }
}
