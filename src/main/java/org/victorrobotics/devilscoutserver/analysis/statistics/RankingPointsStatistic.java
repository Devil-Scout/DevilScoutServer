package org.victorrobotics.devilscoutserver.analysis.statistics;

import org.victorrobotics.devilscoutserver.tba.ScoreBreakdown;

public class RankingPointsStatistic extends Statistic {
  public final Object rankingPoints;

  public RankingPointsStatistic(ScoreBreakdown stats) {
    super(StatisticType.RP, "Ranking Points");
    if (stats == null) {
      this.rankingPoints = null;
    } else {
      this.rankingPoints = stats.getRankingPoints();
    }
  }
}
