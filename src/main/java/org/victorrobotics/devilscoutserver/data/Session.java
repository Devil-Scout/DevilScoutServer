package org.victorrobotics.devilscoutserver.data;

import org.victorrobotics.devilscoutserver.database.User;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiIgnore;

public class Session {
  private static final long DURATION_MILLIS = 8 * 60 * 60 * 1000;

  private final long id;
  private final long userId;
  private final int  team;

  private final UserAccessLevel accessLevel;

  private long expiration;

  public Session(long id, User user) {
    this.id = id;
    this.userId = user.id();
    this.team = user.info()
                    .team();
    this.accessLevel = user.info()
                           .accessLevel();

    expiration = System.currentTimeMillis() + DURATION_MILLIS;
  }

  @JsonCreator
  public Session(@JsonProperty("id") long id, @JsonProperty("userID") long userId,
                 @JsonProperty("team") int team,
                 @JsonProperty("accessLevel") UserAccessLevel accessLevel) {
    this.id = id;
    this.userId = userId;
    this.team = team;
    this.accessLevel = accessLevel;

    expiration = System.currentTimeMillis() + DURATION_MILLIS;
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

  @OpenApiExample("6536270208735686")
  public long getUserId() {
    return userId;
  }

  @OpenApiExample("1559")
  public int getTeam() {
    return team;
  }

  public UserAccessLevel getAccessLevel() {
    return accessLevel;
  }

  @OpenApiExample("1700675366947")
  public long getExpiration() {
    return expiration;
  }

  public void refresh() {
    expiration = System.currentTimeMillis() + DURATION_MILLIS;
  }

  public boolean hasAccess(UserAccessLevel accessLevel) {
    return accessLevel != null && accessLevel.ordinal() <= this.accessLevel.ordinal();
  }
}
