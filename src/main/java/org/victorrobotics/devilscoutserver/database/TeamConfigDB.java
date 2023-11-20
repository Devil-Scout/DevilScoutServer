package org.victorrobotics.devilscoutserver.database;

import org.victorrobotics.devilscoutserver.data.TeamConfig;

import java.util.HashMap;
import java.util.Map;

public class TeamConfigDB {
  private final Map<Integer, TeamConfig> teams;

  public TeamConfigDB() {
    teams = new HashMap<>();
  }

  public TeamConfig get(int team) {
    return teams.get(team);
  }

  public void remove(int team) {
    teams.remove(team);
  }
}
