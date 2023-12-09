package org.victorrobotics.devilscoutserver.database;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class TeamDB {
  private final Map<Integer, Team> teams;

  public TeamDB() {
    teams = new ConcurrentHashMap<>();
  }

  public Team get(int teamNum) {
    return teams.get(teamNum);
  }

  public void put(Team team) {
    teams.put(team.getNumber(), team);
  }

  public void remove(Team team) {
    teams.remove(team.getNumber());
  }

  public Stream<Team> teams() {
    return teams.values()
                .stream()
                .sorted();
  }
}
