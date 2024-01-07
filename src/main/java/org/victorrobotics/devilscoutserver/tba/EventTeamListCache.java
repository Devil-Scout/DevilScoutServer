package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.bluealliance.Team.Keys;
import org.victorrobotics.bluealliance.Team.Simple;
import org.victorrobotics.devilscoutserver.cache.Cache;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class EventTeamListCache
    extends BlueAllianceKeyCache<String, String, Simple, EventTeam, EventTeamList> {
  public EventTeamListCache(Cache<String, Simple, EventTeam> source) {
    super(source, TimeUnit.HOURS.toMillis(8));
  }

  @Override
  protected Endpoint<List<String>> getEndpoint(String eventKey) {
    return Keys.endpointForEvent(eventKey);
  }

  @Override
  protected EventTeamList createValue(String eventKey) {
    return new EventTeamList();
  }
}
