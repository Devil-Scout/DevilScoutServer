package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.bluealliance.Match;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MatchScheduleCache
    extends BlueAllianceCache<String, List<Match.Simple>, MatchSchedule> {
  public MatchScheduleCache() {
    super(TimeUnit.HOURS.toMillis(8));
  }

  @Override
  protected Endpoint<List<Match.Simple>> getEndpoint(String eventKey) {
    return Match.Simple.endpointForEvent(eventKey);
  }

  @Override
  protected MatchSchedule createValue(String key, List<Match.Simple> data) {
    return new MatchSchedule(data);
  }
}
