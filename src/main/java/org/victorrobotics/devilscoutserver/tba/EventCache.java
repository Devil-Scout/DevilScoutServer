package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Event.Simple;

import java.util.stream.IntStream;

public class EventCache extends BlueAllianceListCache<String, Simple, Event> {
  public EventCache() {
    super(IntStream.rangeClosed(2023, 2023)
                   .mapToObj(Simple::endpointForYear)
                   .toList());
  }

  @Override
  protected Event createValue(String key, Simple data) {
    return new Event(key, data);
  }

  @Override
  protected String getKey(Simple data) {
    return data.key;
  }
}
