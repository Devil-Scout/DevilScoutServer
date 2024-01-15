package org.victorrobotics.devilscoutserver.analysis;

import org.victorrobotics.devilscoutserver.analysis.statistics.NumberStatistic;
import org.victorrobotics.devilscoutserver.analysis.statistics.Statistic;
import org.victorrobotics.devilscoutserver.database.Entry;
import org.victorrobotics.devilscoutserver.database.EntryDatabase;
import org.victorrobotics.devilscoutserver.tba.MatchScoresCache;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract sealed class Analyzer permits CrescendoAnalyzer {
  private final EntryDatabase    matchEntryDB;
  private final EntryDatabase    pitEntryDB;
  private final EntryDatabase    driveTeamEntryDB;
  private final MatchScoresCache matchScoresCache;

  protected Analyzer(EntryDatabase matchEntryDB, EntryDatabase pitEntryDB,
                     EntryDatabase driveTeamEntryDB, MatchScoresCache matchScoresCache) {
    this.matchEntryDB = matchEntryDB;
    this.pitEntryDB = pitEntryDB;
    this.driveTeamEntryDB = driveTeamEntryDB;
    this.matchScoresCache = matchScoresCache;
  }

  protected abstract List<Statistic> computeStatistics(int team) throws SQLException;

  protected List<Entry> getMatchEntries(int team) throws SQLException {
    return matchEntryDB.getEntries(team);
  }

  protected List<Entry> getPitEntries(int team) throws SQLException {
    return pitEntryDB.getEntries(team);
  }

  protected List<Entry> getDriveTeamEntries(int team) throws SQLException {
    return driveTeamEntryDB.getEntries(team);
  }

  protected Collection<Integer> getScores(int team) {
    return matchScoresCache.get(team)
                           .value()
                           .getScores();
  }

  protected NumberStatistic scoresStat(int team) {
    return new NumberStatistic("Match Score", matchScoresCache.get(team)
                                                              .value()
                                                              .getScores());
  }

  public Set<Integer> getTeamsToUpdate(long lastUpdate) throws SQLException {
    Set<Integer> teams = new LinkedHashSet<>();
    teams.addAll(matchEntryDB.getTeamsSince(lastUpdate));
    teams.addAll(pitEntryDB.getTeamsSince(lastUpdate));
    teams.addAll(driveTeamEntryDB.getTeamsSince(lastUpdate));
    return teams;
  }
}
