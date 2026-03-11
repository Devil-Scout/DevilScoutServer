package org.victorrobotics.devilscoutserver.analysis.statistics;

import org.victorrobotics.bluealliance.Event.WinLossRecord;

public class WltStatistic extends Statistic {
  public final int wins;
  public final int losses;
  public final int ties;

  public WltStatistic(WinLossRecord wlt) {
    super(StatisticType.WTL, "Event WLT");
    if (wlt == null) {
      wins = 0;
      losses = 0;
      ties = 0;
    } else {
      wins = wlt.wins();
      losses = wlt.losses();
      ties = wlt.ties();
    }
  }
}
