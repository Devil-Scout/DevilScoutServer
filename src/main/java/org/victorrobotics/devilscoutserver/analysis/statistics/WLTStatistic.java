package org.victorrobotics.devilscoutserver.analysis.statistics;

public class WltStatistic extends Statistic {
  public final int wins;
  public final int losses;
  public final int ties;

  public WltStatistic(String name, int wins, int losses, int ties) {
    super(StatisticType.WTL, name);
    this.wins = wins;
    this.losses = losses;
    this.ties = ties;
  }
}
