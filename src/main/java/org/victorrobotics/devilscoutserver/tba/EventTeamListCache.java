package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.bluealliance.Team;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class EventTeamListCache extends BlueAllianceCache<String, List<Team.Simple>, EventTeamList> {
  public EventTeamListCache() {
    super(TimeUnit.HOURS.toMillis(8));
  }

  @Override
  protected Endpoint<List<Team.Simple>> getEndpoint(String eventKey) {
    return Team.Simple.endpointForEvent(eventKey);
  }

  @Override
  protected EventTeamList createValue(String eventKey, List<Team.Simple> data) {
    return new EventTeamList(data);
  }
}
