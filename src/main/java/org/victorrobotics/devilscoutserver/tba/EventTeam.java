package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Team.Simple;
import org.victorrobotics.devilscoutserver.cache.Cacheable;

import java.util.Objects;

import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequired;

public class EventTeam implements Cacheable<Simple>, Comparable<EventTeam> {
  private final String key;

  private int    number;
  private String name;
  private String location;

  public EventTeam(String key) {
    this.key = key;
  }

  @Override
  public boolean update(Simple team) {
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

  @Override
  public int compareTo(EventTeam other) {
    return Integer.compare(number, other.number);
  }
}
