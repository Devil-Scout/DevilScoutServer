package org.victorrobotics.devilscoutserver.database;

import static org.victorrobotics.devilscoutserver.Base64Util.base64Decode;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.javalin.http.ForbiddenResponse;
import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiIgnore;
import io.javalin.openapi.OpenApiRequired;

@SuppressWarnings("java:S6218") // arrays in equals
public record User(@OpenApiRequired @OpenApiExample("6536270208735686") long id,
                   @OpenApiRequired @OpenApiExample("1559") int team,
                   @OpenApiRequired @OpenApiExample("xander") String username,
                   @OpenApiRequired @OpenApiExample("Xander Bhalla") String fullName,
                   @OpenApiRequired UserAccessLevel accessLevel,
                   @JsonIgnore @OpenApiIgnore byte[] salt,
                   @JsonIgnore @OpenApiIgnore byte[] storedKey,
                   @JsonIgnore @OpenApiIgnore byte[] serverKey) {
  public static User fromDatabase(ResultSet resultSet) throws SQLException {
    return new User(resultSet.getLong("id"), resultSet.getInt("team"),
                    resultSet.getString("username"), resultSet.getString("full_name"),
                    UserAccessLevel.valueOf(resultSet.getString("access_level")),
                    base64Decode(resultSet.getString("salt")),
                    base64Decode(resultSet.getString("stored_key")),
                    base64Decode(resultSet.getString("server_key")));
  }

  public void verifyAccess(UserAccessLevel accessLevel) {
    if (accessLevel.ordinal() > this.accessLevel.ordinal()) {
      throw new ForbiddenResponse("Resource requires " + accessLevel + " access");
    }
  }
}
