package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.bluealliance.Event;

import java.util.concurrent.TimeUnit;

public class EventWltCache extends BlueAllianceCache<String, Event.Rankings, EventWlt> {
  public EventWltCache() {
    super(TimeUnit.HOURS.toMillis(8));
  }

  @Override
  protected Endpoint<Event.Rankings> getEndpoint(String key) {
    return Event.Rankings.endpointForEvent(key);
  }

  @Override
  protected EventWlt createValue(String key, Event.Rankings data) {
    return new EventWlt(data);
  }
}
