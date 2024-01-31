package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.bluealliance.Team;
import org.victorrobotics.devilscoutserver.cache.Cache;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EventTeamListCache
    extends BlueAllianceKeyCache<String, Team.Simple, TeamInfo, EventTeamList> {
  private final Cache<String, Team.Simple, TeamInfo> source;

  public EventTeamListCache(Cache<String, Team.Simple, TeamInfo> source) {
    super(TimeUnit.HOURS.toMillis(8));
    this.source = source;
  }

  @Override
  protected Endpoint<List<String>> getEndpoint(String eventKey) {
    return Team.Keys.endpointForEvent(eventKey);
  }

  @Override
  protected EventTeamList createValue(String eventKey, Collection<TeamInfo> data) {
    return new EventTeamList(data);
  }

  @Override
  protected TeamInfo sourceData(String key) {
    Value<Team.Simple, TeamInfo> value = source.get(key);
    return value == null ? null : value.value();
  }
}
