package org.victorrobotics.devilscoutserver.analysis;

import org.victorrobotics.devilscoutserver.analysis.statistics.StatisticsPage;
import org.victorrobotics.devilscoutserver.cache.Cache;
import org.victorrobotics.devilscoutserver.database.DataEntry;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TeamStatisticsCache
    extends Cache<DataEntry.Key, List<StatisticsPage>, TeamStatistics> {
  private final ConcurrentMap<String, SortedSet<Value<List<StatisticsPage>, TeamStatistics>>> eventTeamsMap;

  private final Analyzer analyzer;

  private boolean isLoaded;

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
    Set<DataEntry.Key> updates = analyzer.getUpdates(isLoaded ? lastModified() : 0);
    isLoaded = true;
    if (updates.isEmpty()) return;

    for (DataEntry.Key key : updates) {
      List<StatisticsPage> data = analyzer.computeStatistics(key);

      if (data == null) {
        remove(key);
        continue;
      }

      Value<List<StatisticsPage>, TeamStatistics> value = cacheMap.get(key);
      if (value == null) {
        value = new Value<>(createValue(key, data), this::modified);
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

  protected Map<DataEntry.Key, List<StatisticsPage>> getData() {
    Set<DataEntry.Key> updates = analyzer.getUpdates(lastModified());
    if (updates.isEmpty()) return Map.of();

    Map<DataEntry.Key, List<StatisticsPage>> data = new LinkedHashMap<>();
    for (DataEntry.Key key : updates) {
      List<StatisticsPage> statistics = analyzer.computeStatistics(key);
      data.put(key, statistics);
    }
    return data;
  }

  protected TeamStatistics createValue(DataEntry.Key key, List<StatisticsPage> data) {
    return new TeamStatistics(key, data);
  }
}
