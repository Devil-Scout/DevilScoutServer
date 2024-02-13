package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.bluealliance.Team;

import java.util.List;

public class TeamListCache extends BlueAllianceCache<String, List<Team.Simple>, TeamList> {
  @Override
  protected Endpoint<List<Team.Simple>> getEndpoint(String eventKey) {
    return Team.Simple.endpointForEvent(eventKey);
  }

  @Override
  protected TeamList createValue(String eventKey, List<Team.Simple> data) {
    return new TeamList(data);
  }
}
