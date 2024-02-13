package org.victorrobotics.devilscoutserver.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public record DataEntry(String id,
                        String eventKey,
                        String matchKey,
                        String submittingUser,
                        int submittingTeam,
                        int scoutedTeam,
                        JsonNode json,
                        long timestamp) {
  private static final ObjectMapper PARSER = new ObjectMapper();

  public Integer getInteger(String path) {
    JsonNode node = json.at(path);
    return node.isNumber() ? node.intValue() : null;
  }

  public Boolean getBoolean(String path) {
    JsonNode node = json.at(path);
    return node.isBoolean() ? node.booleanValue() : null;
  }

  @SuppressWarnings("java:S1168") // empty list instead of null
  public List<Integer> getIntegers(String path) {
    JsonNode node = json.at(path);
    if (node.isNumber()) return List.of(node.intValue());
    if (!node.isArray()) return null;

    List<Integer> values = new ArrayList<>(node.size());
    for (int i = 0; i < node.size(); i++) {
      JsonNode node2 = node.get(i);
      if (!node2.isNumber()) return null;
      values.add(node2.intValue());
    }
    return values;
  }

  public static DataEntry fromDatabase(ResultSet resultSet) throws SQLException {
    JsonNode json;
    try {
      String jsonStr = resultSet.getString(6);
      json = PARSER.readTree(jsonStr);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }

    String id = resultSet.getString(1);
    String eventKey = resultSet.getString(2);
    String submittingUser = resultSet.getString(3);
    int submittingTeam = resultSet.getShort(4);
    int scoutedTeam = resultSet.getShort(5);
    long timestamp = resultSet.getTimestamp(7)
                              .getTime();

    return new DataEntry(id, eventKey, null, submittingUser, submittingTeam, scoutedTeam, json,
                         timestamp);
  }

  public static DataEntry fromDatabaseWithMatch(ResultSet resultSet) throws SQLException {
    JsonNode json;
    try {
      String jsonStr = resultSet.getString(7);
      json = PARSER.readTree(jsonStr);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }

    String id = resultSet.getString(1);
    String eventKey = resultSet.getString(2);
    String matchKey = resultSet.getString(3);
    String submittingUser = resultSet.getString(4);
    int submittingTeam = resultSet.getShort(5);
    int scoutedTeam = resultSet.getShort(6);
    long timestamp = resultSet.getTimestamp(8)
                              .getTime();

    return new DataEntry(id, eventKey, matchKey, submittingUser, submittingTeam, scoutedTeam, json,
                         timestamp);
  }
}
