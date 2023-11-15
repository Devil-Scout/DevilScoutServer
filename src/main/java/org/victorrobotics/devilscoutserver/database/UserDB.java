package org.victorrobotics.devilscoutserver.database;

import org.victorrobotics.devilscoutserver.data.User;
import org.victorrobotics.devilscoutserver.data.UserAccessLevel;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;

public class UserDB {
  private final Set<String>       nonces;
  private final Map<String, User> usersByName;
  private final Map<Long, User>   usersByID;

  public UserDB() {
    nonces = new HashSet<>();
    usersByName = new HashMap<>();
    usersByID = new HashMap<>();
    User testUser =
        new User(5, "xander", "Xander Bhalla", 1559, UserAccessLevel.SUDO, "bad-salt".getBytes(),
                 parseHex("8cc790682ce826cf353286c241f70c4aae16dbdf1a0274ac1795911917fb535b"),
                 parseHex("86c11c32671aa7d5962eff976284ff81a981e9bfcfded80ea0e38881b8b6e96f"));
    usersByName.put(userKey(testUser.team(), testUser.username()), testUser);
    usersByID.put(testUser.userID(), testUser);
  }

  public User getUser(int team, String name) {
    return usersByName.get(team + "," + name);
  }

  public User getUser(long userID) {
    return usersByID.get(userID);
  }

  public byte[] getSalt(int team, String username) {
    User entry = getUser(team, username);
    return entry == null ? null : entry.salt();
  }

  public Collection<User> allUsers() {
    return Collections.unmodifiableCollection(usersByName.values());
  }

  public void putNonce(String nonceID) {
    nonces.add(nonceID);
  }

  public boolean containsNonce(String nonceID) {
    return nonces.contains(nonceID);
  }

  public void removeNonce(String nonceID) {
    nonces.remove(nonceID);
  }

  private static String userKey(int team, String username) {
    return team + "," + username;
  }

  private static byte[] parseHex(String hex) {
    return HexFormat.of()
                    .parseHex(hex);
  }
}
