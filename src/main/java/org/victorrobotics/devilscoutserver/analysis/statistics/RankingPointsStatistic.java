package org.victorrobotics.devilscoutserver.analysis.statistics;

import java.util.Map;

public class RankingPointsStatistic extends Statistic {
  public final Map<String, Integer> points;

  public RankingPointsStatistic(Map<String, Integer> rankingPoints) {
    super(StatisticType.RP, "Ranking Points");
    this.points = rankingPoints;
  }
}
