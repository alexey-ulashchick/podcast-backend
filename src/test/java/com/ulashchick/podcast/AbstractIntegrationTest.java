package com.ulashchick.podcast;

import com.ulashchick.podcast.common.ApplicationServerBuilder;
import com.ulashchick.podcast.common.DependencyManager;
import com.ulashchick.podcast.common.config.ConfigService;
import io.grpc.Server;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
@Testcontainers
public abstract class AbstractIntegrationTest {
  public static final Logger logger = LoggerFactory.getLogger(AbstractIntegrationTest.class);

  @Container
  @SuppressWarnings({"rawtypes"})
  protected final CassandraContainer cassandra = new CassandraContainer(DockerImageName.parse("cassandra:latest"));

  @Spy
  protected final ConfigService configService = DependencyManager.getInstance(ConfigService.class);

  @Rule
  protected final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();
  protected final String serverName = UUID.randomUUID().toString();

  @BeforeEach
  void beforeEach() throws IOException {
    final String host = cassandra.getHost();
    final Integer port = cassandra.getFirstMappedPort();

    logger.info("Cassandra container has been started on {}:{}. Replacing config.", host, port);
    Mockito.when(configService.getCassandraEndpoints()).thenReturn(List.of(new InetSocketAddress(host,port)));

    DependencyManager.overrideForTest(ConfigService.class, configService);

    logger.info("Config has been replaced. Starting application server");

    final String basePackage = getClass().getPackage().getName();
    final ApplicationServerBuilder applicationServerBuilder = DependencyManager
        .getInstance(ApplicationServerBuilder.class);

    final Server server = applicationServerBuilder.forTest(serverName)
        .addServices(basePackage)
        .addInterceptors(basePackage)
        .build()
        .start();

    logger.info("Application server for test has been started on {}", serverName);

    grpcCleanup.register(server);
  }
}
