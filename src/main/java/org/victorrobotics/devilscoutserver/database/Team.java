package org.victorrobotics.devilscoutserver.database;

import java.sql.ResultSet;
import java.sql.SQLException;

import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequired;

public record Team(@OpenApiRequired @OpenApiExample("1559") int number,
                   @OpenApiRequired @OpenApiExample("Devil Tech") String name,
                   @OpenApiExample("2023nyrr") String eventKey) {
  public static Team fromDatabase(ResultSet resultSet) throws SQLException {
    return new Team(resultSet.getShort("number"), resultSet.getString("name"),
                    resultSet.getString("event_key"));
  }
}
