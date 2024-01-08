package org.victorrobotics.devilscoutserver.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("java:S2325")
public final class DriveTeamEntryDatabase extends Database {
  private static final String SELECT_JSON_BY_TEAM_AND_YEAR =
      "SELECT data FROM drive_team_entries WHERE scouted_team = ? AND timestamp >= '2024-1-1'";

  private static final String INSERT_ENTRY = "INSERT INTO drive_team_entries "
      + "(event_key, match_key, submitting_user, submitting_team, scouted_team, data) "
      + "VALUES (?, ?, ?, ?, ?, ?::JSON)";

  public DriveTeamEntryDatabase() {}

  public void createEntry(String eventKey, String matchKey, long submittingUser, int submittingTeam,
                          int scoutedTeam, String data)
      throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(INSERT_ENTRY)) {
      statement.setString(1, eventKey);
      statement.setString(2, matchKey);
      statement.setLong(3, submittingUser);
      statement.setShort(4, (short) submittingTeam);
      statement.setShort(5, (short) scoutedTeam);
      statement.setObject(6, data);

      statement.execute();
    }
  }

  public List<String> getEntries(int scoutedTeam) throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement =
             connection.prepareStatement(SELECT_JSON_BY_TEAM_AND_YEAR)) {
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
}
