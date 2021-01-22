package com.ulashchick.dashboard.auth.persistance;

import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ulashchick.dashboard.auth.persistance.CassandraKeyspace.UserByEmailTable;
import io.reactivex.Single;
import javax.annotation.Nonnull;

@Singleton
public class CassandraClient {

  @Inject
  private CassandraSession session;

  public Single<AsyncResultSet> insertIntoUserByEmail(@Nonnull String email,
      @Nonnull String hashedPassword,
      boolean isActive) {

    final SimpleStatement insertStatement = QueryBuilder.insertInto(UserByEmailTable.TABLE_NAME)
        .value(UserByEmailTable.EMAIL, QueryBuilder.literal(email))
        .value(UserByEmailTable.PASSWORD, QueryBuilder.literal(hashedPassword))
        .value(UserByEmailTable.IS_ACTIVE, QueryBuilder.literal(isActive))
        .build();

    return Single.fromFuture(session
        .getSession()
        .executeAsync(insertStatement)
        .toCompletableFuture());
  }

}
