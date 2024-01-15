package org.victorrobotics.devilscoutserver.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class EntryDatabase extends Database {
  private final String  databaseName;
  private final boolean hasMatchKeys;

  public EntryDatabase(String name, boolean hasMatchKeys) {
    this.databaseName = name;
    this.hasMatchKeys = hasMatchKeys;
  }

  public void createEntry(String eventKey, String matchKey, String submittingUser,
                          int submittingTeam, int scoutedTeam, String json)
      throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(insertEntry())) {
      int index = 1;
      statement.setString(index++, eventKey);
      if (hasMatchKeys) {
        statement.setString(index++, matchKey);
      }
      statement.setString(index++, submittingUser);
      statement.setShort(index++, (short) submittingTeam);
      statement.setShort(index++, (short) scoutedTeam);
      statement.setObject(index++, json);

      statement.execute();
    }
  }

  public Set<Integer> getTeamsSince(long timestamp) throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(selectTeamsUpdatedSinceTime())) {
      statement.setLong(1, timestamp);

      try (ResultSet resultSet = statement.executeQuery()) {
        Set<Integer> teams = new LinkedHashSet<>();
        while (resultSet.next()) {
          teams.add((int) resultSet.getShort(1));
        }
        return teams;
      }
    }
  }

  public List<Entry> getEntries(int scoutedTeam) throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(selectEntriesByTeamAndYear())) {
      statement.setShort(1, (short) scoutedTeam);

      try (ResultSet resultSet = statement.executeQuery()) {
        List<Entry> entries = new ArrayList<>();
        if (hasMatchKeys) {
          while (resultSet.next()) {
            entries.add(Entry.fromDatabaseWithMatch(resultSet));
          }
        } else {
          while (resultSet.next()) {
            entries.add(Entry.fromDatabase(resultSet));
          }
        }
        return entries;
      }
    }
  }

  private String insertEntry() {
    return "INSERT INTO " + databaseName + " (event_key, " + (hasMatchKeys ? "match_key, " : "")
        + "submitting_user, submitting_team, scouted_team, data) " + "VALUES ("
        + (hasMatchKeys ? "?, " : "") + "?, ?, ?, ?, ?, ?::JSON)";
  }

  private String selectEntriesByTeamAndYear() {
    return "SELECT * FROM " + databaseName + " WHERE scouted_team = ? AND timestamp >= '2024-1-1'";
  }

  private String selectTeamsUpdatedSinceTime() {
    return "SELECT DISTINCT scouted_team FROM " + databaseName
        + " WHERE timestamp >= TO_TIMESTAMP(?::double precision / 1000);";
  }
}
