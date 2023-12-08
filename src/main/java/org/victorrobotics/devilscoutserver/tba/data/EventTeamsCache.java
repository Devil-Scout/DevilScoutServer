package org.victorrobotics.devilscoutserver.tba.data;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.bluealliance.Team;
import org.victorrobotics.devilscoutserver.tba.cache.Cache;
import org.victorrobotics.devilscoutserver.tba.cache.DependentKeyCache;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class EventTeamsCache
    extends DependentKeyCache<String, String, Team.Simple, TeamInfo, EventTeams> {
  public EventTeamsCache(Cache<String, Team.Simple, TeamInfo> source) {
    super(source, TimeUnit.HOURS.toMillis(8));
  }

  @Override
  protected Endpoint<List<String>> getEndpoint(String eventKey) {
    return Team.Keys.endpointForEvent(eventKey);
  }

  @Override
  protected EventTeams createValue(String eventKey) {
    return new EventTeams();
  }
}
