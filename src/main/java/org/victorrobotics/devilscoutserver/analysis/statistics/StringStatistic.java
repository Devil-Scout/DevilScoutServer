package org.victorrobotics.devilscoutserver.analysis.statistics;

public class StringStatistic extends Statistic {
  public final String value;

  public StringStatistic(String name, String value) {
    super(StatisticType.STRING, name);
    this.value = value;
  }

  public StringStatistic(String name, Object value) {
    this(name, value == null ? null : value.toString());
  }

  public StringStatistic(String name, Object value, String suffix) {
    this(name, value == null ? null : (value.toString() + suffix));
  }
}
