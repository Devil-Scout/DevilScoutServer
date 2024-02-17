package org.victorrobotics.devilscoutserver.analysis.statistics;

import java.util.LinkedHashMap;
import java.util.Map;

public class RadarStatistic extends Statistic {
  public final double              max;
  public final Map<String, Number> points;

  public RadarStatistic(String name, double max, Map<?, Number> points) {
    super(StatisticType.RADAR, name);
    this.max = max;

    // Fixed iteration order
    this.points = new LinkedHashMap<>();
    for (Map.Entry<?, Number> point : points.entrySet()) {
      this.points.put(point.getKey()
                           .toString(),
                      point.getValue());
    }
  }
}
