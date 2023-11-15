package org.victorrobotics.devilscoutserver.data;

import static org.victorrobotics.devilscoutserver.Utils.base64Encode;

import java.util.Arrays;
import java.util.Objects;

import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequired;

public record AuthResponse(@OpenApiRequired @OpenApiExample("Xander Bhalla") String fullName,
                           @OpenApiRequired @OpenApiExample("USER") UserAccessLevel accessLevel,
                           @OpenApiRequired @OpenApiExample("K9UoTnrEY94=") String sessionID,
                           @OpenApiRequired
                           @OpenApiExample("m7squ/lkrdjWSAER1g84uxQm3yDAOYUtVfYEJeYR2Tw=") byte[] serverSignature) {
  public AuthResponse(User user, Session session, byte[] serverSignature) {
    this(user.fullName(), user.accessLevel(), session.getSessionID(), serverSignature);
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj || (obj instanceof AuthResponse other
        && Objects.equals(fullName(), other.fullName()) && accessLevel() == other.accessLevel()
        && Objects.equals(sessionID(), other.sessionID())
        && Arrays.equals(serverSignature(), other.serverSignature()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(fullName(), accessLevel(), sessionID(), Arrays.hashCode(serverSignature()));
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append("AuthResponse[")
     .append("fullName=")
     .append(fullName())
     .append(",accessLevel=")
     .append(accessLevel())
     .append(",sessionID=")
     .append(sessionID())
     .append(",serverSignature=")
     .append(base64Encode(serverSignature()))
     .append("]");
    return b.toString();
  }
}
