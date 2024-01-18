package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Event.Simple;

import java.util.List;

public class EventInfoCache extends BlueAllianceListCache<String, Simple, EventInfo> {
  public EventInfoCache() {
    super(List.of(Simple.endpointForYear(2023)));
  }

  @Override
  protected EventInfo createValue(String key, Simple data) {
    return new EventInfo(key);
  }

  @Override
  protected String getKey(Simple data) {
    return data.key;
  }
}
