package org.victorrobotics.devilscoutserver.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("java:S2325")
public final class TeamDatabase extends Database {
  private static final String GET_TEAM      = "SELECT * FROM teams WHERE number = ?;";
  private static final String ALL_TEAMS     = "SELECT * FROM teams;";
  private static final String DELETE_TEAM   = "DELETE FROM teams WHERE number = ?;";
  private static final String REGISTER_TEAM = """
      INSERT INTO teams (number, name)
      VALUES (?, ?);""";

  public TeamDatabase() {}

  public Team getTeam(int number) throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(GET_TEAM)) {
      statement.setShort(1, (short) number);
      statement.execute();

      ResultSet resultSet = statement.getResultSet();
      return resultSet.next() ? Team.fromDatabase(resultSet) : null;
    }
  }

  public Team registerTeam(int number, String name) throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(REGISTER_TEAM)) {
      statement.setShort(1, (short) number);
      statement.setString(2, name);
      statement.execute();

      ResultSet resultSet = statement.getResultSet();
      return resultSet.next() ? Team.fromDatabase(resultSet) : null;
    }
  }

  public Team editTeam(int number, String name, String eventKey) throws SQLException {
    List<String> edits = new ArrayList<>();

    if (name != null) {
      edits.add("name = '" + name + "'");
    }

    if (eventKey != null) {
      if ("".equals(eventKey)) {
        edits.add("event_key = NULL");
      } else {
        edits.add("event_key = '" + eventKey + "'");
      }
    }

    if (edits.isEmpty()) {
      return getTeam(number);
    }

    String query = "UPDATE teams SET " + String.join(", ", edits) + " WHERE number = " + number
        + " RETURNING *;";
    try (Connection connection = getConnection();
         Statement statement = connection.createStatement()) {
      statement.execute(query);

      ResultSet resultSet = statement.getResultSet();
      return resultSet.next() ? Team.fromDatabase(resultSet) : null;
    }
  }

  public void deleteTeam(int number) throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(DELETE_TEAM)) {
      statement.setShort(1, (short) number);
      statement.execute();
    }
  }

  public Collection<Team> allTeams() throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(ALL_TEAMS)) {
      statement.execute();

      ResultSet resultSet = statement.getResultSet();
      List<Team> teams = new ArrayList<>();
      while (resultSet.next()) {
        teams.add(Team.fromDatabase(resultSet));
      }
      return List.copyOf(teams);
    }
  }
}
