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
                    JsonNode json) {

  private static final ObjectMapper PARSER = new ObjectMapper();

  public static Entry fromDatabase(ResultSet resultSet) throws SQLException {
    JsonNode json;
    try {
      String jsonStr = resultSet.getString(7);
      json = PARSER.readTree(jsonStr);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }

    String id = resultSet.getString(1);
    long timestamp = resultSet.getTimestamp(2)
                              .getTime();
    String eventKey = resultSet.getString(3);
    String submittingUser = resultSet.getString(4);
    int submittingTeam = resultSet.getShort(5);
    int scoutedTeam = resultSet.getShort(6);

    return new Entry(id, timestamp, eventKey, null, submittingUser, submittingTeam, scoutedTeam,
                     json);
  }

  public static Entry fromDatabaseWithMatch(ResultSet resultSet) throws SQLException {
    JsonNode json;
    try {
      String jsonStr = resultSet.getString(8);
      json = PARSER.readTree(jsonStr);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }

    String id = resultSet.getString(1);
    long timestamp = resultSet.getTimestamp(2)
                              .getTime();
    String eventKey = resultSet.getString(3);
    String matchKey = resultSet.getString(4);
    String submittingUser = resultSet.getString(5);
    int submittingTeam = resultSet.getShort(6);
    int scoutedTeam = resultSet.getShort(7);

    return new Entry(id, timestamp, eventKey, matchKey, submittingUser, submittingTeam, scoutedTeam,
                     json);
  }
}
