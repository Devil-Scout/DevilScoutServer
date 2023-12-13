package org.victorrobotics.devilscoutserver.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiIgnore;

public class Session {
  private static final long DURATION_MILLIS = 8 * 60 * 60 * 1000;

  private final long id;
  private final long user;
  private final int  team;

  private long expiration;

  public Session(long id, long userId, int team) {
    this.id = id;
    this.user = userId;
    this.team = team;

    expiration = System.currentTimeMillis() + DURATION_MILLIS;
  }

  @JsonCreator // for testing
  private Session(@JsonProperty("id") long id, @JsonProperty("expiration") long expiration) {
    this.id = id;
    this.expiration = expiration;
    this.user = -1;
    this.team = -1;
  }

  @JsonIgnore
  @OpenApiIgnore
  public boolean isExpired() {
    return System.currentTimeMillis() >= expiration;
  }

  @OpenApiExample("1572531932698856")
  public long getId() {
    return id;
  }

  @JsonIgnore
  @OpenApiIgnore
  public long getUser() {
    return user;
  }

  @JsonIgnore
  @OpenApiIgnore
  public int getTeam() {
    return team;
  }

  @OpenApiExample("1700675366947")
  public long getExpiration() {
    return expiration;
  }

  public void refresh() {
    expiration = System.currentTimeMillis() + DURATION_MILLIS;
  }
}
