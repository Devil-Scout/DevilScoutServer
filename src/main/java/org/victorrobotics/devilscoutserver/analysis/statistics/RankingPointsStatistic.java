package org.victorrobotics.devilscoutserver.analysis.statistics;

import org.victorrobotics.devilscoutserver.analysis.AnalysisData;

import java.util.Map;

public class RankingPointsStatistic extends Statistic {
  public final Map<String, Integer> rankingPoints;

  public RankingPointsStatistic(AnalysisData data) {
    super(StatisticType.RP, "Ranking Points");
    this.rankingPoints = data == null ? Map.of() : data.rankingPoints();
  }
}
