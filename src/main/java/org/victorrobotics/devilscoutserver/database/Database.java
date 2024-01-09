package org.victorrobotics.devilscoutserver.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public sealed class Database permits UserDatabase, TeamDatabase, EntryDatabase {
  private static HikariDataSource CONNECTION_POOL;

  protected Database() {}

  public static void initConnectionPool() {
    HikariConfig config = new HikariConfig();
    config.setDataSourceClassName("com.impossibl.postgres.jdbc.PGDataSource");
    config.addDataSourceProperty("databaseName", "devilscoutserver");
    config.setUsername("team1559");
    config.setPassword(System.getenv("POSTGRESQL_PASSWORD"));
    config.setMaximumPoolSize(32);
    config.setMinimumIdle(4);
    config.setThreadFactory(Thread.ofVirtual()
                                  .factory());
    CONNECTION_POOL = new HikariDataSource(config);
  }

  protected static Connection getConnection() throws SQLException {
    return CONNECTION_POOL.getConnection();
  }

  protected static <T> List<T> listFromDatabase(ResultSet resultSet, SQLFactory<T> factory)
      throws SQLException {
    List<T> items = new ArrayList<>();
    while (resultSet.next()) {
      items.add(factory.apply(resultSet));
    }
    return Collections.unmodifiableList(items);
  }

  @FunctionalInterface
  protected interface SQLFactory<T> {
    T apply(ResultSet resultSet) throws SQLException;
  }
}
