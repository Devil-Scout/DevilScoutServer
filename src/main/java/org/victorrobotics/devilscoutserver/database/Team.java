package org.victorrobotics.devilscoutserver.database;

import java.sql.ResultSet;
import java.sql.SQLException;

public record Team(int number,
                   String name,
                   String eventKey) {
  public static Team fromDatabase(ResultSet resultSet) throws SQLException {
    int number = resultSet.getShort(1);
    String name = resultSet.getString(2);
    String eventKey = resultSet.getString(3);
    return new Team(number, name, eventKey);
  }
}
