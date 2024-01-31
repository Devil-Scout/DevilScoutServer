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

  public Set<DataEntry.Key> getEntryKeysSince(long timestamp) throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement =
             connection.prepareStatement(selectNewEntriesEventAndTeam())) {
      statement.setLong(1, timestamp);

      try (ResultSet resultSet = statement.executeQuery()) {
        Set<DataEntry.Key> keys = new LinkedHashSet<>();
        while (resultSet.next()) {
          keys.add(DataEntry.Key.fromDatabase(resultSet));
        }
        return keys;
      }
    }
  }

  public List<DataEntry> getEntries(String eventKey, int scoutedTeam) throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(selectEntriesByTeamAndYear())) {
      statement.setShort(1, (short) scoutedTeam);
      statement.setString(2, eventKey);

      try (ResultSet resultSet = statement.executeQuery()) {
        List<DataEntry> entries = new ArrayList<>();
        if (hasMatchKeys) {
          while (resultSet.next()) {
            entries.add(DataEntry.fromDatabaseWithMatch(resultSet));
          }
        } else {
          while (resultSet.next()) {
            entries.add(DataEntry.fromDatabase(resultSet));
          }
        }
        return entries;
      }
    }
  }

  private String insertEntry() {
    return "INSERT INTO " + databaseName + " (event_key, " + (hasMatchKeys ? "match_key, " : "")
        + "submitting_user, submitting_team, scouted_team, data) " + "VALUES ("
        + (hasMatchKeys ? "?, " : "") + "?, ?, ?, ?, ?::JSON)";
  }

  private String selectEntriesByTeamAndYear() {
    return "SELECT * FROM " + databaseName + " WHERE scouted_team = ? AND event_key = ?";
  }

  private String selectNewEntriesEventAndTeam() {
    return "SELECT DISTINCT event_key, scouted_team FROM " + databaseName
        + " WHERE timestamp >= TO_TIMESTAMP(? / 1000.0);";
  }
}
