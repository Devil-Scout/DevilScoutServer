package org.victorrobotics.devilscoutserver.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@SuppressWarnings("java:S2325")
public final class MatchEntryDatabase extends Database {
  private static final String COUNT_ENTRIES_BY_SUBMITTING_TEAM         =
      "SELECT COUNT(*) FROM match_entries WHERE submitting_team = ?";
  private static final String SELECT_ENTRIES_BY_SCOUTED_TEAM_AND_EVENT =
      "SELECT * FROM match_entries WHERE scouted_team = ? AND event_key = ? ORDER BY id";

  private static final String COUNT_ENTRIES_BY_SUBMITTING_TEAM_AT_EVENT_UNIQUE_MATCHES =
      "SELECT COUNT(*) FROM match_entries WHERE submitting_team = ? AND event_key = ?";

  private static final String INSERT_ENTRY = "INSERT INTO match_entries "
      + "(event_key, match_key, submitting_user, submitting_team, scouted_team, data) "
      + "VALUES (?, ?, ?, ?, ?, ?::JSON)";

  public MatchEntryDatabase() {}

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
}
