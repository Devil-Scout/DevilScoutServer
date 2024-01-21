package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.bluealliance.Match;
import org.victorrobotics.bluealliance.Match.Simple;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MatchScoresCache extends BlueAllianceCache<Integer, List<Match.Simple>, MatchScores> {
  public MatchScoresCache() {
    super(TimeUnit.HOURS.toMillis(8));
  }

  @Override
  protected Endpoint<List<Simple>> getEndpoint(Integer team) {
    return Match.Simple.endpointForTeam("frc" + team, 2023);
  }

  @Override
  protected MatchScores createValue(Integer key, List<Match.Simple> data) {
    return new MatchScores(key, data);
  }
}
