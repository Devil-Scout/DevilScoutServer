package org.victorrobotics.devilscoutserver.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

  public Integer getInteger(String path) {
    JsonNode node = json.at(path);
    return node.isNumber() ? node.intValue() : null;
  }

  public Boolean getBoolean(String path) {
    JsonNode node = json.at(path);
    return node.isBoolean() ? node.booleanValue() : null;

  }

  public List<Integer> getIntegers(String path) {
    JsonNode node = json.at(path);
    if (node.isNumber()) return List.of(node.intValue());
    if (!node.isArray()) return null;

    List<Integer> values = new ArrayList<>(node.size());
    for (int i = 0; i < node.size(); i++) {
      JsonNode node2 = node.get(i);
      if (!node2.isNumber()) {
        return null;
      }
      values.add(node2.intValue());
    }
    return values;
  }

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
