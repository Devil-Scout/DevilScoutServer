package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.bluealliance.Match;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MatchScheduleCache extends BlueAllianceCache<String, List<Match>, MatchSchedule> {
  public MatchScheduleCache() {
    super(TimeUnit.HOURS.toMillis(8));
  }

  @Override
  protected Endpoint<List<Match>> getEndpoint(String eventKey) {
    return Match.endpointForEvent(eventKey);
  }

  @Override
  protected MatchSchedule createValue(String key, List<Match> data) {
    return new MatchSchedule(data);
  }
}
