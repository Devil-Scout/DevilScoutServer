package org.victorrobotics.devilscoutserver.tba.data;

import org.victorrobotics.bluealliance.Event.Simple;
import org.victorrobotics.devilscoutserver.tba.cache.ListCache;

import java.util.List;

public class EventCache extends ListCache<String, Simple, Event> {
  public EventCache() {
    super(List.of(Simple.endpointForYear(2023)));
  }

  @Override
  protected Event createValue(String key) {
    return new Event(key);
  }

  @Override
  protected String getKey(Simple data) {
    return data.key;
  }
}
