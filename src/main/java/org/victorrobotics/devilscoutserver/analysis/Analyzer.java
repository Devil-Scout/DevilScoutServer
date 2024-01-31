package org.victorrobotics.devilscoutserver.analysis;

import org.victorrobotics.devilscoutserver.analysis.statistics.OprStatistic;
import org.victorrobotics.devilscoutserver.analysis.statistics.Statistic;
import org.victorrobotics.devilscoutserver.database.DataEntry;
import org.victorrobotics.devilscoutserver.database.EntryDatabase;
import org.victorrobotics.devilscoutserver.tba.TeamOprsCache;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Analyzer {
  private final EntryDatabase matchEntryDB;
  private final EntryDatabase pitEntryDB;
  private final EntryDatabase driveTeamEntryDB;
  private final TeamOprsCache teamOprsCache;

  protected Analyzer(EntryDatabase matchEntryDB, EntryDatabase pitEntryDB,
                     EntryDatabase driveTeamEntryDB, TeamOprsCache teamOprsCache) {
    this.matchEntryDB = matchEntryDB;
    this.pitEntryDB = pitEntryDB;
    this.driveTeamEntryDB = driveTeamEntryDB;
    this.teamOprsCache = teamOprsCache;
  }

  protected abstract List<Statistic> computeStatistics(DataHandle handle);

  public List<Statistic> computeStatistics(DataEntry.Key key) {
    return computeStatistics(new DataHandle(key));
  }

  public Set<DataEntry.Key> getUpdates(long lastUpdate) {
    try {
      Set<DataEntry.Key> keys = new LinkedHashSet<>();
      keys.addAll(matchEntryDB.getEntryKeysSince(lastUpdate));
      keys.addAll(pitEntryDB.getEntryKeysSince(lastUpdate));
      keys.addAll(driveTeamEntryDB.getEntryKeysSince(lastUpdate));
      return Collections.unmodifiableSet(keys);
    } catch (SQLException e) {
      return Set.of();
    }
  }

  protected OprStatistic teamOprs(int team) {
    return new OprStatistic("Total OPRs", teamOprsCache.get(team)
                                                       .value());
  }

  protected class DataHandle {
    private final DataEntry.Key key;

    DataHandle(DataEntry.Key key) {
      this.key = key;
    }

    public List<DataEntry> getPitEntries() {
      try {
        return pitEntryDB.getEntries(key.eventKey(), key.team());
      } catch (SQLException e) {
        throw new IllegalStateException(e);
      }
    }

    public Map<String, List<DataEntry>> getMatchEntries() {
      return entryMap(matchEntryDB, key.eventKey(), key.team());
    }

    public Map<String, List<DataEntry>> getDriveTeamEntries() {
      return entryMap(driveTeamEntryDB, key.eventKey(), key.team());
    }

    private static Map<String, List<DataEntry>> entryMap(EntryDatabase database, String eventKey,
                                                         int team) {
      List<DataEntry> entries;
      try {
        entries = database.getEntries(eventKey, team);
      } catch (SQLException e) {
        throw new IllegalStateException(e);
      }

      Map<String, List<DataEntry>> entryMap = new LinkedHashMap<>();
      for (DataEntry entry : entries) {
        entryMap.computeIfAbsent(entry.matchKey(), s -> new ArrayList<>(1))
                .add(entry);
      }
      return entryMap;
    }
  }
}
