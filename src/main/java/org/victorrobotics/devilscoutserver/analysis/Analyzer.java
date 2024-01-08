package org.victorrobotics.devilscoutserver.analysis;

import org.victorrobotics.devilscoutserver.analysis.statistics.Statistic;
import org.victorrobotics.devilscoutserver.database.DriveTeamEntryDatabase;
import org.victorrobotics.devilscoutserver.database.MatchEntryDatabase;
import org.victorrobotics.devilscoutserver.database.PitEntryDatabase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract sealed class Analyzer permits CrescendoAnalyzer {
  private static final ObjectMapper JSON = new ObjectMapper();

  protected abstract List<Statistic>
      computeStatistics(Map<String, List<Object>> matchSubmissions,
                        Map<String, List<Object>> pitSubmissions,
                        Map<String, List<Object>> driveTeamSubmissions);

  public List<Statistic> processTeam(int team, MatchEntryDatabase matchEntryDB,
                                     PitEntryDatabase pitEntryDB,
                                     DriveTeamEntryDatabase driveTeamEntryDB)
      throws SQLException, JsonProcessingException {
    List<String> matchJsons = matchEntryDB.getEntries(team);
    List<String> pitJsons = pitEntryDB.getEntries(team);
    List<String> driveTeamJsons = driveTeamEntryDB.getEntries(team);

    Map<String, List<Object>> matchData = new LinkedHashMap<>();
    for (String json : matchJsons) {
      Map<String, Map<String, Object>> data = JSON.readValue(json, Map.class);
      for (Map.Entry<String, Map<String, Object>> page : data.entrySet()) {
        String pageKey = page.getKey();
        for (Map.Entry<String, Object> response : page.getValue()
                                                      .entrySet()) {
          matchData.computeIfAbsent(pageKey + "/" + response.getKey(), s -> new ArrayList<>())
                   .add(response.getValue());
        }
      }
    }

    Map<String, List<Object>> pitData = new LinkedHashMap<>();
    for (String json : pitJsons) {
      Map<String, Map<String, Object>> data = JSON.readValue(json, Map.class);
      for (Map.Entry<String, Map<String, Object>> page : data.entrySet()) {
        String pageKey = page.getKey();
        for (Map.Entry<String, Object> response : page.getValue()
                                                      .entrySet()) {
          pitData.computeIfAbsent(pageKey + "/" + response.getKey(), s -> new ArrayList<>())
                 .add(response.getValue());
        }
      }
    }

    Map<String, List<Object>> driveTeamData = new LinkedHashMap<>();
    for (String json : driveTeamJsons) {
      Map<String, Object> data = JSON.readValue(json, Map.class);
      for (Map.Entry<String, Object> response : data.entrySet()) {
        driveTeamData.computeIfAbsent(response.getKey(), s -> new ArrayList<>())
                     .add(response.getValue());
      }
    }

    return computeStatistics(matchData, pitData, driveTeamData);
  }
}
