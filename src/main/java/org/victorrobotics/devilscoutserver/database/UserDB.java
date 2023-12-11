package org.victorrobotics.devilscoutserver.database;

import static org.victorrobotics.devilscoutserver.Base64Util.base64Encode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("java:S2325")
public final class UserDB extends Database {
  private static final String ADD_USER      = """
      INSERT INTO users
      (team, username, full_name, access_level, salt, stored_key, server_key)
      VALUES (?, ?, ?, ?::user_access_level, ?, ?, ?)
      RETURNING *;
      """;
  private static final String DELETE_USER   = "DELETE FROM users WHERE id = ?;";
  private static final String USER_BY_KEY   =
      "SELECT * FROM users WHERE team = ? AND username = ?;";
  private static final String USER_BY_ID    = "SELECT * FROM users WHERE id = ?;";
  private static final String USERS_BY_TEAM = "SELECT * FROM users WHERE team = ?;";
  private static final String ALL_USERS     = "SELECT * FROM users;";

  public UserDB() {}

  public User registerUser(int team, String username, String fullName, UserAccessLevel accessLevel,
                           byte[] salt, byte[] storedKey, byte[] serverKey)
      throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(ADD_USER)) {
      statement.setShort(1, (short) team);
      statement.setString(2, username);
      statement.setString(3, fullName);
      statement.setString(4, accessLevel.toString());
      statement.setString(5, base64Encode(salt));
      statement.setString(6, base64Encode(storedKey));
      statement.setString(7, base64Encode(serverKey));
      statement.execute();

      ResultSet resultSet = statement.getResultSet();
      return resultSet.next() ? User.fromDatabase(resultSet) : null;
    }
  }

  public void deleteUser(long id) throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(DELETE_USER)) {
      statement.setLong(1, id);
      statement.execute();
    }
  }

  public User editUser(long id, String username, String fullName, UserAccessLevel accessLevel,
                       byte[][] authInfo)
      throws SQLException {
    StringBuilder edits = new StringBuilder();

    if (username != null) {
      edits.append("username = '" + username + "', ");
    }

    if (fullName != null) {
      edits.append("full_name = '" + fullName + "', ");
    }

    if (accessLevel != null) {
      edits.append("access_level = '" + accessLevel + "', ");
    }

    if (authInfo != null) {
      edits.append("salt = '" + base64Encode(authInfo[0]) + "', stored_key = '"
          + base64Encode(authInfo[1]) + "', server_key = '" + base64Encode(authInfo[2]) + "', ");
    }

    if (edits.length() == 0) {
      return getUser(id);
    }

    String query =
        "UPDATE users SET " + edits.substring(0, edits.length() - 2) + "WHERE id = " + id + ";";
    try (Connection connection = getConnection();
         Statement statement = connection.createStatement()) {
      statement.execute(query);
    }

    return getUser(id);
  }

  public User getUser(int team, String username) throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(USER_BY_KEY)) {
      statement.setShort(1, (short) team);
      statement.setString(2, username);
      statement.execute();

      ResultSet resultSet = statement.getResultSet();
      return resultSet.next() ? User.fromDatabase(resultSet) : null;
    }
  }

  public User getUser(long id) throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(USER_BY_ID)) {
      statement.setLong(1, id);
      statement.execute();

      ResultSet resultSet = statement.getResultSet();
      return resultSet.next() ? User.fromDatabase(resultSet) : null;
    }
  }

  public Collection<User> allUsers() throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(ALL_USERS)) {
      statement.execute();

      ResultSet resultSet = statement.getResultSet();
      List<User> users = new ArrayList<>();
      while (resultSet.next()) {
        users.add(User.fromDatabase(resultSet));
      }

      return List.copyOf(users);
    }
  }

  public Collection<User> usersOnTeam(int team) throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(USERS_BY_TEAM)) {
      statement.setShort(1, (short) team);
      statement.execute();

      ResultSet resultSet = statement.getResultSet();
      List<User> users = new ArrayList<>();
      while (resultSet.next()) {
        users.add(User.fromDatabase(resultSet));
      }

      return List.copyOf(users);
    }
  }

  public UserAccessLevel getAccessLevel(long id) throws SQLException {
    if (id == -1) return UserAccessLevel.SUDO;
    if (id == -2) return UserAccessLevel.ADMIN;
    if (id == -3) return UserAccessLevel.USER;

    return getUser(id).accessLevel();
  }
}
