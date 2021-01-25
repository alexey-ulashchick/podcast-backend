package com.ulashchick.dashboard.auth;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.ulashchick.dashboard.auth.annotations.GrpcService;
import com.ulashchick.dashboard.auth.config.ConfigService;
import com.ulashchick.dashboard.auth.persistance.CassandraClient;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.log4j.PropertyConfigurator;
import org.reflections.Reflections;
import org.slf4j.Logger;

/**
 * Class responsible for preparing {@link io.grpc.Server}.
 */
@Singleton
public class ApplicationServerBuilder {

  @Inject
  private Logger logger;

  @Inject
  private CassandraClient cassandraClient;

  @Inject
  private ConfigService configService;

  private List<BindableService> services;

  /**
   * Scans loaded services for {@link GrpcService} annotation and add them as a hooks to server
   * configuration.
   */
  public ApplicationServerBuilder bindAnnotatedServices() {
    final Reflections reflections = new Reflections(this.getClass().getPackage().getName());
    final Injector injector = DependencyManager.getInjector();

    services = reflections
        .getTypesAnnotatedWith(GrpcService.class)
        .stream()
        .map(this::toBindableServiceOrNull)
        .filter(Objects::nonNull)
        .map(injector::getInstance)
        .collect(Collectors.toList());

    return this;
  }

  public ApplicationServerBuilder initCassandraClient() {
    cassandraClient.init();
    return this;
  }

  public ApplicationServerBuilder initLogger() {
    PropertyConfigurator.configure(configService.getLog4jPropertyFilePath());
    return this;
  }

  public Server build() throws IOException {
    final int port = configService.getApplicationConfig().getGrpcServerConfig().getPort();
    final ServerBuilder<?> serverBuilder = ServerBuilder.forPort(port);

    services.forEach(service -> addServiceToBuilder(serverBuilder, service));

    return serverBuilder.build();
  }

  private void addServiceToBuilder(@Nonnull ServerBuilder<?> serverBuilder,
      @Nonnull BindableService service) {
    logger.info("Binding GrpcService: {}", service.getClass().getName());
    serverBuilder.addService(service);
  }

  @Nullable
  @SuppressWarnings("unchecked")
  private Class<BindableService> toBindableServiceOrNull(Class<?> klass) {
    return BindableService.class.isAssignableFrom(klass)
        ? (Class<BindableService>) klass
        : null;
  }

}
