package org.victorrobotics.devilscoutserver.analysis.statistics;

import org.victorrobotics.devilscoutserver.tba.ScoreBreakdown;
import org.victorrobotics.devilscoutserver.tba.ScoreBreakdown.WltRecord;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class WltStatistic extends Statistic {
  @JsonUnwrapped
  public final WltRecord wlt;

  public WltStatistic(ScoreBreakdown breakdown) {
    super(StatisticType.WTL, "Event WLT");
    this.wlt = breakdown.getWltRecord();
  }
}
