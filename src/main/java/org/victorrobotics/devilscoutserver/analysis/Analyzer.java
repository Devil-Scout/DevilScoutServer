package org.victorrobotics.devilscoutserver.analysis;

import org.victorrobotics.devilscoutserver.analysis.statistics.OprStatistic;
import org.victorrobotics.devilscoutserver.analysis.statistics.RankingPointsStatistic;
import org.victorrobotics.devilscoutserver.analysis.statistics.StatisticsPage;
import org.victorrobotics.devilscoutserver.analysis.statistics.WltStatistic;
import org.victorrobotics.devilscoutserver.database.DataEntry;
import org.victorrobotics.devilscoutserver.database.EntryDatabase;
import org.victorrobotics.devilscoutserver.tba.EventOprs.TeamOpr;
import org.victorrobotics.devilscoutserver.tba.EventOprsCache;
import org.victorrobotics.devilscoutserver.tba.MatchScheduleCache;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
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

  private final MatchScheduleCache<?> matchScheduleCache;
  private final EventOprsCache        oprsCache;

  protected Analyzer(EntryDatabase matchEntryDB, EntryDatabase pitEntryDB,
                     EntryDatabase driveTeamEntryDB, MatchScheduleCache<?> matchScheduleCache,
                     EventOprsCache teamOprsCache) {
    this.matchEntryDB = matchEntryDB;
    this.pitEntryDB = pitEntryDB;
    this.driveTeamEntryDB = driveTeamEntryDB;
    this.matchScheduleCache = matchScheduleCache;
    this.oprsCache = teamOprsCache;
  }

  protected abstract List<StatisticsPage> computeStatistics(DataHandle handle);

  public List<StatisticsPage> computeStatistics(DataEntry.Key key) {
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

  protected class DataHandle {
    private final DataEntry.Key key;

    DataHandle(DataEntry.Key key) {
      this.key = key;
    }

    public OprStatistic oprStatistic(String name) {
      TeamOpr oprs = oprsCache.get(key.eventKey())
                              .value()
                              .get(key.team());
      if (oprs == null) {
        return new OprStatistic(name);
      }
      return new OprStatistic(name, oprs.getOpr(), oprs.getDpr(), oprs.getCcwm());
    }

    public WltStatistic wltStatistic(String name) {
      return new WltStatistic(name, matchScheduleCache.get(key.eventKey())
                                                      .value()
                                                      .getTeamStatistics(key.team()));
    }

    public RankingPointsStatistic rpStatistic(String name) {
      return new RankingPointsStatistic(name, matchScheduleCache.get(key.eventKey())
                                                                .value()
                                                                .getTeamStatistics(key.team()));
    }

    public List<DataEntry> getPitEntries() {
      try {
        return pitEntryDB.getEntries(key.eventKey(), key.team());
      } catch (SQLException e) {
        throw new IllegalStateException(e);
      }
    }

    public Collection<List<DataEntry>> getMatchEntries() {
      return getEntries(matchEntryDB, key.eventKey(), key.team());
    }

    public Collection<List<DataEntry>> getDriveTeamEntries() {
      return getEntries(driveTeamEntryDB, key.eventKey(), key.team());
    }

    private static Collection<List<DataEntry>> getEntries(EntryDatabase database, String eventKey,
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
      return entryMap.values();
    }
  }
}
