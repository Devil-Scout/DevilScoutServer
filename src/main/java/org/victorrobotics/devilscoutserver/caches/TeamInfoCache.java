package org.victorrobotics.devilscoutserver.caches;

import org.victorrobotics.bluealliance.Team;
import org.victorrobotics.devilscoutserver.cache.ListCache;
import org.victorrobotics.devilscoutserver.data.TeamInfo;

import java.util.stream.IntStream;

public class TeamInfoCache extends ListCache<String, Team.Simple, TeamInfo> {
  public TeamInfoCache() {
    super(IntStream.range(0, 20)
                   .mapToObj(Team.Simple::endpointForPage)
                   .toList());
  }

  @Override
  protected TeamInfo createValue(String teamKey) {
    return new TeamInfo(teamKey);
  }

  @Override
  protected String getKey(Team.Simple data) {
    return data.key;
  }
}
