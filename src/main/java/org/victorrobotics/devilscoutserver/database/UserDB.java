package org.victorrobotics.devilscoutserver.database;

public interface UserDB {
  UserDB INSTANCE = new MockUserDB();

  User getUser(int team, String username);

  default byte[] getSalt(int team, String username) {
    User entry = getUser(team, username);
    return entry == null ? null : entry.salt();
  }

  void putNonce(String nonceID);

  boolean containsNonce(String nonceID);
}
