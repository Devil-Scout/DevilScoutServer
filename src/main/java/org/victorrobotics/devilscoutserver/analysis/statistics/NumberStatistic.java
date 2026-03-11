package org.victorrobotics.devilscoutserver.analysis.statistics;

import org.victorrobotics.devilscoutserver.analysis.data.NumberSummary;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public final class NumberStatistic extends Statistic {
  @JsonUnwrapped
  public final NumberSummary data;

  public NumberStatistic(String name, NumberSummary data) {
    super(StatisticType.NUMBER, name);
    this.data = data;
  }
}
