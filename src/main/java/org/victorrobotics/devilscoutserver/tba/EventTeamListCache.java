package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.bluealliance.Team.Keys;
import org.victorrobotics.bluealliance.Team.Simple;
import org.victorrobotics.devilscoutserver.cache.Cache;
import org.victorrobotics.devilscoutserver.cache.CacheValue;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EventTeamListCache
    extends BlueAllianceKeyCache<String, Simple, TeamInfo, EventTeamList> {
  private final Cache<String, Simple, TeamInfo> source;

  public EventTeamListCache(Cache<String, Simple, TeamInfo> source) {
    super(TimeUnit.HOURS.toMillis(8));
    this.source = source;
  }

  @Override
  protected Endpoint<List<String>> getEndpoint(String eventKey) {
    return Keys.endpointForEvent(eventKey);
  }

  @Override
  protected EventTeamList createValue(String eventKey, Collection<TeamInfo> data) {
    return new EventTeamList();
  }

  @Override
  protected TeamInfo sourceData(String key) {
    CacheValue<Simple, TeamInfo> value = source.get(key);
    return value == null ? null : value.value();
  }
}
