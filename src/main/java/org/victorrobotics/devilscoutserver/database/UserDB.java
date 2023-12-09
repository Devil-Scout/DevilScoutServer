package org.victorrobotics.devilscoutserver.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UserDB {
  private final ConcurrentMap<String, User> usersByKey;
  private final ConcurrentMap<Long, User>   usersById;

  private final ConcurrentMap<Integer, Collection<User>> usersByTeam;

  public UserDB() {
    usersByKey = new ConcurrentHashMap<>();
    usersById = new ConcurrentHashMap<>();
    usersByTeam = new ConcurrentHashMap<>();
  }

  public void addUser(User user) {
    usersByKey.put(userKey(user.getTeam(), user.getUsername()), user);
    usersById.put(user.getId(), user);
    usersByTeam.computeIfAbsent(user.getTeam(), x -> new ArrayList<>())
               .add(user);
  }

  public void removeUser(User user) {
    usersByKey.remove(userKey(user.getTeam(), user.getUsername()));
    usersById.remove(user.getId());
    usersByTeam.get(user.getTeam())
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
    return entry == null ? null : entry.getSalt();
  }

  public Collection<User> allUsers() {
    return Collections.unmodifiableCollection(usersByKey.values());
  }

  public Collection<User> usersByTeam(int team) {
    Collection<User> users = usersByTeam.get(team);
    return users == null ? null : Collections.unmodifiableCollection(users);
  }

  private static String userKey(int team, String username) {
    return team + "," + username;
  }
}
