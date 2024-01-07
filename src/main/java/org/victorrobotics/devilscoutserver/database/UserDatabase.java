package org.victorrobotics.devilscoutserver.database;

import static org.victorrobotics.devilscoutserver.EncodingUtil.base64Decode;
import static org.victorrobotics.devilscoutserver.EncodingUtil.base64Encode;

import org.victorrobotics.devilscoutserver.database.User.AccessLevel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("java:S2325")
public final class UserDatabase extends Database {
  private static final String SELECT_ALL_USERS     = "SELECT * FROM users ORDER BY id";
  private static final String SELECT_USERS_BY_TEAM =
      "SELECT * FROM users WHERE team = ? ORDER BY id";

  private static final String SELECT_USER_BY_ID                = "SELECT * FROM users WHERE id = ?";
  private static final String SELECT_USER_BY_TEAM_AND_USERNAME =
      "SELECT * FROM users WHERE team = ? AND username = ?";

  private static final String SELECT_SALT_BY_TEAM_AND_USERNAME =
      "SELECT salt FROM users WHERE team = ? AND username = ?";
  private static final String SELECT_ACCESS_LEVEL_BY_ID        =
      "SELECT access_level FROM users WHERE id = ?";

  private static final String COUNT_USER_BY_ID = "SELECT COUNT(*) FROM users WHERE id = ?";

  private static final String INSERT_USER = "INSERT INTO users "
      + "(team, username, full_name, access_level, salt, stored_key, server_key) "
      + "VALUES (?, ?, ?, ?::user_access_level, ?, ?, ?)";
  private static final String DELETE_USER = "DELETE FROM users WHERE id = ?";

  public UserDatabase() {}

  public Collection<User> allUsers() throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(SELECT_ALL_USERS);
         ResultSet resultSet = statement.executeQuery()) {
      return listFromDatabase(resultSet, User::fromDatabase);
    }
  }

  public Collection<User> usersOnTeam(int team) throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(SELECT_USERS_BY_TEAM)) {
      statement.setShort(1, (short) team);
      try (ResultSet resultSet = statement.executeQuery()) {
        return listFromDatabase(resultSet, User::fromDatabase);
      }
    }
  }

  public User getUser(long id) throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(SELECT_USER_BY_ID)) {
      statement.setLong(1, id);
      try (ResultSet resultSet = statement.executeQuery()) {
        return resultSet.next() ? User.fromDatabase(resultSet) : null;
      }
    }
  }

  public User getUser(int team, String username) throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement =
             connection.prepareStatement(SELECT_USER_BY_TEAM_AND_USERNAME)) {
      statement.setShort(1, (short) team);
      statement.setString(2, username);
      try (ResultSet resultSet = statement.executeQuery()) {
        return resultSet.next() ? User.fromDatabase(resultSet) : null;
      }
    }
  }

  public boolean containsUser(long id) throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(COUNT_USER_BY_ID)) {
      statement.setLong(1, id);
      try (ResultSet resultSet = statement.executeQuery()) {
        return resultSet.next() && resultSet.getInt(1) != 0;
      }
    }
  }

  public User registerUser(int team, String username, String fullName, AccessLevel accessLevel,
                           byte[] salt, byte[] storedKey, byte[] serverKey)
      throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement =
             connection.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {
      statement.setShort(1, (short) team);
      statement.setString(2, username);
      statement.setString(3, fullName);
      statement.setString(4, accessLevel.toString());
      statement.setString(5, base64Encode(salt));
      statement.setString(6, base64Encode(storedKey));
      statement.setString(7, base64Encode(serverKey));
      try (ResultSet resultSet = statement.executeQuery()) {
        return !resultSet.next() ? null : new User(resultSet.getLong(1), team, username, fullName,
                                                   accessLevel, salt, storedKey, serverKey);
      }
    }
  }

  public void deleteUser(long id) throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(DELETE_USER)) {
      statement.setLong(1, id);
      statement.execute();
    }
  }

  public User editUser(long id, String username, String fullName, AccessLevel accessLevel,
                       byte[][] authInfo)
      throws SQLException {
    List<String> edits = new ArrayList<>();

    if (username != null) {
      edits.add("username = ?");
    }

    if (fullName != null) {
      edits.add("full_name = ?");
    }

    if (accessLevel != null) {
      edits.add("access_level = ?::user_access_level");
    }

    if (authInfo != null) {
      edits.add("salt = ?");
      edits.add("stored_key = ?");
      edits.add("server_key = ?");
    }

    if (edits.isEmpty()) {
      return getUser(id);
    }

    String query = "UPDATE users SET " + String.join(", ", edits) + " WHERE id = ? RETURNING *";
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(query)) {
      int index = 1;

      if (username != null) {
        statement.setString(index++, username);
      }

      if (fullName != null) {
        statement.setString(index++, fullName);
      }

      if (accessLevel != null) {
        statement.setString(index++, accessLevel.toString());
      }

      if (authInfo != null) {
        statement.setString(index++, base64Encode(authInfo[0]));
        statement.setString(index++, base64Encode(authInfo[1]));
        statement.setString(index++, base64Encode(authInfo[2]));
      }

      statement.setLong(index, id);
      try (ResultSet resultSet = statement.executeQuery()) {
        return resultSet.next() ? User.fromDatabase(resultSet) : null;
      }
    }
  }

  public byte[] getSalt(int team, String username) throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement =
             connection.prepareStatement(SELECT_SALT_BY_TEAM_AND_USERNAME)) {
      statement.setShort(1, (short) team);
      statement.setString(2, username);
      try (ResultSet resultSet = statement.executeQuery()) {
        return resultSet.next() ? base64Decode(resultSet.getString(1)) : null;
      }
    }
  }

  public AccessLevel getAccessLevel(long id) throws SQLException {
    if (id == -1) return AccessLevel.SUDO;
    if (id == -2) return AccessLevel.ADMIN;
    if (id == -3) return AccessLevel.USER;

    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(SELECT_ACCESS_LEVEL_BY_ID)) {
      statement.setLong(1, id);
      try (ResultSet resultSet = statement.executeQuery()) {
        return resultSet.next() ? AccessLevel.valueOf(resultSet.getString(1)) : null;
      }
    }
  }
}
