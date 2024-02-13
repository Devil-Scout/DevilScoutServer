package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.bluealliance.Event;
import org.victorrobotics.bluealliance.Event.Rankings;

import java.util.concurrent.TimeUnit;

public class EventRankingsCache extends BlueAllianceCache<String, Event.Rankings, EventRankings> {
  public EventRankingsCache() {
    super(TimeUnit.HOURS.toMillis(8));
  }

  @Override
  protected Endpoint<Rankings> getEndpoint(String key) {
    return Event.Rankings.endpointForEvent(key);
  }

  @Override
  protected EventRankings createValue(String key, Rankings data) {
    return new EventRankings(data);
  }
}
