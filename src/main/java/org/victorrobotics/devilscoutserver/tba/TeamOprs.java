package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.devilscoutserver.cache.Cacheable;
import org.victorrobotics.devilscoutserver.tba.EventOprs.EventTeamOprs;

import java.util.LinkedHashMap;
import java.util.Map;

public class TeamOprs implements Cacheable<Map<String, EventTeamOprs>> {
  private final Map<String, EventTeamOprs> eventOprs;

  public TeamOprs(Map<String, EventTeamOprs> data) {
    eventOprs = new LinkedHashMap<>(data);
  }

  @Override
  public boolean update(Map<String, EventTeamOprs> data) {
    boolean mods = eventOprs.keySet()
                            .retainAll(data.keySet());

    for (Map.Entry<String, EventTeamOprs> entry : data.entrySet()) {
      String key = entry.getKey();
      EventTeamOprs value = entry.getValue();

      EventTeamOprs current = eventOprs.get(key);
      if (!value.equals(current)) {
        eventOprs.put(key, value);
        mods = true;
      }
    }
    return mods;
  }
}
