package org.victorrobotics.devilscoutserver.session;

import org.victorrobotics.devilscoutserver.controller.Controller;

import java.sql.SQLException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.javalin.http.ForbiddenResponse;

public class Session {
  private static final long DURATION_MILLIS = 8 * 60 * 60 * 1000;

  private final String key;
  private final String user;
  private final int    team;

  private long expiration;

  public Session(String key, String userId, int team) {
    this.key = key;
    this.user = userId;
    this.team = team;

    expiration = System.currentTimeMillis() + DURATION_MILLIS;
  }

  @JsonIgnore
  public boolean isExpired() {
    return System.currentTimeMillis() >= expiration;
  }

  public String getKey() {
    return key;
  }

  public String getUser() {
    return user;
  }

  public int getTeam() {
    return team;
  }

  public void refresh() {
    expiration = System.currentTimeMillis() + DURATION_MILLIS;
  }

  public void verifyAdmin() throws SQLException {
    if (!Controller.userDB()
                   .isAdmin(getUser())) {
      throw new ForbiddenResponse("Access to resource requires admin privileges");
    }
  }
}
