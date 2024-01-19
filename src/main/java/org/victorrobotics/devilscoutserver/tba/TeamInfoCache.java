package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Team;

import java.util.stream.IntStream;

public class TeamInfoCache extends BlueAllianceListCache<String, Team.Simple, TeamInfo> {
  public TeamInfoCache() {
    super(IntStream.range(0, 20)
                   .mapToObj(Team.Simple::endpointForPage)
                   .toList());
  }

  @Override
  protected TeamInfo createValue(String key, Team.Simple data) {
    return new TeamInfo(key, data);
  }

  @Override
  protected String getKey(Team.Simple data) {
    return data.key;
  }
}
