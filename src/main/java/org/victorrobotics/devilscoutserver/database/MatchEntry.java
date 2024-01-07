package org.victorrobotics.devilscoutserver.database;

import java.sql.ResultSet;
import java.sql.SQLException;

public record MatchEntry(long id,
                         long timestamp,
                         String eventKey,
                         String matchKey,
                         long submittingUser,
                         int submittingTeam,
                         int scoutedTeam,
                         String data) {
  public static MatchEntry fromDatabase(ResultSet resultSet) throws SQLException {
    long id = resultSet.getLong(1);
    long timestamp = resultSet.getLong(2);
    String eventKey = resultSet.getString(3);
    String matchKey = resultSet.getString(4);
    long submittingUser = resultSet.getLong(5);
    int submittingTeam = resultSet.getShort(6);
    int scoutedTeam = resultSet.getShort(7);
    String data = resultSet.getString(8);
    return new MatchEntry(id, timestamp, eventKey, matchKey, submittingUser, submittingTeam, scoutedTeam,
                          data);
  }
}
