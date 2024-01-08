package org.victorrobotics.devilscoutserver.analysis;

import org.victorrobotics.devilscoutserver.analysis.statistics.Statistic;

import java.util.List;
import java.util.Map;

public final class CrescendoAnalyzer extends Analyzer {
  @Override
  protected List<Statistic> computeStatistics(Map<String, List<Object>> matchSubmissions,
                                              Map<String, List<Object>> pitSubmissions,
                                              Map<String, List<Object>> driveTeamSubmissions) {
    return null;
  }
}
