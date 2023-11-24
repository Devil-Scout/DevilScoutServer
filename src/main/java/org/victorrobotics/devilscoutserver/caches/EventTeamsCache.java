package org.victorrobotics.devilscoutserver.caches;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.bluealliance.Team;
import org.victorrobotics.devilscoutserver.cache.DependentKeyCache;
import org.victorrobotics.devilscoutserver.data.TeamInfo;
import org.victorrobotics.devilscoutserver.data.TeamList;

import java.util.List;

public class EventTeamsCache
    extends DependentKeyCache<String, String, Team.Simple, TeamInfo, TeamList> {
  public EventTeamsCache(TeamInfoCache source) {
    super(source);
  }

  @Override
  protected Endpoint<List<String>> getEndpoint(String eventKey) {
    return Team.Keys.endpointForEvent(eventKey);
  }

  @Override
  protected TeamList createValue(String eventKey) {
    return new TeamList();
  }
}
