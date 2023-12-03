package org.victorrobotics.devilscoutserver.database;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequired;

public class Team {
  private final int number;
  private String    name;
  private String    eventKey;

  public Team(int number, String name) {
    this.number = number;
    this.name = name;
  }

  @OpenApiRequired
  @OpenApiExample("1559")
  public int getNumber() {
    return number;
  }

  @OpenApiRequired
  @OpenApiExample("Devil Tech")
  public String getName() {
    return name;
  }

  @OpenApiRequired
  @OpenApiExample("2023nyrr")
  @JsonInclude(Include.ALWAYS)
  public String getEventKey() {
    return eventKey;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setEventKey(String eventKey) {
    this.eventKey = eventKey;
  }
}
