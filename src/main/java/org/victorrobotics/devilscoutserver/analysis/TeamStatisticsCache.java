package org.victorrobotics.devilscoutserver.analysis;

import org.victorrobotics.devilscoutserver.analysis.statistics.Statistic;
import org.victorrobotics.devilscoutserver.cache.Cache;
import org.victorrobotics.devilscoutserver.cache.CacheValue;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.fasterxml.jackson.core.JsonProcessingException;

public class TeamStatisticsCache implements Cache<Integer, List<Statistic>, TeamStatistics> {
  private final ConcurrentMap<Integer, CacheValue<List<Statistic>, TeamStatistics>> cacheMap;

  private final Analyzer analyzer;

  private long timestamp;

  public TeamStatisticsCache(Analyzer analyzer) {
    this.cacheMap = new ConcurrentHashMap<>();
    this.analyzer = analyzer;
  }

  @Override
  public CacheValue<List<Statistic>, TeamStatistics> get(Integer key) {
    return cacheMap.get(key);
  }

  @Override
  public boolean containsKey(Integer key) {
    return cacheMap.containsKey(key);
  }

  @Override
  public void refresh() throws SQLException {
    Set<Integer> teamsToUpdate = analyzer.getTeamsToUpdate(timestamp);
    if (teamsToUpdate.isEmpty()) return;

    long updates = teamsToUpdate.stream()
                                .filter(team -> {
                                  CacheValue<List<Statistic>, TeamStatistics> value =
                                      cacheMap.computeIfAbsent(team,
                                                               t -> new CacheValue<>(new TeamStatistics(t)));
                                  try {
                                    return value.refresh(analyzer.computeStatistics(team));
                                  } catch (SQLException e) {
                                    e.printStackTrace();
                                  }
                                  return false;
                                })
                                .count();
    if (updates != 0) {
      timestamp = System.currentTimeMillis();
    }
  }

  @Override
  public int size() {
    return cacheMap.size();
  }

  @Override
  public long timestamp() {
    return timestamp;
  }
}
