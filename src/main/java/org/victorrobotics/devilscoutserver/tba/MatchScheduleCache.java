package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.bluealliance.Match;

import java.util.List;

public class MatchScheduleCache extends BlueAllianceCache<String, List<Match>, MatchSchedule> {
  @Override
  protected Endpoint<List<Match>> getEndpoint(String eventKey) {
    return Match.endpointForEvent(eventKey);
  }

  @Override
  protected MatchSchedule createValue(String key, List<Match> data) {
    return new MatchSchedule(data);
  }
}
