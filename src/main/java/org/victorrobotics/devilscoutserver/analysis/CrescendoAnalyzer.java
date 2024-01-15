package org.victorrobotics.devilscoutserver.analysis;

import org.victorrobotics.devilscoutserver.analysis.statistics.BooleanStatistic;
import org.victorrobotics.devilscoutserver.analysis.statistics.NumberStatistic;
import org.victorrobotics.devilscoutserver.analysis.statistics.PercentageStatistic;
import org.victorrobotics.devilscoutserver.analysis.statistics.Statistic;
import org.victorrobotics.devilscoutserver.database.Entry;
import org.victorrobotics.devilscoutserver.database.EntryDatabase;
import org.victorrobotics.devilscoutserver.tba.MatchScoresCache;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public final class CrescendoAnalyzer extends Analyzer {
  public CrescendoAnalyzer(EntryDatabase matchEntryDB, EntryDatabase pitEntryDB,
                           EntryDatabase driveTeamEntryDB, MatchScoresCache matchScoresCache) {
    super(matchEntryDB, pitEntryDB, driveTeamEntryDB, matchScoresCache);
  }

  @Override
  protected List<Statistic> computeStatistics(int team) throws SQLException {
    Map<String, List<Entry>> matchEntries = getMatchEntries(team);
    // Map<String, List<Entry>> pitEntries = getPitEntries(team);
    // Map<String, List<Entry>> driveTeamEntries = getDriveTeamEntries(team);
    return List.of(scoresStat(team),
                   NumberStatistic.direct("Ground pickups", matchEntries, "/teleop/pickup_ground"),
                   BooleanStatistic.direct("Trap", matchEntries, "/endgame/trap"),
                   PercentageStatistic.direct("Start Location", matchEntries,
                                              List.of("Next to amp", "Front of speaker",
                                                      "Next to speaker", "Next to source"),
                                              "/auto/start_pos"));
  }
}
