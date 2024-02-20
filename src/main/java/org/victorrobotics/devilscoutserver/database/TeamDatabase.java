package org.victorrobotics.devilscoutserver.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressWarnings("java:S2325")
public final class TeamDatabase extends Database {
  private static final String SELECT_TEAM_BY_NUMBER = "SELECT * FROM teams WHERE number = ?";
  private static final String SELECT_ACTIVE_EVENTS  = "SELECT DISTINCT event_key FROM teams";

  public TeamDatabase() {}

  public Team getTeam(int number) throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(SELECT_TEAM_BY_NUMBER)) {
      statement.setShort(1, (short) number);
      try (ResultSet resultSet = statement.executeQuery()) {
        return resultSet.next() ? Team.fromDatabase(resultSet) : null;
      }
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
      return setFromDatabase(resultSet, r -> r.getString(1));
    }
  }
}
