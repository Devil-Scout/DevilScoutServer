package org.victorrobotics.devilscoutserver.analysis;

import org.victorrobotics.devilscoutserver.database.Entry;
import org.victorrobotics.devilscoutserver.database.EntryDatabase;

import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;

public abstract sealed class Analyzer permits CrescendoAnalyzer {
  private final EntryDatabase matchEntryDB;
  private final EntryDatabase pitEntryDB;
  private final EntryDatabase driveTeamEntryDB;

  protected Analyzer(EntryDatabase matchEntryDB, EntryDatabase pitEntryDB,
                     EntryDatabase driveTeamEntryDB) {
    this.matchEntryDB = matchEntryDB;
    this.pitEntryDB = pitEntryDB;
    this.driveTeamEntryDB = driveTeamEntryDB;
  }

  protected abstract List<Statistic> computeStatistics(List<Entry> matchEntries,
                                                       List<Entry> pitEntries,
                                                       List<Entry> driveTeamEntries)
      throws JsonProcessingException;

  public Set<Integer> getTeamsToUpdate(long lastUpdate) throws SQLException {
    Set<Integer> teams = new LinkedHashSet<>();
    teams.addAll(matchEntryDB.getTeamsSince(lastUpdate));
    teams.addAll(pitEntryDB.getTeamsSince(lastUpdate));
    teams.addAll(driveTeamEntryDB.getTeamsSince(lastUpdate));
    return teams;
  }

  public List<Statistic> processTeam(int team) throws SQLException, JsonProcessingException {
    List<Entry> matchEntries = matchEntryDB.getEntries(team);
    List<Entry> pitEntries = pitEntryDB.getEntries(team);
    List<Entry> driveTeamEntries = driveTeamEntryDB.getEntries(team);

    return computeStatistics(matchEntries, pitEntries, driveTeamEntries);
  }
}
