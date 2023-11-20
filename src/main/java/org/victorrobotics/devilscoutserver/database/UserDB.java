package org.victorrobotics.devilscoutserver.database;

import org.victorrobotics.devilscoutserver.data.User;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
  }

  public void addUser(User user) {
    usersByName.put(userKey(user.team(), user.username()), user);
    usersByID.put(user.userID(), user);
  }

  public User getUser(int team, String username) {
    return usersByName.get(userKey(team, username));
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
}
