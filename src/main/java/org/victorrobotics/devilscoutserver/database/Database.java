package org.victorrobotics.devilscoutserver.database;

import java.sql.Connection;
import java.sql.SQLException;

import org.postgresql.ds.PGConnectionPoolDataSource;

public sealed class Database permits UserDB, TeamDB {
  private static PGConnectionPoolDataSource CONNECTION_POOL;

  protected Database() {}

  public static void initConnectionPool() {
    CONNECTION_POOL = new PGConnectionPoolDataSource();
    CONNECTION_POOL.setDatabaseName("devilscoutserver");
    CONNECTION_POOL.setUser("team1559");
    CONNECTION_POOL.setPassword(System.getenv("POSTGRESQL_PASSWORD"));

  }

  protected static Connection getConnection() throws SQLException {
    return CONNECTION_POOL.getConnection();
  }
}
