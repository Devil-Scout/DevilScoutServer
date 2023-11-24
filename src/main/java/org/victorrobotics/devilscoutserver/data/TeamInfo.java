package org.victorrobotics.devilscoutserver.data;

import org.victorrobotics.bluealliance.Team;
import org.victorrobotics.devilscoutserver.cache.Cacheable;

import java.util.Objects;

import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequired;

public class TeamInfo implements Cacheable<Team.Simple> {
  private final String key;

  private int    number;
  private String name;
  private String location;

  public TeamInfo(String key) {
    this.key = key;
  }

  @Override
  public boolean update(Team.Simple team) {
    if (!key.equals(team.key)) {
      throw new IllegalArgumentException();
    }

    boolean changed = false;

    if (number != team.number) {
      number = team.number;
      changed = true;
    }

    if (!Objects.equals(name, team.name)) {
      name = team.name;
      changed = true;
    }

    String teamLocation = team.city + ", " + team.province + ", " + team.country;
    if (!Objects.equals(location, teamLocation)) {
      location = teamLocation;
      changed = true;
    }

    return changed;
  }

  @OpenApiExample("1559")
  @OpenApiRequired
  public int getNumber() {
    return number;
  }

  @OpenApiExample("Devil Tech")
  @OpenApiRequired
  public String getName() {
    return name;
  }

  @OpenApiExample("Victor, New York, USA")
  @OpenApiRequired
  public String getLocation() {
    return location;
  }
}