package org.victorrobotics.devilscoutserver.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UserDB {
  private final Set<String> nonces;

  private final Map<String, User> usersByKey;
  private final Map<Long, User>   usersById;

  private final Map<Integer, Collection<User>> usersByTeam;

  public UserDB() {
    nonces = new HashSet<>();
    usersByKey = new HashMap<>();
    usersById = new HashMap<>();
    usersByTeam = new HashMap<>();
  }

  public void addUser(User user) {
    usersByKey.put(userKey(user.team(), user.username()), user);
    usersById.put(user.id(), user);
    usersByTeam.computeIfAbsent(user.team(), x -> new ArrayList<>())
               .add(user);
  }

  public void removeUser(User user) {
    usersByKey.remove(userKey(user.team(), user.username()));
    usersById.remove(user.id());
    usersByTeam.get(user.team())
               .remove(user);
  }

  public void editUser(User oldUser, User newUser) {
    removeUser(oldUser);
    addUser(newUser);
  }

  public User getUser(int team, String username) {
    return usersByKey.get(userKey(team, username));
  }

  public User getUser(long id) {
    return usersById.get(id);
  }

  public byte[] getSalt(int team, String username) {
    User entry = getUser(team, username);
    return entry == null ? null : entry.salt();
  }

  public Collection<User> allUsers() {
    return Collections.unmodifiableCollection(usersByKey.values());
  }

  public Collection<User> usersByTeam(int team) {
    Collection<User> users = usersByTeam.get(team);
    return users == null ? null : Collections.unmodifiableCollection(users);
  }

  public void putNonce(String nonceId) {
    nonces.add(nonceId);
  }

  public boolean containsNonce(String nonceId) {
    return nonces.contains(nonceId);
  }

  public void removeNonce(String nonceId) {
    nonces.remove(nonceId);
  }

  private static String userKey(int team, String username) {
    return team + "," + username;
  }
}
