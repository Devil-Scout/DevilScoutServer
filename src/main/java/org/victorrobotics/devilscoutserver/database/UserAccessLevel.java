package org.victorrobotics.devilscoutserver.database;

import io.javalin.http.ForbiddenResponse;

public enum UserAccessLevel {
  USER,
  ADMIN,
  SUDO;

  public void verifyAccess(UserAccessLevel required) {
    if (this.ordinal() < required.ordinal()) {
      throw new ForbiddenResponse("Resource requires accessLevel " + required);
    }
  }
}
