package org.victorrobotics.devilscoutserver.analysis;

import org.victorrobotics.devilscoutserver.analysis.statistics.StatisticsPage;
import org.victorrobotics.devilscoutserver.cache.Cache;
import org.victorrobotics.devilscoutserver.database.DataEntry;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TeamStatisticsCache
    extends Cache<DataEntry.Key, List<StatisticsPage>, TeamStatistics> {
  private final ConcurrentMap<String, SortedSet<Value<List<StatisticsPage>, TeamStatistics>>> eventTeamsMap;

  private final Analyzer analyzer;

  public TeamStatisticsCache(Analyzer analyzer) {
    eventTeamsMap = new ConcurrentHashMap<>();
    this.analyzer = analyzer;
  }

  @Override
  protected Value<List<StatisticsPage>, TeamStatistics> getValue(DataEntry.Key key) {
    return cacheMap.get(key);
  }

  @Override
  public void refresh() {
    Collection<DataEntry.Key> updates = analyzer.getUpdates();
    if (updates.isEmpty()) return;

    for (DataEntry.Key key : updates) {
      List<StatisticsPage> data = analyzer.computeStatistics(key);

      if (data == null) {
        remove(key);
        continue;
      }

      Value<List<StatisticsPage>, TeamStatistics> value = cacheMap.get(key);
      if (value == null) {
        value = new Value<>(new TeamStatistics(key, data), this::modified);
        cacheMap.put(key, value);
        SortedSet<Value<List<StatisticsPage>, TeamStatistics>> eventTeams =
            eventTeamsMap.computeIfAbsent(key.eventKey(), k -> new TreeSet<>());
        eventTeams.add(value);

        modified();
      } else {
        updateValue(value, data);
      }
    }
  }

  public Collection<Value<List<StatisticsPage>, TeamStatistics>> getEvent(String eventKey) {
    SortedSet<Value<List<StatisticsPage>, TeamStatistics>> value = eventTeamsMap.get(eventKey);
    return value == null ? List.of() : Collections.unmodifiableCollection(value);
  }
}
