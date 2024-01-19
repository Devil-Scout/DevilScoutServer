package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.devilscoutserver.cache.Cacheable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import com.fasterxml.jackson.annotation.JsonValue;

public class EventTeamList implements Cacheable<Collection<TeamInfo>> {
  private final ConcurrentNavigableMap<Integer, TeamInfo> teamMap;
  private final Collection<TeamInfo>                      teams;

  public EventTeamList(Collection<TeamInfo> teams) {
    this.teamMap = new ConcurrentSkipListMap<>();
    this.teams = Collections.unmodifiableCollection(teamMap.values());
    update(teams);
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
