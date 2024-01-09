package org.victorrobotics.devilscoutserver.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public sealed class EntryDatabase extends Database
    permits MatchEntryDatabase, PitEntryDatabase, DriveTeamEntryDatabase {
  protected final String databaseName;

  public EntryDatabase(String name) {
    this.databaseName = name;
  }

  public Set<Integer> getTeamsSince(long timestamp) throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement =
             connection.prepareStatement(selectTeamsUpdatedSinceTime(databaseName))) {
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

  public List<String> getEntries(int scoutedTeam) throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement =
             connection.prepareStatement(selectJsonByTeamAndYear(databaseName))) {
      statement.setShort(1, (short) scoutedTeam);

      try (ResultSet resultSet = statement.executeQuery()) {
        List<String> entries = new ArrayList<>();
        while (resultSet.next()) {
          entries.add(resultSet.getString(1));
        }
        return entries;
      }
    }
  }

  private static String selectJsonByTeamAndYear(String databaseName) {
    return "SELECT data FROM " + databaseName
        + " WHERE scouted_team = ? AND timestamp >= '2024-1-1'";
  }

  private static String selectTeamsUpdatedSinceTime(String databaseName) {
    return "SELECT DISTINCT scouted_team FROM " + databaseName
        + " WHERE timestamp >= TO_TIMESTAMP(?::double precision / 1000);";
  }
}
