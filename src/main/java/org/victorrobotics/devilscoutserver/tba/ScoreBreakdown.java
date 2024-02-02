package org.victorrobotics.devilscoutserver.tba;

public interface ScoreBreakdown {
  Object getRankingPoints();

  WltRecord getWltRecord();

  record WltRecord(int wins,
                   int losses,
                   int ties) {}
}
