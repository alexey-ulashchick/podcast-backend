package com.ulashchick.podcast.common.persistance;

import com.datastax.oss.driver.api.core.AsyncPagingIterable;
import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.relation.Relation;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ulashchick.podcast.common.config.ConfigService;
import com.ulashchick.podcast.common.config.EnvironmentService;
import com.ulashchick.podcast.common.config.EnvironmentService.Environment;
import com.ulashchick.podcast.common.persistance.CassandraKeyspace.UserByEmailTable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.time.Duration;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import protos.com.dashboard.ulashchick.auth.UserProfile;

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

  @Inject
  private ExecutorService executorService;

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

  public Single<UUID> upsertUser(@Nonnull UserProfile userProfile) {
    return insertIfNotExists(userProfile.getEmail())
        .flatMap(res -> updateProfile(userProfile))
        .flatMap(res -> getUserUUIDByEmail(userProfile.getEmail()).toSingle());
  }

  public Maybe<UUID> getUserUUIDByEmail(@Nonnull String email) {
    final SimpleStatement selectStatement = QueryBuilder.selectFrom(UserByEmailTable.TABLE_NAME)
        .column(UserByEmailTable.ID)
        .where(Relation.column(UserByEmailTable.EMAIL).isEqualTo(QueryBuilder.literal(email)))
        .build();

    return execute(selectStatement)
        .map(AsyncPagingIterable::currentPage)
        .map(Iterable::iterator)
        .filter(Iterator::hasNext)
        .map(Iterator::next)
        .map(row -> row.get(UserByEmailTable.ID, UUID.class));
  }

  public Single<Boolean> updateProfile(@Nonnull UserProfile userProfile) {
    SimpleStatement insert = QueryBuilder.update(UserByEmailTable.TABLE_NAME)
        .setColumn(UserByEmailTable.FIRST_NAME, QueryBuilder.literal(userProfile.getFirstName()))
        .setColumn(UserByEmailTable.LAST_NAME, QueryBuilder.literal(userProfile.getLastName()))
        .setColumn(UserByEmailTable.IMAGE_URL, QueryBuilder.literal(userProfile.getPictureUrl()))
        .whereColumn(UserByEmailTable.EMAIL).isEqualTo(QueryBuilder.literal(userProfile.getEmail()))
        .build();

    return execute(insert)
        .map(AsyncResultSet::wasApplied);
  }

  public Single<Boolean> insertIfNotExists(@Nonnull String email) {
    final UUID uuid = Uuids.timeBased();
    final SimpleStatement insertIfNotExists = QueryBuilder.insertInto(UserByEmailTable.TABLE_NAME)
        .value(UserByEmailTable.EMAIL, QueryBuilder.literal(email))
        .value(UserByEmailTable.ID, QueryBuilder.literal(uuid))
        .ifNotExists()
        .build();

    return execute(insertIfNotExists)
        .map(AsyncResultSet::wasApplied);
  }

  private Single<AsyncResultSet> execute(@Nonnull SimpleStatement simpleStatement) {
    simpleStatement = simpleStatement
        .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM)
        .setSerialConsistencyLevel(ConsistencyLevel.LOCAL_SERIAL);

    logger.info("Executing {} with CL {}/{}", simpleStatement.getQuery(),
        simpleStatement.getConsistencyLevel(), simpleStatement.getSerialConsistencyLevel());

    final CompletableFuture<AsyncResultSet> completableFuture = cassandraSession.getSession()
        .executeAsync(simpleStatement)
        .toCompletableFuture();

    return Single
        .fromFuture(completableFuture)
        .subscribeOn(Schedulers.from(executorService));
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
