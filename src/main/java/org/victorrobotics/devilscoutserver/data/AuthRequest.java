package org.victorrobotics.devilscoutserver.data;

import static org.victorrobotics.devilscoutserver.Utils.base64Encode;

import java.util.Arrays;
import java.util.Objects;

import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequired;

public record AuthRequest(@OpenApiRequired @OpenApiExample("1559") int team,
                          @OpenApiRequired @OpenApiExample("xander") String username,
                          @OpenApiRequired @OpenApiExample("EjRWeJCrze8SNFZ4kKvN7w==") byte[] nonce,
                          @OpenApiRequired
                          @OpenApiExample("EjRWeJCrze8SNFZ4kKvN7xI0VniQq83vEjRWeJCrze8=") byte[] clientProof) {
  @Override
  public boolean equals(Object obj) {
    return this == obj || (obj instanceof AuthRequest other && team() == other.team()
        && Objects.equals(username(), other.username()) && Arrays.equals(nonce(), other.nonce())
        && Arrays.equals(clientProof(), other.clientProof()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(team(), username(), Arrays.hashCode(nonce()),
                        Arrays.hashCode(clientProof()));
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append("AuthRequest[")
     .append("team=")
     .append(team())
     .append(",username=")
     .append(username())
     .append(",nonce=")
     .append(base64Encode(nonce()))
     .append(",clientProof=")
     .append(base64Encode(clientProof()))
     .append("]");
    return b.toString();
  }
}
