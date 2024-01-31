package org.victorrobotics.devilscoutserver.analysis._2024;

import org.victorrobotics.devilscoutserver.analysis.Analyzer;
import org.victorrobotics.devilscoutserver.analysis.statistics.BooleanStatistic;
import org.victorrobotics.devilscoutserver.analysis.statistics.NumberStatistic;
import org.victorrobotics.devilscoutserver.analysis.statistics.PieChartStatistic;
import org.victorrobotics.devilscoutserver.analysis.statistics.Statistic;
import org.victorrobotics.devilscoutserver.database.DataEntry;
import org.victorrobotics.devilscoutserver.database.EntryDatabase;
import org.victorrobotics.devilscoutserver.tba.TeamOprsCache;

import java.util.List;
import java.util.Map;

public final class CrescendoAnalyzer extends Analyzer {
  public CrescendoAnalyzer(EntryDatabase matchEntryDB, EntryDatabase pitEntryDB,
                           EntryDatabase driveTeamEntryDB, TeamOprsCache teamOprsCache) {
    super(matchEntryDB, pitEntryDB, driveTeamEntryDB, teamOprsCache);
  }

  @Override
  protected List<Statistic> computeStatistics(DataHandle handle) {
    Map<String, List<DataEntry>> matchEntries = handle.getMatchEntries();

    return List.of();
  }

  private static NumberStatistic autoScore(Map<String, List<DataEntry>> matchEntries) {
    return NumberStatistic.computed("Autonomous Points", matchEntries, entry -> {
      List<Integer> actions = entry.getIntegers("/auto/routine");
      if (actions == null) return null;

      int points = 0;
      for (int action : actions) {
        points += switch (action) {
          case 0 -> 2; // Leave start
          case 1 -> 5; // Score speaker
          case 2 -> 2; // Score amp
          default -> 0;
        };
      }
      return points;
    });
  }
}
