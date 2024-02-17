package org.victorrobotics.devilscoutserver.analysis.statistics;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PieChartStatistic extends Statistic {
  public final Map<String, Number> slices;

  public PieChartStatistic(String name, Map<?, ? extends Number> slices) {
    super(StatisticType.PIE_CHART, name);

    if (slices.isEmpty()) {
      this.slices = null;
    } else {
      // Fixed iteration order
      this.slices = new LinkedHashMap<>();
      for (Map.Entry<?, ? extends Number> point : slices.entrySet()) {
        this.slices.put(point.getKey()
                             .toString(),
                        point.getValue());
      }
    }
  }
}
