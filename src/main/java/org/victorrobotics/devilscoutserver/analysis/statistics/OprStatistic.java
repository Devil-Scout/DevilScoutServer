package org.victorrobotics.devilscoutserver.analysis.statistics;

import org.victorrobotics.devilscoutserver.tba.TeamOprs;

public final class OprStatistic extends Statistic {
  public final TeamOprs events;

  public OprStatistic(String name, TeamOprs oprs) {
    super(StatisticType.OPR, name);
    this.events = oprs;
  }
}
