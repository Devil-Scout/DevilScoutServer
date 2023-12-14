package org.victorrobotics.devilscoutserver.tba.data;

import org.victorrobotics.bluealliance.Team.Simple;
import org.victorrobotics.devilscoutserver.tba.cache.ListCache;

import java.util.stream.IntStream;

public class EventTeamCache extends ListCache<String, Simple, EventTeam> {
  public EventTeamCache() {
    super(IntStream.range(0, 20)
                   .mapToObj(Simple::endpointForPage)
                   .toList());
  }

  @Override
  protected EventTeam createValue(String teamKey) {
    return new EventTeam(teamKey);
  }

  @Override
  protected String getKey(Simple data) {
    return data.key;
  }
}
