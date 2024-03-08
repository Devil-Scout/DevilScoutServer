package org.victorrobotics.devilscoutserver.database;

import static org.victorrobotics.devilscoutserver.EncodingUtil.base64Decode;
import static org.victorrobotics.devilscoutserver.EncodingUtil.base64Encode;

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
  private static final String SELECT_USERS_BY_TEAM =
      "SELECT * FROM users WHERE team = ? ORDER BY id";
  private static final String SELECT_USER_BY_ID    = "SELECT * FROM users WHERE id = ?";

  private static final String SELECT_USER_BY_TEAM_AND_USERNAME =
      "SELECT * FROM users WHERE team = ? AND username = ?::citext";
  private static final String SELECT_SALT_BY_TEAM_AND_USERNAME =
      "SELECT salt FROM users WHERE team = ? AND username = ?::citext";

  private static final String SELECT_ADMIN_BY_ID = "SELECT admin FROM users WHERE id = ?";

  private static final String INSERT_USER =
      "INSERT INTO users (team, username, full_name, admin, salt, stored_key, server_key) VALUES (?, ?, ?, ?, ?, ?, ?)";

  private static final String DELETE_USER = "DELETE FROM users WHERE id = ?";

  public UserDatabase() {}

  public Collection<User> usersOnTeam(int team) throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(SELECT_USERS_BY_TEAM)) {
      statement.setShort(1, (short) team);
      try (ResultSet resultSet = statement.executeQuery()) {
        return listFromDatabase(resultSet, User::fromDatabase);
      }
    }
  }

  public User getUser(String id) throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(SELECT_USER_BY_ID)) {
      statement.setString(1, id);
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

  public User registerUser(int team, String username, String fullName, boolean admin, byte[] salt,
                           byte[] storedKey, byte[] serverKey)
      throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement =
             connection.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {
      statement.setShort(1, (short) team);
      statement.setString(2, username);
      statement.setString(3, fullName);
      statement.setBoolean(4, admin);
      statement.setString(5, base64Encode(salt));
      statement.setString(6, base64Encode(storedKey));
      statement.setString(7, base64Encode(serverKey));

      statement.execute();
      try (ResultSet resultSet = statement.getGeneratedKeys()) {
        return !resultSet.next() ? null : new User(resultSet.getString(1), team, username, fullName,
                                                   admin, salt, storedKey, serverKey);
      }
    }
  }

  public void deleteUser(String id) throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(DELETE_USER)) {
      statement.setString(1, id);
      statement.execute();
    }
  }

  public User editUser(String id, String username, String fullName, Boolean admin,
                       byte[][] authInfo)
      throws SQLException {
    List<String> edits = new ArrayList<>();

    if (username != null) {
      edits.add("username = ?");
    }

    if (fullName != null) {
      edits.add("full_name = ?");
    }

    if (admin != null) {
      edits.add("admin = ?");
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

      if (admin != null) {
        statement.setBoolean(index++, admin);
      }

      if (authInfo != null) {
        statement.setString(index++, base64Encode(authInfo[0]));
        statement.setString(index++, base64Encode(authInfo[1]));
        statement.setString(index++, base64Encode(authInfo[2]));
      }

      statement.setString(index, id);
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

  public boolean isAdmin(String id) throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(SELECT_ADMIN_BY_ID)) {
      statement.setString(1, id);
      try (ResultSet resultSet = statement.executeQuery()) {
        return resultSet.next() && resultSet.getBoolean(1);
      }
    }
  }
}
