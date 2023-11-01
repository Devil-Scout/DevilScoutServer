package org.victorrobotics.devilscoutserver.database;

public interface CredentialDB {
  Credentials get(int team, String name);

  default byte[] getSalt(int team, String name) {
    Credentials entry = get(team, name);
    return entry == null ? null : entry.salt();
  }

  void putNonce(byte[] userHash, byte[] nonce);

  byte[] getNonce(byte[] userHash);
}
