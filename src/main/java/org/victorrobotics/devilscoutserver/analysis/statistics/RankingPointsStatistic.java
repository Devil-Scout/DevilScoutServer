package org.victorrobotics.devilscoutserver.analysis.statistics;

import java.util.Map;

public class RankingPointsStatistic extends Statistic {
  public final Map<String, Integer> rankingPoints;

  public RankingPointsStatistic(Map<String, Integer> rankingPoints) {
    super(StatisticType.RP, "Ranking Points");
    this.rankingPoints = rankingPoints == null ? Map.of() : rankingPoints;
  }
}
