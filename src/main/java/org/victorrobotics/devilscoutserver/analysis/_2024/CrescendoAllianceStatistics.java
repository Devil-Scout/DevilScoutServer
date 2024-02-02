package org.victorrobotics.devilscoutserver.analysis._2024;

import org.victorrobotics.bluealliance.Match;
import org.victorrobotics.devilscoutserver.tba.AllianceStatistics;

import java.util.Collection;

public class CrescendoAllianceStatistics implements AllianceStatistics {
  public final int x;

  public CrescendoAllianceStatistics(Match.ScoreBreakdown breakdown) {
    x = 1;
  }

  public CrescendoAllianceStatistics(Collection<CrescendoAllianceStatistics> matches) {
    x = matches.size();
  }

  @Override
  public Object getRankingPoints() {
    // TODO: implement for 2024
    return x;
  }
}
