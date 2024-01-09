package org.victorrobotics.devilscoutserver.analysis;

import org.victorrobotics.devilscoutserver.database.DriveTeamEntryDatabase;
import org.victorrobotics.devilscoutserver.database.MatchEntryDatabase;
import org.victorrobotics.devilscoutserver.database.PitEntryDatabase;

import java.util.List;
import java.util.Map;

public final class CrescendoAnalyzer extends Analyzer {
  public CrescendoAnalyzer(MatchEntryDatabase matchEntryDB, PitEntryDatabase pitEntryDB,
                           DriveTeamEntryDatabase driveTeamEntryDB) {
    super(matchEntryDB, pitEntryDB, driveTeamEntryDB);
  }

  @Override
  protected List<Statistic> computeStatistics(Map<String, List<Object>> matchSubmissions,
                                              Map<String, List<Object>> pitSubmissions,
                                              Map<String, List<Object>> driveTeamSubmissions) {
    return List.of();
  }
}
