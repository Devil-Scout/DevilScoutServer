package org.victorrobotics.devilscoutserver.analysis.statistics;

import org.victorrobotics.bluealliance.Event.WinLossRecord;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class WltStatistic extends Statistic {
  @JsonUnwrapped
  public final WinLossRecord wlt;

  public WltStatistic(WinLossRecord wlt) {
    super(StatisticType.WTL, "Event WLT");
    this.wlt = wlt;
  }
}
