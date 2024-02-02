package org.victorrobotics.devilscoutserver.analysis.statistics;

import org.victorrobotics.devilscoutserver.tba.EventOprs.TeamOpr;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public final class OprStatistic extends Statistic {
  @JsonUnwrapped
  public final TeamOpr oprs;

  public OprStatistic(String name, TeamOpr oprs) {
    super(StatisticType.OPR, name);
    this.oprs = oprs;
  }
}
