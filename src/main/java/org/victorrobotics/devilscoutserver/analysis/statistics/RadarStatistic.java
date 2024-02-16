package org.victorrobotics.devilscoutserver.analysis.statistics;

import java.util.List;

public class RadarStatistic extends Statistic {
  public static record RadarPoint(String label,
                                  Number value) {}

  public final double           max;
  public final List<RadarPoint> points;

  public RadarStatistic(String name, double max, List<RadarPoint> points) {
    super(StatisticType.RADAR, name);
    this.max = max;
    this.points = List.copyOf(points);
  }
}
