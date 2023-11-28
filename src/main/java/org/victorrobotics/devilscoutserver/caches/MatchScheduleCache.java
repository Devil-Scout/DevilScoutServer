package org.victorrobotics.devilscoutserver.caches;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.bluealliance.Match;
import org.victorrobotics.bluealliance.Match.Simple;
import org.victorrobotics.devilscoutserver.cache.IndividualCache;
import org.victorrobotics.devilscoutserver.data.MatchSchedule;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MatchScheduleCache extends IndividualCache<String, List<Match.Simple>, MatchSchedule> {
  public MatchScheduleCache() {
    super(TimeUnit.HOURS.toMillis(8));
  }

  @Override
  protected Endpoint<List<Simple>> getEndpoint(String eventKey) {
    return Match.Simple.endpointForEvent(eventKey);
  }

  @Override
  protected MatchSchedule createValue(String key) {
    return new MatchSchedule();
  }
}
