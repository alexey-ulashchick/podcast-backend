package com.ulashchick.podcast.common.persistance;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ulashchick.podcast.common.config.ConfigService;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

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
      logger.info("Re-initializing session.");
    }

    session = buildSession(keyspace);
  }

  @Nullable
  private CqlSession buildSession(@Nullable String keyspace) {
    final CqlSessionBuilder cqlSessionBuilder = CqlSession.builder()
        .withLocalDatacenter(CassandraKeyspace.DATACENTER)
        .addContactPoints(configService.getCassandraEndpoints());

    session = keyspace != null
        ? cqlSessionBuilder.withKeyspace(keyspace).build()
        : cqlSessionBuilder.build();

    logger.info("Connected to Cassandra cluster {}", configService.getCassandraEndpoints());
    return this.session;

  }

}
