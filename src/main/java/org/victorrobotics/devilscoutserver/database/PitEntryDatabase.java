package org.victorrobotics.devilscoutserver.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@SuppressWarnings("java:S2325")
public final class PitEntryDatabase extends EntryDatabase {
  private static final String INSERT_ENTRY = "INSERT INTO pit_entries "
      + "(event_key, submitting_user, submitting_team, scouted_team, data) "
      + "VALUES (?, ?, ?, ?, ?::JSON)";

  public PitEntryDatabase() {
    super("pit_entries");
  }

  public void createEntry(String eventKey, String submittingUser, int submittingTeam, int scoutedTeam,
                          String data)
      throws SQLException {
    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(INSERT_ENTRY)) {
      statement.setString(1, eventKey);
      statement.setString(2, submittingUser);
      statement.setShort(3, (short) submittingTeam);
      statement.setShort(4, (short) scoutedTeam);
      statement.setObject(5, data);

      statement.execute();
    }
  }
}
