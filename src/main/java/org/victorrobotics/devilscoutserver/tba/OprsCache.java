package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.bluealliance.Event;
import org.victorrobotics.bluealliance.Event.OPRs;

public class OprsCache extends BlueAllianceCache<String, OPRs, Oprs> {
  @Override
  protected Endpoint<OPRs> getEndpoint(String eventKey) {
    return Event.OPRs.endpointForEvent(eventKey);
  }

  @Override
  protected Oprs createValue(String key, OPRs data) {
    return new Oprs(data);
  }
}
