package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Team;
import org.victorrobotics.devilscoutserver.cache.Cacheable;

import java.util.Objects;

public class TeamInfo implements Cacheable<Team.Simple>, Comparable<TeamInfo> {
  private final String key;

  private int    number;
  private String name;
  private String location;

  public TeamInfo(String key, Team.Simple team) {
    this.key = key;
    update(team);
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

  public int getNumber() {
    return number;
  }

  public String getName() {
    return name;
  }

  public String getLocation() {
    return location;
  }

  @Override
  @SuppressWarnings("java:S1210") // override equals too
  public int compareTo(TeamInfo other) {
    return Integer.compare(number, other.number);
  }
}
