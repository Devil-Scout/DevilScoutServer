package org.victorrobotics.devilscoutserver.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@SuppressWarnings("java:S2325")
public final class PitEntryDatabase extends Database {
  private static final String COUNT_ENTRIES_BY_SUBMITTING_TEAM         =
      "SELECT COUNT(*) FROM pit_entries WHERE submitting_team = ?";
  private static final String SELECT_ENTRIES_BY_SCOUTED_TEAM_AND_EVENT =
      "SELECT * FROM pit_entries WHERE scouted_team = ? AND event_key = ? ORDER BY id";

  private static final String COUNT_ENTRIES_BY_SUBMITTING_TEAM_AT_EVENT_UNIQUE_MATCHES =
      "SELECT COUNT(*) FROM pit_entries WHERE submitting_team = ? AND event_key = ?";

  private static final String INSERT_ENTRY = "INSERT INTO pit_entries "
      + "(event_key, submitting_user, submitting_team, scouted_team, data) "
      + "VALUES (?, ?, ?, ?, ?::JSON)";

  public PitEntryDatabase() {}

  public void createEntry(String eventKey, long submittingUser, int submittingTeam, int scoutedTeam,
                          String data)
      throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(INSERT_ENTRY)) {
      statement.setString(1, eventKey);
      statement.setLong(2, submittingUser);
      statement.setShort(3, (short) submittingTeam);
      statement.setShort(4, (short) scoutedTeam);
      statement.setObject(5, data);

      statement.execute();
    }
  }
}
