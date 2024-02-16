package org.victorrobotics.devilscoutserver.analysis.statistics;

public final class BooleanStatistic extends Statistic {
  public final Double percent;

  public BooleanStatistic(String name, Double percent) {
    super(StatisticType.BOOLEAN, name);
    this.percent = percent;
  }
}
