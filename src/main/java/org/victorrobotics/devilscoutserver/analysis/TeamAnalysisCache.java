package org.victorrobotics.devilscoutserver.analysis;

import org.victorrobotics.devilscoutserver.cache.ListCache;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TeamAnalysisCache extends ListCache<Integer, List<Statistic>, TeamStatistics> {
  private final Analyzer analyzer;

  public TeamAnalysisCache(Analyzer analyzer) {
    super(false);
    this.analyzer = analyzer;
  }

  @Override
  protected Map<Integer, List<Statistic>> getData() {
    Set<Integer> teamsToUpdate;
    try {
      teamsToUpdate = analyzer.getTeamsToUpdate(lastModified());
    } catch (SQLException e) {
      throw new IllegalStateException(e);
    }

    if (teamsToUpdate.isEmpty()) return Map.of();

    Map<Integer, List<Statistic>> data = new LinkedHashMap<>();
    for (Integer team : teamsToUpdate) {
      List<Statistic> statistics = analyzer.processTeam(team);
      data.put(team, statistics);
    }
    return data;
  }

  @Override
  protected TeamStatistics createValue(Integer key, List<Statistic> data) {
    return new TeamStatistics(key, data);
  }
}
