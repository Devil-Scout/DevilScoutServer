package org.victorrobotics.devilscoutserver.database;

public record User(long userID,
                   String username,
                   String fullName,
                   int team,
                   AccessLevel accessLevel,
                   byte[] salt,
                   byte[] storedKey,
                   byte[] serverKey) {
  public enum AccessLevel {
    USER,
    COACH,
    SUDO;
  }
}
