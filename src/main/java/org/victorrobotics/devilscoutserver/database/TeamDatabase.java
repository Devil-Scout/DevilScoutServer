package org.victorrobotics.devilscoutserver.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("java:S2325")
public final class TeamDatabase extends Database {
  private static final String SELECT_ALL_TEAMS      = "SELECT * FROM teams ORDER BY number";
  private static final String SELECT_TEAM_BY_NUMBER = "SELECT * FROM teams WHERE number = ?";
  private static final String COUNT_TEAM_BY_NUMBER  = "SELECT COUNT(*) FROM teams WHERE number = ?";

  private static final String INSERT_TEAM = "INSERT INTO teams (number, name) VALUES (?, ?)";
  private static final String DELETE_TEAM = "DELETE FROM teams WHERE number = ?";

  private static final String SELECT_ACTIVE_EVENTS = "SELECT DISTINCT event_key FROM teams";

  public TeamDatabase() {}

  public Collection<Team> allTeams() throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(SELECT_ALL_TEAMS);
         ResultSet resultSet = statement.executeQuery()) {
      return listFromDatabase(resultSet, Team::fromDatabase);
    }
  }

  public Team getTeam(int number) throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(SELECT_TEAM_BY_NUMBER)) {
      statement.setShort(1, (short) number);
      try (ResultSet resultSet = statement.executeQuery()) {
        return resultSet.next() ? Team.fromDatabase(resultSet) : null;
      }
    }
  }

  public boolean containsTeam(int number) throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(COUNT_TEAM_BY_NUMBER)) {
      statement.setShort(1, (short) number);
      try (ResultSet resultSet = statement.executeQuery()) {
        return resultSet.next() && resultSet.getInt("count") != 0;
      }
    }
  }

  public Team registerTeam(int number, String name) throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(INSERT_TEAM)) {
      statement.setShort(1, (short) number);
      statement.setString(2, name);
      try (ResultSet resultSet = statement.executeQuery()) {
        return resultSet.next() ? new Team(number, name, "") : null;
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

  public Set<String> getActiveEvents() throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(SELECT_ACTIVE_EVENTS);
         ResultSet resultSet = statement.executeQuery()) {
      Set<String> eventKeys = new LinkedHashSet<>();
      while (resultSet.next()) {
        eventKeys.add(resultSet.getString(1));
      }
      return eventKeys;
    }
  }
}
