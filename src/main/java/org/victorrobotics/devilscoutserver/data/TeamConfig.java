package org.victorrobotics.devilscoutserver.data;

public class TeamConfig {
  private final int team;
  private final String name;

  private String eventKey;

  public TeamConfig(int team, String name) {
    this.team = team;
    this.name = name;
  }

  public int getTeam() {
    return team;
  }

  public String getName() {
    return name;
  }

  public String getEventKey() {
    return eventKey;
  }

  public void setEventKey(String eventKey) {
    this.eventKey = eventKey;
  }
}
