package org.victorrobotics.devilscoutserver.years._2024;

import org.victorrobotics.bluealliance.Match;
import org.victorrobotics.devilscoutserver.tba.ScoreBreakdown;

import java.util.Collection;

public class CrescendoScoreBreakdown implements ScoreBreakdown {
  private final WltRecord wltRecord;

  public CrescendoScoreBreakdown(Match.ScoreBreakdown breakdown, Boolean wonMatch) {
    if (wonMatch == null) {
      this.wltRecord = new WltRecord(0, 0, 1);
    } else if (wonMatch) {
      this.wltRecord = new WltRecord(1, 0, 0);
    } else {
      this.wltRecord = new WltRecord(1, 1, 0);
    }
  }

  public CrescendoScoreBreakdown(Collection<CrescendoScoreBreakdown> matches) {
    int wins = 0;
    int losses = 0;
    int ties = 0;

    for (CrescendoScoreBreakdown match : matches) {
      WltRecord matchWlt = match.getWltRecord();
      wins += matchWlt.wins();
      losses += matchWlt.losses();
      ties += matchWlt.ties();
    }

    this.wltRecord = new WltRecord(wins, losses, ties);
  }

  @Override
  public Object getRankingPoints() {
    // TODO: implement for 2024
    return null;
  }

  @Override
  public WltRecord getWltRecord() {
    return wltRecord;
  }
}
