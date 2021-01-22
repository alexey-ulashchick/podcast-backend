package com.ulashchick.dashboard.auth.persistance;

import com.datastax.oss.driver.api.core.CqlSession;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ulashchick.dashboard.auth.config.ConfigService;
import java.io.IOException;

@Singleton
public class CassandraSession {

  @Inject
  private ConfigService configService;

  private final CqlSession session;

  public CassandraSession() throws IOException {
    session = CqlSession.builder()
        .withLocalDatacenter(CassandraKeyspace.DATACENTER)
        .addContactPoints(configService.getCassandraEndpoints())
        .withKeyspace(CassandraKeyspace.KEYSPACE)
        .build();
  }

  public CqlSession getSession() {
    return session;
  }

}
