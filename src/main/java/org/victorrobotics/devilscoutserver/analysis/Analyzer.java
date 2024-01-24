package org.victorrobotics.devilscoutserver.analysis;

import org.victorrobotics.devilscoutserver.analysis.statistics.OprStatistic;
import org.victorrobotics.devilscoutserver.analysis.statistics.Statistic;
import org.victorrobotics.devilscoutserver.database.Entry;
import org.victorrobotics.devilscoutserver.database.EntryDatabase;
import org.victorrobotics.devilscoutserver.tba.TeamOprsCache;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract sealed class Analyzer permits CrescendoAnalyzer {
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

  protected abstract List<Statistic> computeStatistics(int team);

  protected Map<String, List<Entry>> getMatchEntries(int team) {
    return entryMap(matchEntryDB, team);
  }

  protected Map<String, List<Entry>> getPitEntries(int team) {
    return entryMap(pitEntryDB, team);
  }

  protected Map<String, List<Entry>> getDriveTeamEntries(int team) {
    return entryMap(driveTeamEntryDB, team);
  }

  public Set<Integer> getTeamsToUpdate(long lastUpdate) throws SQLException {
    Set<Integer> teams = new LinkedHashSet<>();
    teams.addAll(matchEntryDB.getTeamsSince(lastUpdate));
    teams.addAll(pitEntryDB.getTeamsSince(lastUpdate));
    teams.addAll(driveTeamEntryDB.getTeamsSince(lastUpdate));
    return teams;
  }

  protected OprStatistic teamOprs(int team) {
    return new OprStatistic("Total OPRs", teamOprsCache.get(team)
                                                       .value());
  }

  private static Map<String, List<Entry>> entryMap(EntryDatabase database, int team) {
    List<Entry> entries;
    try {
      entries = database.getEntries(team);
    } catch (SQLException e) {
      throw new IllegalStateException(e);
    }

    Map<String, List<Entry>> entryMap = new LinkedHashMap<>();
    for (Entry entry : entries) {
      entryMap.computeIfAbsent(entry.matchKey(), s -> new ArrayList<>(1))
              .add(entry);
    }
    return entryMap;
  }
}
