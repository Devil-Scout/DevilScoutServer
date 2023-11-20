package org.victorrobotics.devilscoutserver.data;

public class TeamConfig {
  private final int team;

  private String eventKey;

  public TeamConfig(int team) {
    this.team = team;
  }

  public int getTeam() {
    return team;
  }

  public String getEventKey() {
    return eventKey;
  }

  public void setEventKey(String eventKey) {
    this.eventKey = eventKey;
  }
}
