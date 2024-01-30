package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Team;
import org.victorrobotics.devilscoutserver.cache.Cacheable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import com.fasterxml.jackson.annotation.JsonValue;

public class EventTeamList implements Cacheable<List<Team.Simple>> {
  public static class TeamInfo implements Cacheable<Team.Simple>, Comparable<TeamInfo> {
    private final int number;

    private String name;
    private String location;

    public TeamInfo(Team.Simple team) {
      this.number = team.number;
      update(team);
    }

    @Override
    public boolean update(Team.Simple team) {
      boolean changed = false;

      if (!Objects.equals(name, team.name)) {
        name = team.name;
        changed = true;
      }

      String teamLocation = team.city + ", " + team.province + ", " + team.country;
      if (!Objects.equals(location, teamLocation)) {
        location = teamLocation;
        changed = true;
      }

      return changed;
    }

    public int getNumber() {
      return number;
    }

    public String getName() {
      return name;
    }

    public String getLocation() {
      return location;
    }

    @Override
    @SuppressWarnings("java:S1210") // override equals too
    public int compareTo(TeamInfo other) {
      return Integer.compare(number, other.number);
    }
  }

  private final ConcurrentNavigableMap<String, TeamInfo> teamMap;
  private final Collection<TeamInfo>                     teams;

  public EventTeamList(List<Team.Simple> teams) {
    this.teamMap = new ConcurrentSkipListMap<>();
    this.teams = Collections.unmodifiableCollection(teamMap.values());
    update(teams);
  }

  @Override
  public boolean update(List<Team.Simple> teams) {
    boolean change = false;
    Collection<String> keys = new ArrayList<>();
    for (Team.Simple team : teams) {
      keys.add(team.key);

      TeamInfo info = teamMap.get(team.key);
      if (info == null) {
        teamMap.put(team.key, new TeamInfo(team));
        change = true;
      } else {
        change |= info.update(team);
      }
    }
    change |= teamMap.keySet()
                     .retainAll(keys);

    return change;
  }

  @JsonValue
  public Collection<TeamInfo> teams() {
    return teams;
  }
}
