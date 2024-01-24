package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.bluealliance.Event;
import org.victorrobotics.bluealliance.Event.OPRs;

import java.util.concurrent.TimeUnit;

public class EventOprsCache extends BlueAllianceCache<String, OPRs, EventOprs> {
  public EventOprsCache() {
    super(TimeUnit.HOURS.toMillis(8));
  }

  @Override
  protected Endpoint<OPRs> getEndpoint(String eventKey) {
    return Event.OPRs.endpointForEvent(eventKey);
  }

  @Override
  protected EventOprs createValue(String key, OPRs data) {
    return new EventOprs(key, data);
  }
}
