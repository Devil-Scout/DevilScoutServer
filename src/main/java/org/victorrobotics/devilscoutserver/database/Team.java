package org.victorrobotics.devilscoutserver.database;

import java.sql.ResultSet;
import java.sql.SQLException;

import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequired;

public record Team(@OpenApiRequired @OpenApiExample("1559") int number,
                   @OpenApiRequired @OpenApiExample("Devil Tech") String name,
                   @OpenApiRequired @OpenApiExample("2023nyrr") String eventKey) {
  public static Team fromDatabase(ResultSet resultSet) throws SQLException {
    int number = resultSet.getShort(1);
    String name = resultSet.getString(2);
    String eventKey = resultSet.getString(3);
    return new Team(number, name, eventKey);
  }
}
