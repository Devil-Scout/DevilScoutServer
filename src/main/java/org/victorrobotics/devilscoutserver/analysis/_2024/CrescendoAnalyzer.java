package org.victorrobotics.devilscoutserver.analysis._2024;

import org.victorrobotics.devilscoutserver.analysis.Analyzer;
import org.victorrobotics.devilscoutserver.analysis.statistics.StatisticsPage;
import org.victorrobotics.devilscoutserver.database.EntryDatabase;
import org.victorrobotics.devilscoutserver.tba.TeamOprsCache;

import java.util.List;

public final class CrescendoAnalyzer extends Analyzer {
  public CrescendoAnalyzer(EntryDatabase matchEntryDB, EntryDatabase pitEntryDB,
                           EntryDatabase driveTeamEntryDB, TeamOprsCache teamOprsCache) {
    super(matchEntryDB, pitEntryDB, driveTeamEntryDB, teamOprsCache);
  }

  @Override
  protected List<StatisticsPage> computeStatistics(DataHandle handle) {
    return List.of();
  }
}
