package org.victorrobotics.devilscoutserver.analysis;

import org.victorrobotics.bluealliance.Match.ScoreBreakdown;
import org.victorrobotics.devilscoutserver.analysis.statistics.StatisticsPage;
import org.victorrobotics.devilscoutserver.database.DataEntry;
import org.victorrobotics.devilscoutserver.database.EntryDatabase;
import org.victorrobotics.devilscoutserver.tba.MatchScheduleCache;
import org.victorrobotics.devilscoutserver.tba.MatchScheduleCache.MatchInfo;
import org.victorrobotics.devilscoutserver.tba.MatchScheduleCache.MatchSchedule;
import org.victorrobotics.devilscoutserver.tba.OprsCache;
import org.victorrobotics.devilscoutserver.tba.RankingsCache;
import org.victorrobotics.devilscoutserver.tba.OprsCache.TeamOpr;

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
  private final OprsCache          oprsCache;
  private final RankingsCache      rankingsCache;

  protected Analyzer(EntryDatabase matchEntryDB, EntryDatabase pitEntryDB,
                     EntryDatabase driveTeamEntryDB, MatchScheduleCache matchScheduleCache,
                     OprsCache teamOprsCache, RankingsCache rankingsCache) {
    this.matchEntryDB = matchEntryDB;
    this.pitEntryDB = pitEntryDB;
    this.driveTeamEntryDB = driveTeamEntryDB;
    this.matchScheduleCache = matchScheduleCache;
    this.oprsCache = teamOprsCache;
    this.rankingsCache = rankingsCache;
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

    private Collection<ScoreBreakdown> scoreBreakdowns;
    private TeamOpr                    opr;
    private RankingsCache.Team         rankings;

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
        matchEntries = loadEntriesByMatch(matchEntryDB, eventKey, team);
      }
      return matchEntries;
    }

    public Collection<List<DataEntry>> getDriveTeamEntries() {
      if (driveTeamEntries == null) {
        driveTeamEntries = loadEntriesByMatch(driveTeamEntryDB, eventKey, team);
      }
      return driveTeamEntries;
    }

    public TeamOpr getOpr() {
      if (opr == null) {
        opr = oprsCache.get(eventKey)
                       .value()
                       .get(team);
      }
      return opr;
    }

    public Collection<ScoreBreakdown> getScoreBreakdowns() {
      if (scoreBreakdowns == null) {
        scoreBreakdowns = new ArrayList<>();
        MatchSchedule schedule = matchScheduleCache.get(eventKey)
                                                   .value();
        for (MatchInfo match : schedule.values()) {
          ScoreBreakdown breakdown = getBreakdown(match);
          if (breakdown != null) {
            scoreBreakdowns.add(breakdown);
          }
        }
      }
      return scoreBreakdowns;
    }

    private ScoreBreakdown getBreakdown(MatchInfo match) {
      for (int t : match.getRed()) {
        if (t == team) {
          return match.getRedBreakdown();
        }
      }

      for (int t : match.getBlue()) {
        if (t == team) {
          return match.getBlueBreakdown();
        }
      }

      return null;
    }

    public RankingsCache.Team getRankings() {
      if (rankings == null) {
        rankings = rankingsCache.get(eventKey)
                                .value()
                                .get(team);
      }
      return rankings;
    }

    private static Collection<List<DataEntry>> loadEntriesByMatch(EntryDatabase database,
                                                                  String eventKey, int team) {
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
