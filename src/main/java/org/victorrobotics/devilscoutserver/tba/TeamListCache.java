package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Endpoint;
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

public class TeamListCache
    extends BlueAllianceCache<String, List<Team.Simple>, TeamListCache.TeamList> {
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

  public static class TeamList implements Cacheable<List<Team.Simple>> {
    private final ConcurrentNavigableMap<Integer, TeamInfo> teamMap;
    private final Collection<TeamInfo>                      teams;

    public TeamList(List<Team.Simple> teams) {
      this.teamMap = new ConcurrentSkipListMap<>();
      this.teams = Collections.unmodifiableCollection(teamMap.values());
      update(teams);
    }

    @Override
    public boolean update(List<Team.Simple> teams) {
      boolean change = false;
      Collection<Integer> keys = new ArrayList<>();
      for (Team.Simple team : teams) {
        keys.add(team.number);

        TeamInfo info = teamMap.get(team.number);
        if (info == null) {
          teamMap.put(team.number, new TeamInfo(team));
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

  @Override
  protected Endpoint<List<Team.Simple>> getEndpoint(String eventKey) {
    return Team.Simple.endpointForEvent(eventKey);
  }

  @Override
  protected TeamList createValue(String eventKey, List<Team.Simple> data) {
    return new TeamList(data);
  }
}