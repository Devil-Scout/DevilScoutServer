package org.victorrobotics.devilscoutserver.analysis.statistics;

@SuppressWarnings("java:S1694") // non-abstract
public abstract sealed class Statistic permits BooleanStatistic, NumberStatistic, PercentageStatistic {
  public final StatisticType type;
  public final String        name;

  protected Statistic(StatisticType type, String name) {
    this.type = type;
    this.name = name;
  }
}
