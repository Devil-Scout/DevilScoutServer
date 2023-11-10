package org.victorrobotics.devilscoutserver.database;

public interface CredentialDB {
  Credentials get(int team, String username);

  default byte[] getSalt(int team, String username) {
    Credentials entry = get(team, username);
    return entry == null ? null : entry.salt();
  }

  void putNonce(byte[] userHash, byte[] nonceHash);

  boolean containsNonce(byte[] userHash, byte[] nonceHash);
}
