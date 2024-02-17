package org.victorrobotics.devilscoutserver.analysis.statistics;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public final class PieChartStatistic extends Statistic {
  public final SortedMap<Comparable<?>, Number> slices;

  public PieChartStatistic(String name, Map<? extends Comparable<?>, ? extends Number> slices) {
    super(StatisticType.PIE_CHART, name);
    this.slices = slices.isEmpty() ? null : new TreeMap<>(slices);
  }
}
