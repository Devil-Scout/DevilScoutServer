package org.victorrobotics.devilscoutserver.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@SuppressWarnings("java:S2325")
public final class MatchEntryDatabase extends EntryDatabase {
  private static final String INSERT_ENTRY = "INSERT INTO match_entries "
      + "(event_key, match_key, submitting_user, submitting_team, scouted_team, data) "
      + "VALUES (?, ?, ?, ?, ?, ?::JSON)";

  public MatchEntryDatabase() {
    super("match_entries");
  }

  public void createEntry(String eventKey, String matchKey, String submittingUser, int submittingTeam,
                          int scoutedTeam, String data)
      throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(INSERT_ENTRY)) {
      statement.setString(1, eventKey);
      statement.setString(2, matchKey);
      statement.setString(3, submittingUser);
      statement.setShort(4, (short) submittingTeam);
      statement.setShort(5, (short) scoutedTeam);
      statement.setObject(6, data);

      statement.execute();
    }
  }
}
