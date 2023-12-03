package org.victorrobotics.devilscoutserver.tba.data;

import org.victorrobotics.bluealliance.Event;
import org.victorrobotics.devilscoutserver.tba.cache.ListCache;

import java.util.List;

public class EventInfoCache extends ListCache<String, Event.Simple, EventInfo> {
  public EventInfoCache() {
    super(List.of(Event.Simple.endpointForYear(2023)));
  }

  @Override
  protected EventInfo createValue(String key) {
    return new EventInfo(key);
  }

  @Override
  protected String getKey(Event.Simple data) {
    return data.key;
  }
}
