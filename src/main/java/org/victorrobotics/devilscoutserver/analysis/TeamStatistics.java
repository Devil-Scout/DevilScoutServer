package org.victorrobotics.devilscoutserver.analysis;

import org.victorrobotics.devilscoutserver.analysis.statistics.StatisticsPage;
import org.victorrobotics.devilscoutserver.cache.Cacheable;
import org.victorrobotics.devilscoutserver.database.DataEntry;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class TeamStatistics implements Cacheable<List<StatisticsPage>>, Comparable<TeamStatistics> {
  private final DataEntry.Key key;

  private List<StatisticsPage> data;

  public TeamStatistics(DataEntry.Key key, List<StatisticsPage> data) {
    this.key = key;
    data = List.copyOf(data);
  }

  @Override
  public boolean update(List<StatisticsPage> data) {
    if (Objects.equals(this.data, data)) {
      return false;
    }

    this.data = List.copyOf(data);
    return true;
  }

  public List<StatisticsPage> getData() {
    return data;
  }

  public int getTeam() {
    return key.team();
  }

  @JsonIgnore
  public DataEntry.Key getKey() {
    return key;
  }

  @Override
  @SuppressWarnings("java:S1210") // override equals()
  public int compareTo(TeamStatistics o) {
    return key.compareTo(o.key);
  }
}
