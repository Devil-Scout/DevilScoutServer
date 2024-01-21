package org.victorrobotics.devilscoutserver.analysis;

import org.victorrobotics.devilscoutserver.analysis.statistics.Statistic;
import org.victorrobotics.devilscoutserver.cache.Cacheable;

import java.util.List;
import java.util.Objects;

public class TeamStatistics implements Cacheable<List<Statistic>> {
  private final int team;

  private List<Statistic> data;

  public TeamStatistics(int team, List<Statistic> data) {
    this.team = team;
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
    return team;
  }
}
