package org.victorrobotics.devilscoutserver.database;

public interface UserDB {
  User get(int team, String username);

  default byte[] getSalt(int team, String username) {
    User entry = get(team, username);
    return entry == null ? null : entry.salt();
  }

  void putNonce(byte[] userHash, byte[] nonceHash);

  boolean containsNonce(byte[] userHash, byte[] nonceHash);
}
