package org.victorrobotics.devilscoutserver.analysis.statistics;

import java.util.Map;

public final class PieChartStatistic extends Statistic {
  public static record LabeledCount(String label,
                                    int count) {}

  public final Map<Object, Integer> slices;

  public PieChartStatistic(String name) {
    super(StatisticType.PIE_CHART, name);
    this.slices = null;
  }

  public PieChartStatistic(String name, Map<?, Integer> slices) {
    super(StatisticType.PIE_CHART, name);
    this.slices = Map.copyOf(slices);
  }
}
