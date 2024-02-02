package org.victorrobotics.devilscoutserver.analysis.statistics;

import org.victorrobotics.devilscoutserver.tba.ScoreBreakdown;

public class RankingPointsStatistic extends Statistic {
  public final Object rankingPoints;

  public RankingPointsStatistic(String name, ScoreBreakdown stats) {
    super(StatisticType.RP, name);
    if (stats == null) {
      this.rankingPoints = null;
    } else {
      this.rankingPoints = stats.getRankingPoints();
    }
  }
}
