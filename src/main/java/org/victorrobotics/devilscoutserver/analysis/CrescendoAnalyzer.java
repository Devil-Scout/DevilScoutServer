package org.victorrobotics.devilscoutserver.analysis;

import org.victorrobotics.devilscoutserver.analysis.statistics.Statistic;
import org.victorrobotics.devilscoutserver.database.EntryDatabase;
import org.victorrobotics.devilscoutserver.tba.MatchScoresCache;

import java.sql.SQLException;
import java.util.List;

public final class CrescendoAnalyzer extends Analyzer {
  public CrescendoAnalyzer(EntryDatabase matchEntryDB, EntryDatabase pitEntryDB,
                           EntryDatabase driveTeamEntryDB, MatchScoresCache matchScoresCache) {
    super(matchEntryDB, pitEntryDB, driveTeamEntryDB, matchScoresCache);
  }

  @Override
  protected List<Statistic> computeStatistics(int team) throws SQLException {
    return List.of();
  }
}
