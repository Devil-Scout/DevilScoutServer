package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.bluealliance.Event;

public class RankingsCache extends BlueAllianceCache<String, Event.Rankings, Rankings> {
  @Override
  protected Endpoint<Event.Rankings> getEndpoint(String key) {
    return Event.Rankings.endpointForEvent(key);
  }

  @Override
  protected Rankings createValue(String key, Event.Rankings data) {
    return new Rankings(data);
  }
}
