package com.ulashchick.dashboard.auth.persistance;

import com.datastax.oss.driver.api.core.AsyncPagingIterable;
import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.relation.Relation;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ulashchick.dashboard.auth.config.ConfigService;
import com.ulashchick.dashboard.auth.config.EnvironmentService;
import com.ulashchick.dashboard.auth.config.EnvironmentService.Environment;
import com.ulashchick.dashboard.auth.persistance.CassandraKeyspace.UserByEmailTable;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.time.Duration;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
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
    return getUserByEmail(userProfile.getEmail())
        .defaultIfEmpty(Uuids.timeBased())
        .flatMapSingle(uuid ->
            insertUser(userProfile, uuid).andThen(Single.just(uuid))
        );
  }

  public Maybe<UUID> getUserByEmail(@Nonnull String email) {
    final SimpleStatement selectStatement = QueryBuilder.selectFrom(UserByEmailTable.TABLE_NAME)
        .column(UserByEmailTable.ID)
        .where(Relation.column(UserByEmailTable.EMAIL).isEqualTo(QueryBuilder.literal(email)))
        .build();

    final CompletableFuture<AsyncResultSet> completableFuture = cassandraSession.getSession()
        .executeAsync(selectStatement)
        .toCompletableFuture();

    return Maybe
        .fromFuture(completableFuture)
        .map(AsyncPagingIterable::currentPage)
        .map(Iterable::iterator)
        .filter(Iterator::hasNext)
        .map(Iterator::next)
        .map(row -> row.get(UserByEmailTable.ID, UUID.class));
  }

  public Completable insertUser(@Nonnull UserProfile userProfile, @Nonnull UUID timeBasedUuid) {
    SimpleStatement insert = QueryBuilder.insertInto(UserByEmailTable.TABLE_NAME)
        .value(UserByEmailTable.EMAIL, QueryBuilder.literal(userProfile.getEmail()))
        .value(UserByEmailTable.ID, QueryBuilder.literal(timeBasedUuid))
        .value(UserByEmailTable.FIRST_NAME, QueryBuilder.literal(userProfile.getFirstName()))
        .value(UserByEmailTable.LAST_NAME, QueryBuilder.literal(userProfile.getLastName()))
        .value(UserByEmailTable.IMAGE_URL, QueryBuilder.literal(userProfile.getPictureUrl()))
        .build();

    final CompletableFuture<AsyncResultSet> completableFuture = cassandraSession.getSession()
        .executeAsync(insert)
        .toCompletableFuture();

    return Completable.fromFuture(completableFuture);
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
