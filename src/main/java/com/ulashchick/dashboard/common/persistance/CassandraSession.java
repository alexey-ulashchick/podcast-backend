package com.ulashchick.dashboard.common.persistance;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ulashchick.dashboard.common.config.ConfigService;
import java.io.IOException;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;

@Singleton
public class CassandraSession {

  @Inject
  private ConfigService configService;

  @Inject
  private Logger logger;

  @Nullable
  private CqlSession session;

  @Nonnull
  public CqlSession getSession() {
    return Optional
        .ofNullable(session)
        .orElseThrow(() -> new IllegalStateException("Session was not initialized"));
  }

  public void initWithKeyspace(@Nullable String keyspace) {
    init(keyspace);
  }

  public void initWithoutKeyspace() {
    init(null);
  }

  private void init(@Nullable String keyspace) {
    if (session != null) {
      throw new IllegalStateException("Session already has been created");
    }

    session = buildSession(keyspace);
  }

  @Nullable
  private CqlSession buildSession(@Nullable String keyspace) {
    try {
      final CqlSessionBuilder cqlSessionBuilder = CqlSession.builder()
          .withLocalDatacenter(CassandraKeyspace.DATACENTER)
          .addContactPoints(configService.getCassandraEndpoints());

      session = keyspace != null
          ? cqlSessionBuilder.withKeyspace(keyspace).build()
          : cqlSessionBuilder.build();

      logger.info("Connected to Cassandra cluster {}", configService.getCassandraEndpoints());
      return this.session;
    } catch (IOException e) {
      logger.error("Cannot connect to Cassandra cluster", e);
      return null;
    }
  }

}
