package org.victorrobotics.devilscoutserver.data;

import static org.victorrobotics.devilscoutserver.Utils.base64Encode;

import java.util.Arrays;
import java.util.Objects;

import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequired;

public record LoginRequest(@OpenApiRequired @OpenApiExample("1559") int team,
                           @OpenApiRequired @OpenApiExample("xander") String username,
                           @OpenApiRequired @OpenApiExample("EjRWeJCrze8=") byte[] clientNonce) {
  @Override
  public boolean equals(Object obj) {
    return this == obj || (obj instanceof LoginRequest other && team() == other.team()
        && Objects.equals(username(), other.username())
        && Arrays.equals(clientNonce(), other.clientNonce()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(team(), username(), Arrays.hashCode(clientNonce()));
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append("LoginRequest[")
     .append("team=")
     .append(team())
     .append(",username=")
     .append(username())
     .append(",clientNonce=")
     .append(base64Encode(clientNonce()))
     .append("]");
    return b.toString();
  }
}
