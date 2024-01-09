package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Event.Simple;

import java.util.List;

public class EventCache extends BlueAllianceListCache<String, Simple, Event> {
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
