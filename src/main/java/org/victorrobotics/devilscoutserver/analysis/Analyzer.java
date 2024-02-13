package org.victorrobotics.devilscoutserver.analysis;

import org.victorrobotics.devilscoutserver.analysis.statistics.StatisticsPage;
import org.victorrobotics.devilscoutserver.database.DataEntry;
import org.victorrobotics.devilscoutserver.database.EntryDatabase;
import org.victorrobotics.devilscoutserver.tba.EventOprsCache;
import org.victorrobotics.devilscoutserver.tba.MatchScheduleCache;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class Analyzer<D extends AnalysisData> {
  private final EntryDatabase matchEntryDB;
  private final EntryDatabase pitEntryDB;
  private final EntryDatabase driveTeamEntryDB;

  private final MatchScheduleCache matchScheduleCache;
  private final EventOprsCache     oprsCache;

  protected Analyzer(EntryDatabase matchEntryDB, EntryDatabase pitEntryDB,
                     EntryDatabase driveTeamEntryDB, MatchScheduleCache matchScheduleCache,
                     EventOprsCache teamOprsCache) {
    this.matchEntryDB = matchEntryDB;
    this.pitEntryDB = pitEntryDB;
    this.driveTeamEntryDB = driveTeamEntryDB;
    this.matchScheduleCache = matchScheduleCache;
    this.oprsCache = teamOprsCache;
  }

  protected abstract D computeData(Handle handle);

  protected abstract List<StatisticsPage> generateStatistics(D data);

  public D computeData(String eventKey, int team) {
    return computeData(new Handle(eventKey, team));
  }

  protected class Handle {
    private final String eventKey;
    private final int    team;

    private Collection<List<DataEntry>> matchEntries;
    private List<DataEntry>             pitEntries;
    private Collection<List<DataEntry>> driveTeamEntries;

    Handle(String eventKey, int team) {
      this.eventKey = eventKey;
      this.team = team;
    }

    public List<DataEntry> getPitEntries() {
      if (pitEntries == null) {
        try {
          pitEntries = pitEntryDB.getEntries(eventKey, team);
        } catch (SQLException e) {
          throw new IllegalStateException(e);
        }
      }
      return pitEntries;
    }

    public Collection<List<DataEntry>> getMatchEntries() {
      if (matchEntries == null) {
        matchEntries = loadEntries(matchEntryDB, eventKey, team);
      }
      return matchEntries;
    }

    public Collection<List<DataEntry>> getDriveTeamEntries() {
      if (driveTeamEntries == null) {
        driveTeamEntries = loadEntries(driveTeamEntryDB, eventKey, team);
      }
      return driveTeamEntries;
    }

    private static Collection<List<DataEntry>> loadEntries(EntryDatabase database, String eventKey,
                                                           int team) {
      List<DataEntry> entries;
      try {
        entries = database.getEntries(eventKey, team);
      } catch (SQLException e) {
        throw new IllegalStateException(e);
      }

      if (entries.isEmpty()) {
        return List.of();
      }

      Map<String, List<DataEntry>> entryMap = new LinkedHashMap<>();
      for (DataEntry entry : entries) {
        entryMap.computeIfAbsent(entry.matchKey(), s -> new ArrayList<>(1))
                .add(entry);
      }
      return entryMap.values();
    }
  }
}
