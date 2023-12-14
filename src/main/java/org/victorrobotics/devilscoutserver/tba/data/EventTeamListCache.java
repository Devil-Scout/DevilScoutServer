package org.victorrobotics.devilscoutserver.tba.data;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.bluealliance.Team.Simple;
import org.victorrobotics.bluealliance.Team.Keys;
import org.victorrobotics.devilscoutserver.tba.cache.Cache;
import org.victorrobotics.devilscoutserver.tba.cache.DependentKeyCache;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class EventTeamListCache
    extends DependentKeyCache<String, String, Simple, EventTeam, EventTeamList> {
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
