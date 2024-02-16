package org.victorrobotics.devilscoutserver.analysis.statistics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PieChartStatistic extends Statistic {
  public static record LabeledCount(String label,
                                    int count) {}

  public final Map<String, Integer> slices;

  public PieChartStatistic(String name) {
    super(StatisticType.PIE_CHART, name);
    this.slices = null;
  }

  public PieChartStatistic(String name, Map<String, Integer> slices) {
    super(StatisticType.PIE_CHART, name);
    this.slices = slices;
  }

  public PieChartStatistic(String name, Map<Integer, Integer> slices, List<Object> labels) {
    super(StatisticType.PIE_CHART, name);
    if (slices == null) {
      this.slices = null;
    } else {
      this.slices = new HashMap<>();
      for (Map.Entry<Integer, Integer> entry : slices.entrySet()) {
        this.slices.put(labels.get(entry.getKey())
                              .toString(),
                        entry.getValue());
      }
      for (Object label : labels) {
        this.slices.putIfAbsent(label.toString(), null);
      }
    }
  }
}
