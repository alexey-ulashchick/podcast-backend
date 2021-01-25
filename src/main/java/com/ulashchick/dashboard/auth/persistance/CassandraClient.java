package com.ulashchick.dashboard.auth.persistance;

import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ulashchick.dashboard.auth.config.ConfigService;
import com.ulashchick.dashboard.auth.config.EnvironmentService;
import com.ulashchick.dashboard.auth.config.EnvironmentService.Environment;
import com.ulashchick.dashboard.auth.persistance.CassandraKeyspace.UserByEmailTable;
import io.reactivex.Single;
import java.time.Duration;
import javax.annotation.Nonnull;
import org.slf4j.Logger;

@Singleton
public class CassandraClient {

  @Inject
  private CassandraSession cassandraSession;

  @Inject
  private ConfigService configService;

  @Inject
  private EnvironmentService environmentService;

  @Inject
  private Logger logger;

  /**
   * Initialize Cassandra client. For any non-production environment, initialization runs a set of
   * CQL scripts for setting up keyspace and tables.
   */
  public void init() {
    if (environmentService.getCurrentEnvironment().equals(Environment.PROD)) {
      cassandraSession.initWithKeyspace(CassandraKeyspace.KEYSPACE);
    } else {
      cassandraSession.initWithoutKeyspace();
      initCassandraKeyspaceAndTables();
    }
  }

  public Single<AsyncResultSet> insertIntoUserByEmail(@Nonnull String email,
      @Nonnull String hashedPassword,
      boolean isActive) {

    final SimpleStatement insertStatement = QueryBuilder.insertInto(UserByEmailTable.TABLE_NAME)
        .value(UserByEmailTable.EMAIL, QueryBuilder.literal(email))
        .value(UserByEmailTable.PASSWORD, QueryBuilder.literal(hashedPassword))
        .value(UserByEmailTable.IS_ACTIVE, QueryBuilder.literal(isActive))
        .build();

    return Single.fromFuture(cassandraSession
        .getSession()
        .executeAsync(insertStatement)
        .toCompletableFuture());
  }

  /**
   * Method reads CQL scripts from resources/${ENV}/cql-init folder and executes them against
   * Cassandra cluster.
   */
  private void initCassandraKeyspaceAndTables() {
    // During initialization keyspace and tables will be created. It's quite a heavy operation
    // and might take more than the standard 2000ms timeout. For these particular operations, we
    // increase timeout allowing them to finish gracefully.
    final Duration timeout = Duration.ofMillis(60_000);

    configService.getCassandraInitStatements()
        .stream()
        .map(statement -> statement.setTimeout(timeout))
        .forEach(statement -> {
          logger.info("Executing: \n{}", statement.getQuery());
          cassandraSession.getSession().execute(statement.setTimeout(timeout));
        });
  }

}
