package org.victorrobotics.devilscoutserver.database;

import org.victorrobotics.devilscoutserver.data.TeamConfig;

import java.util.HashMap;
import java.util.Map;

public class TeamConfigDB {
  private final Map<Integer, TeamConfig> teams;

  public TeamConfigDB() {
    teams = new HashMap<>();
    teams.put(1559, new TeamConfig(1559, "Devil Tech"));
    teams.get(1559).setEventKey("2023nyrr");
  }

  public TeamConfig get(int team) {
    return teams.get(team);
  }

  public TeamConfig getOrCreate(int team, String name) {
    return teams.computeIfAbsent(team, x -> new TeamConfig(team, name));
  }

  public void destroy(int team) {
    teams.remove(team);
  }
}
