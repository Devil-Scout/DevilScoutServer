package org.victorrobotics.devilscoutserver.database;

import static org.victorrobotics.devilscoutserver.Utils.base64Encode;

import java.util.Arrays;
import java.util.Objects;

public record User(long userID,
                   String username,
                   String fullName,
                   int team,
                   User.AccessLevel accessLevel,
                   byte[] salt,
                   byte[] storedKey,
                   byte[] serverKey) {
  public enum AccessLevel {
    USER,
    ADMIN,
    SUDO;
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj || (obj instanceof User other && userID() == other.userID()
        && Objects.equals(username(), other.username())
        && Objects.equals(fullName(), other.fullName()) && team() == other.team()
        && accessLevel() == other.accessLevel() && Arrays.equals(salt(), other.salt())
        && Arrays.equals(storedKey(), other.storedKey())
        && Arrays.equals(serverKey(), other.serverKey()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(userID(), username(), fullName(), team(), accessLevel(),
                        Arrays.hashCode(salt()), Arrays.hashCode(storedKey()),
                        Arrays.hashCode(serverKey()));
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append("User[")
     .append("userID=")
     .append(userID())
     .append(",username=")
     .append(username())
     .append(",fullName=")
     .append(fullName())
     .append(",team=")
     .append(team())
     .append(",accessLevel=")
     .append(accessLevel())
     .append(",salt=")
     .append(base64Encode(salt()))
     .append(",storerdKey=")
     .append(base64Encode(storedKey()))
     .append(",serverKey=")
     .append(base64Encode(serverKey()))
     .append("]");
    return b.toString();
  }
}
