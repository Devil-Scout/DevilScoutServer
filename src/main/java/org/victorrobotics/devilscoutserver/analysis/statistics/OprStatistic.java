package org.victorrobotics.devilscoutserver.analysis.statistics;

import org.victorrobotics.devilscoutserver.tba.Oprs.TeamOpr;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public final class OprStatistic extends Statistic {
  @JsonUnwrapped
  public final TeamOpr oprs;

  public OprStatistic(TeamOpr oprs) {
    super(StatisticType.OPR, "OPRs");
    this.oprs = oprs;
  }
}
