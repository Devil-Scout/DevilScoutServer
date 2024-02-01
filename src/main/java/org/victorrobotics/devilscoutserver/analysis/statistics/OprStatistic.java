package org.victorrobotics.devilscoutserver.analysis.statistics;

public final class OprStatistic extends Statistic {
  public final Double opr;
  public final Double dpr;
  public final Double ccwm;

  public OprStatistic(String name) {
    super(StatisticType.OPR, name);
    opr = null;
    dpr = null;
    ccwm = null;
  }

  public OprStatistic(String name, Double opr, Double dpr, Double ccwm) {
    super(StatisticType.OPR, name);
    this.opr = opr;
    this.dpr = dpr;
    this.ccwm = ccwm;
  }
}
