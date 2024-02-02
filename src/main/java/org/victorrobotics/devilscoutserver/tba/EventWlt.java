package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Event;
import org.victorrobotics.bluealliance.Event.WinLossRecord;
import org.victorrobotics.devilscoutserver.cache.Cacheable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class EventWlt implements Cacheable<Event.Rankings> {
  private final Map<Integer, WinLossRecord> teams;

  public EventWlt(Event.Rankings data) {
    this.teams = new LinkedHashMap<>();
    update(data);
  }

  @Override
  public boolean update(Event.Rankings data) {
    boolean mods = false;
    Collection<Integer> keys = new ArrayList<>();

    for (Event.Rankings.Team team : data.rankings) {
      int teamNum = teamNum(team.teamKey);
      keys.add(teamNum);

      WinLossRecord wlt = teams.get(teamNum);
      if (wlt == null || !wlt.equals(team.winLossRecord)) {
        teams.put(teamNum, team.winLossRecord);
        mods = true;
      }
    }

    mods |= teams.keySet()
                 .retainAll(keys);
    return mods;
  }

  public WinLossRecord get(int team) {
    return teams.get(team);
  }

  private static int teamNum(String teamKey) {
    return Integer.parseInt(teamKey.substring(3));
  }
}
