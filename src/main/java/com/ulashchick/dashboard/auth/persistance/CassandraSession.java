package com.ulashchick.dashboard.auth.persistance;

import com.datastax.oss.driver.api.core.CqlSession;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ulashchick.dashboard.auth.config.ConfigService;
import java.io.IOException;
import org.slf4j.Logger;

@Singleton
public class CassandraSession {

  @Inject
  private ConfigService configService;

  @Inject
  private Logger logger;

  private final CqlSession session = null;

  public CassandraSession() {
//    CqlSession session;
//    try {
//      session = CqlSession.builder()
//          .withLocalDatacenter(CassandraKeyspace.DATACENTER)
//          .addContactPoints(configService.getCassandraEndpoints())
//          .withKeyspace(CassandraKeyspace.KEYSPACE)
//          .build();
//    } catch (IOException e) {
//      logger.error("Cannot connect to Cassandra cluster", e);
//      session = null;
//    }
//
//    this.session = session;
  }

  public CqlSession getSession() {
    return session;
  }

}
