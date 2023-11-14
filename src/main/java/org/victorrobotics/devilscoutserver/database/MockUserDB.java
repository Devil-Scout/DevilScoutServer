package org.victorrobotics.devilscoutserver.database;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;

public class MockUserDB implements UserDB {
  private final Set<String>       nonces;
  private final Map<String, User> usersByName;
  private final Map<Long, User>   usersByID;

  public MockUserDB() {
    nonces = new HashSet<>();
    usersByName = new HashMap<>();
    usersByID = new HashMap<>();
    User testUser =
        new User(5, "xander", "Xander Bhalla", 1559, User.AccessLevel.SUDO, "bad-salt".getBytes(),
                 parseHex("8cc790682ce826cf353286c241f70c4aae16dbdf1a0274ac1795911917fb535b"),
                 parseHex("86c11c32671aa7d5962eff976284ff81a981e9bfcfded80ea0e38881b8b6e96f"));
    usersByName.put(userKey(testUser.team(), testUser.username()), testUser);
    usersByID.put(testUser.userID(), testUser);
  }

  @Override
  public User getUser(int team, String name) {
    return usersByName.get(team + "," + name);
  }

  @Override
  public User getUser(long userID) {
    return usersByID.get(userID);
  }

  @Override
  public Collection<User> allUsers() {
    return Collections.unmodifiableCollection(usersByName.values());
  }

  @Override
  public void putNonce(String nonceID) {
    nonces.add(nonceID);
  }

  @Override
  public boolean containsNonce(String nonceID) {
    return nonces.contains(nonceID);
  }

  @Override
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
