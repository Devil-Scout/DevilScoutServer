package org.victorrobotics.devilscoutserver.database;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public record Entry(String id,
                    long timestamp,
                    String eventKey,
                    String matchKey,
                    String submittingUser,
                    int submittingTeam,
                    int scoutedTeam,
                    String json) {

  private static final ObjectMapper PARSER = new ObjectMapper();

  public static Entry fromDatabase(ResultSet resultSet) throws SQLException {
    String id = resultSet.getString(1);
    long timestamp = resultSet.getLong(2);
    String eventKey = resultSet.getString(3);
    String submittingUser = resultSet.getString(4);
    int submittingTeam = resultSet.getShort(5);
    int scoutedTeam = resultSet.getShort(6);
    String json = resultSet.getString(7);
    return new Entry(id, timestamp, eventKey, null, submittingUser, submittingTeam, scoutedTeam,
                     json);
  }

  public static Entry fromDatabaseWithMatch(ResultSet resultSet) throws SQLException {
    String id = resultSet.getString(1);
    long timestamp = resultSet.getLong(2);
    String eventKey = resultSet.getString(3);
    String matchKey = resultSet.getString(4);
    String submittingUser = resultSet.getString(5);
    int submittingTeam = resultSet.getShort(6);
    int scoutedTeam = resultSet.getShort(7);
    String json = resultSet.getString(8);
    return new Entry(id, timestamp, eventKey, matchKey, submittingUser, submittingTeam, scoutedTeam,
                     json);
  }

  public JsonNode parseJson() throws JsonProcessingException {
    return PARSER.readTree(json);
  }
}
