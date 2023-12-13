package org.victorrobotics.devilscoutserver.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("java:S2325")
public final class TeamDatabase extends Database {
  private static final String ALL_TEAMS      = "SELECT * FROM teams ORDER BY number";
  private static final String TEAM_BY_NUMBER = "SELECT * FROM teams WHERE number = ?";
  private static final String CONTAINS_TEAM  = "SELECT COUNT(*) FROM teams WHERE number = ?";

  private static final String ADD_TEAM    =
      "INSERT INTO teams (number, name) VALUES (?, ?) RETURNING *";
  private static final String DELETE_TEAM = "DELETE FROM teams WHERE number = ?";

  public TeamDatabase() {}

  public Collection<Team> allTeams() throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(ALL_TEAMS);
         ResultSet resultSet = statement.executeQuery()) {
      return listFromDatabase(resultSet, Team::fromDatabase);
    }
  }

  public Team getTeam(int number) throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(TEAM_BY_NUMBER)) {
      statement.setShort(1, (short) number);
      try (ResultSet resultSet = statement.executeQuery()) {
        return resultSet.next() ? Team.fromDatabase(resultSet) : null;
      }
    }
  }

  public boolean containsTeam(int number) throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(CONTAINS_TEAM)) {
      statement.setShort(1, (short) number);
      try (ResultSet resultSet = statement.executeQuery()) {
        return resultSet.next() && resultSet.getInt("count") != 0;
      }
    }
  }

  public Team registerTeam(int number, String name) throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(ADD_TEAM)) {
      statement.setShort(1, (short) number);
      statement.setString(2, name);
      try (ResultSet resultSet = statement.executeQuery()) {
        return resultSet.next() ? Team.fromDatabase(resultSet) : null;
      }
    }
  }

  public void deleteTeam(int number) throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(DELETE_TEAM)) {
      statement.setShort(1, (short) number);
      statement.execute();
    }
  }

  public Team editTeam(int number, String name, String eventKey) throws SQLException {
    List<String> edits = new ArrayList<>();

    if (name != null) {
      edits.add("name = ?");
    }

    if (eventKey != null) {
      edits.add("event_key = ?");
    }

    if (edits.isEmpty()) {
      return getTeam(number);
    }

    String query = "UPDATE teams SET " + String.join(", ", edits) + " WHERE number = ? RETURNING *";
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(query)) {
      int index = 1;

      if (name != null) {
        statement.setString(index++, name);
      }

      if (eventKey != null) {
        statement.setString(index++, eventKey);
      }

      statement.setShort(index, (short) number);
      try (ResultSet resultSet = statement.executeQuery()) {
        return resultSet.next() ? Team.fromDatabase(resultSet) : null;
      }
    }
  }
}
