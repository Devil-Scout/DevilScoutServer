package org.victorrobotics.devilscoutserver.analysis._2024;

import org.victorrobotics.devilscoutserver.analysis.Analyzer;
import org.victorrobotics.devilscoutserver.analysis.statistics.RadarStatistic;
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
    return List.of(summaryPage(handle));
  }

  private StatisticsPage summaryPage(DataHandle handle) {
    return new StatisticsPage("Summary", List.of(handle.wltStatistic(), handle.rpStatistic(),
                                                 handle.oprStatistic(), driveTeamRadar(handle)));
  }

  private RadarStatistic driveTeamRadar(DataHandle handle) {
    return RadarStatistic.directMatch("Drive Team", handle.getDriveTeamEntries(), 5,
                                      List.of("Communication", "Strategy", "Adaptability",
                                              "Professionalism"),
                                      List.of("/communication", "/strategy", "/adaptability",
                                              "/professionalism"));
  }
}
