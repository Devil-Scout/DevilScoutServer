package org.victorrobotics.devilscoutserver.analysis;

import org.victorrobotics.devilscoutserver.database.DriveTeamEntryDatabase;
import org.victorrobotics.devilscoutserver.database.MatchEntryDatabase;
import org.victorrobotics.devilscoutserver.database.PitEntryDatabase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract sealed class Analyzer permits CrescendoAnalyzer {
  private static final ObjectMapper JSON = new ObjectMapper();

  private final MatchEntryDatabase     matchEntryDB;
  private final PitEntryDatabase       pitEntryDB;
  private final DriveTeamEntryDatabase driveTeamEntryDB;

  protected Analyzer(MatchEntryDatabase matchEntryDB, PitEntryDatabase pitEntryDB,
                     DriveTeamEntryDatabase driveTeamEntryDB) {
    this.matchEntryDB = matchEntryDB;
    this.pitEntryDB = pitEntryDB;
    this.driveTeamEntryDB = driveTeamEntryDB;
  }

  protected abstract List<Statistic>
      computeStatistics(Map<String, List<Object>> matchSubmissions,
                        Map<String, List<Object>> pitSubmissions,
                        Map<String, List<Object>> driveTeamSubmissions);

  public Set<Integer> getTeamsToUpdate(long lastUpdate) throws SQLException {
    Set<Integer> teams = new LinkedHashSet<>();
    teams.addAll(matchEntryDB.getTeamsSince(lastUpdate));
    teams.addAll(pitEntryDB.getTeamsSince(lastUpdate));
    teams.addAll(driveTeamEntryDB.getTeamsSince(lastUpdate));
    return teams;
  }

  @SuppressWarnings("unchecked")
  public List<Statistic> processTeam(int team) {
    List<String> matchJsons;
    List<String> pitJsons;
    List<String> driveTeamJsons;
    try {
      matchJsons = matchEntryDB.getEntries(team);
      pitJsons = pitEntryDB.getEntries(team);
      driveTeamJsons = driveTeamEntryDB.getEntries(team);
    } catch (SQLException e) {
      throw new IllegalStateException(e);
    }

    Map<String, List<Object>> matchData = new LinkedHashMap<>();
    for (String json : matchJsons) {
      Map<String, Map<String, Object>> data;
      try {
        data = JSON.readValue(json, Map.class);
      } catch (JsonProcessingException e) {
        throw new IllegalStateException(e);
      }

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
      Map<String, Map<String, Object>> data;
      try {
        data = JSON.readValue(json, Map.class);
      } catch (JsonProcessingException e) {
        throw new IllegalStateException(e);
      }

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
      Map<String, Object> data;
      try {
        data = JSON.readValue(json, Map.class);
      } catch (JsonProcessingException e) {
        throw new IllegalStateException(e);
      }

      for (Map.Entry<String, Object> response : data.entrySet()) {
        driveTeamData.computeIfAbsent(response.getKey(), s -> new ArrayList<>())
                     .add(response.getValue());
      }
    }

    return computeStatistics(matchData, pitData, driveTeamData);
  }
}
