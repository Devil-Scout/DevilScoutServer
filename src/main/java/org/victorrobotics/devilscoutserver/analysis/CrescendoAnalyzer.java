package org.victorrobotics.devilscoutserver.analysis;

import org.victorrobotics.devilscoutserver.database.Entry;
import org.victorrobotics.devilscoutserver.database.EntryDatabase;

import java.util.List;

public final class CrescendoAnalyzer extends Analyzer {
  public CrescendoAnalyzer(EntryDatabase matchEntryDB, EntryDatabase pitEntryDB,
                           EntryDatabase driveTeamEntryDB) {
    super(matchEntryDB, pitEntryDB, driveTeamEntryDB);
  }

  @Override
  protected List<Statistic> computeStatistics(List<Entry> matchEntries, List<Entry> pitEntries,
                                              List<Entry> driveTeamEntries) {
    return List.of();
  }
}
