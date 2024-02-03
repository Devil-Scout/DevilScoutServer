package org.victorrobotics.devilscoutserver.analysis._2024;

import org.victorrobotics.devilscoutserver.analysis.Analyzer;
import org.victorrobotics.devilscoutserver.analysis.statistics.StatisticsPage;
import org.victorrobotics.devilscoutserver.database.EntryDatabase;
import org.victorrobotics.devilscoutserver.database.TeamDatabase;
import org.victorrobotics.devilscoutserver.tba.EventOprsCache;
import org.victorrobotics.devilscoutserver.tba.EventTeamListCache;
import org.victorrobotics.devilscoutserver.tba.MatchScheduleCache;

import java.util.List;

public final class CrescendoAnalyzer extends Analyzer {
  public CrescendoAnalyzer(TeamDatabase teamDB, EventTeamListCache teamListCache,
                           EntryDatabase matchEntryDB, EntryDatabase pitEntryDB,
                           EntryDatabase driveTeamEntryDB, MatchScheduleCache<?> matchScheduleCache,
                           EventOprsCache teamOprsCache) {
    super(teamDB, teamListCache, matchEntryDB, pitEntryDB, driveTeamEntryDB, matchScheduleCache,
          teamOprsCache);
  }

  @Override
  protected List<StatisticsPage> computeStatistics(DataHandle handle) {
    return List.of();
  }
}
