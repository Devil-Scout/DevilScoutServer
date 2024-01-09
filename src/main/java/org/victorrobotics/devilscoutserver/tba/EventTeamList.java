package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.devilscoutserver.cache.Cacheable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import com.fasterxml.jackson.annotation.JsonValue;

public class EventTeamList implements Cacheable<Collection<EventTeam>> {
  private final ConcurrentNavigableMap<Integer, EventTeam> teamMap;
  private final Collection<EventTeam>                      teams;

  public EventTeamList() {
    teamMap = new ConcurrentSkipListMap<>();
    teams = Collections.unmodifiableCollection(teamMap.values());
  }

  @Override
  public boolean update(Collection<EventTeam> teams) {
    if (this.teams.equals(teams)) return false;

    boolean change = false;
    Collection<Integer> keys = new ArrayList<>();
    for (EventTeam team : teams) {
      int key = team.getNumber();
      keys.add(key);

      EventTeam info = teamMap.get(key);
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
  public Collection<EventTeam> teams() {
    return teams;
  }
}
