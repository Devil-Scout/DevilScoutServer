package org.victorrobotics.devilscoutserver.analysis;

import org.victorrobotics.devilscoutserver.analysis.statistics.Statistic;
import org.victorrobotics.devilscoutserver.cache.ListCache;
import org.victorrobotics.devilscoutserver.database.DataEntry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TeamStatisticsCache extends ListCache<DataEntry.Key, List<Statistic>, TeamStatistics> {
  private final Analyzer analyzer;

  public TeamStatisticsCache(Analyzer analyzer) {
    super(false);
    this.analyzer = analyzer;
  }

  @Override
  protected Map<DataEntry.Key, List<Statistic>> getData() {
    Set<DataEntry.Key> updates = analyzer.getUpdates(lastModified());
    if (updates.isEmpty()) return Map.of();

    Map<DataEntry.Key, List<Statistic>> data = new LinkedHashMap<>();
    for (DataEntry.Key key : updates) {
      List<Statistic> statistics = analyzer.computeStatistics(key);
      data.put(key, statistics);
    }
    return data;
  }

  @Override
  protected TeamStatistics createValue(DataEntry.Key key, List<Statistic> data) {
    return new TeamStatistics(key, data);
  }
}
