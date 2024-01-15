package org.victorrobotics.devilscoutserver.analysis.statistics;

public abstract class Statistic {
  public final StatisticType type;
  public final String name;

  protected Statistic(StatisticType type, String name) {
    this.type = type;
    this.name = name;
  }
}
