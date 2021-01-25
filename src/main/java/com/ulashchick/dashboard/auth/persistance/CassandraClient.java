package com.ulashchick.dashboard.auth.persistance;

import com.datastax.oss.driver.api.core.CqlSession;
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

  public void init() {
    if (environmentService.getCurrentEnvironment().equals(Environment.PROD)) {
      cassandraSession.initWithKeyspace(CassandraKeyspace.KEYSPACE);
    } else {
      cassandraSession.initWithoutKeyspace();
      initCassandraKeyspaceAndTables();
    }
  }

  private void initCassandraKeyspaceAndTables() {
    CqlSession session = cassandraSession.getSession();
    configService.getCassandraInitStatements().forEach(statement -> {
      logger.info("Executing: \n{}", statement.getQuery());
      session.execute(statement.setTimeout(Duration.ofSeconds(10)));
    });
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

}
