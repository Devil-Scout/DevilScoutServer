package org.victorrobotics.devilscoutserver.analysis;

import org.victorrobotics.devilscoutserver.analysis.statistics.Statistic;
import org.victorrobotics.devilscoutserver.cache.Cacheable;
import org.victorrobotics.devilscoutserver.database.DataEntry;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class TeamStatistics implements Cacheable<List<Statistic>> {
  private final DataEntry.Key key;

  private List<Statistic> data;

  public TeamStatistics(DataEntry.Key key, List<Statistic> data) {
    this.key = key;
    data = List.copyOf(data);
  }

  @Override
  public boolean update(List<Statistic> data) {
    if (Objects.equals(this.data, data)) {
      return false;
    }

    this.data = List.copyOf(data);
    return true;
  }

  public List<Statistic> getData() {
    return data;
  }

  public int getTeam() {
    return key.team();
  }

  @JsonIgnore
  public DataEntry.Key getKey() {
    return key;
  }
}
