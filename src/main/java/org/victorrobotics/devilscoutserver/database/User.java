package org.victorrobotics.devilscoutserver.database;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiIgnore;
import io.javalin.openapi.OpenApiRequired;

@SuppressWarnings("java:S2384") // copy arrays
public class User {
  private final long id;
  private final int  team;

  private String          username;
  private String          fullName;
  private UserAccessLevel accessLevel;

  private byte[] salt;
  private byte[] storedKey;
  private byte[] serverKey;

  public User(long id, int team, String username, String fullName, UserAccessLevel accessLevel,
              byte[] salt, byte[] storedKey, byte[] serverKey) {
    this.id = id;
    this.team = team;
    this.username = username;
    this.fullName = fullName;
    this.accessLevel = accessLevel;
    this.salt = salt;
    this.storedKey = storedKey;
    this.serverKey = serverKey;
  }

  @JsonCreator // only for testing
  public User(@JsonProperty("id") long id, @JsonProperty("team") int team,
              @JsonProperty("username") String username, @JsonProperty("fullName") String fullName,
              @JsonProperty("accessLevel") UserAccessLevel accessLevel) {
    this(id, team, username, fullName, accessLevel, null, null, null);
  }

  @OpenApiRequired
  @OpenApiExample("6536270208735686")
  public long getId() {
    return id;
  }

  @OpenApiRequired
  @OpenApiExample("1559")
  public int getTeam() {
    return team;
  }

  @OpenApiRequired
  @OpenApiExample("xander")
  public String getUsername() {
    return username;
  }

  @OpenApiRequired
  @OpenApiExample("Xander Bhalla")
  public String getFullName() {
    return fullName;
  }

  @OpenApiRequired
  public UserAccessLevel getAccessLevel() {
    return accessLevel;
  }

  @JsonIgnore
  @OpenApiIgnore
  public byte[] getSalt() {
    return salt;
  }

  @JsonIgnore
  @OpenApiIgnore
  public byte[] getStoredKey() {
    return storedKey;
  }

  @JsonIgnore
  @OpenApiIgnore
  public byte[] getServerKey() {
    return serverKey;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public void setAccessLevel(UserAccessLevel accessLevel) {
    this.accessLevel = accessLevel;
  }

  public void setSalt(byte[] salt) {
    this.salt = salt;
  }

  public void setStoredKey(byte[] storedKey) {
    this.storedKey = storedKey;
  }

  public void setServerKey(byte[] serverKey) {
    this.serverKey = serverKey;
  }
}
