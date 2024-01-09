package org.victorrobotics.devilscoutserver.analysis;

import org.victorrobotics.devilscoutserver.cache.Cacheable;

import java.util.List;
import java.util.Objects;

import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequired;

public class TeamStatistics implements Cacheable<List<Statistic>> {
  private final int team;

  private List<Statistic> data;

  public TeamStatistics(int team) {
    this.team = team;
    data = List.of();
  }

  @Override
  public boolean update(List<Statistic> data) {
    if (Objects.equals(this.data, data)) {
      return false;
    }

    this.data = List.copyOf(data);
    return true;
  }

  @OpenApiRequired
  public List<Statistic> getData() {
    return data;
  }

  @OpenApiRequired
  @OpenApiExample("1559")
  public int getTeam() {
    return team;
  }
}
