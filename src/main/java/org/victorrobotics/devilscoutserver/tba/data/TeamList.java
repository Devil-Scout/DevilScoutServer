package org.victorrobotics.devilscoutserver.tba.data;

import org.victorrobotics.devilscoutserver.tba.cache.Cacheable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import com.fasterxml.jackson.annotation.JsonValue;

public class TeamList implements Cacheable<Collection<TeamInfo>> {
  private final ConcurrentNavigableMap<Integer, TeamInfo> teamMap;
  private final Collection<TeamInfo>         teams;

  public TeamList() {
    teamMap = new ConcurrentSkipListMap<>();
    teams = Collections.unmodifiableCollection(teamMap.values());
  }

  @Override
  public boolean update(Collection<TeamInfo> teams) {
    if (this.teams.equals(teams)) return false;

    boolean change = false;
    Collection<Integer> keys = new ArrayList<>();
    for (TeamInfo team : teams) {
      int key = team.getNumber();
      keys.add(key);

      TeamInfo info = teamMap.get(key);
      if (!team.equals(info)) {
        teamMap.put(key, team);
        change = true;
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
