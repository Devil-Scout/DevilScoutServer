package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Event;

import java.util.List;

public class EventInfoCache extends BlueAllianceListCache<String, Event.Simple, EventInfo> {
  public EventInfoCache() {
    super(List.of(Event.Simple.endpointForYear(2023)));
  }

  @Override
  protected EventInfo createValue(String key, Event.Simple data) {
    return new EventInfo(key, data);
  }

  @Override
  protected String getKey(Event.Simple data) {
    return data.key;
  }
}
