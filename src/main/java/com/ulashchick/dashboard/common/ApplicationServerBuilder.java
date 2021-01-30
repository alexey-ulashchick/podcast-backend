package com.ulashchick.dashboard.common;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.ulashchick.dashboard.auth.AuthInterceptor;
import com.ulashchick.dashboard.common.annotations.GrpcService;
import com.ulashchick.dashboard.common.annotations.NoAuthRequired;
import com.ulashchick.dashboard.common.config.ConfigService;
import com.ulashchick.dashboard.common.persistance.CassandraClient;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.log4j.PropertyConfigurator;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
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

  @Inject
  private AuthInterceptor authInterceptor;

  @Inject
  private ExecutorService executorService;

  private List<BindableService> services;

  /**
   * Scans loaded services for {@link GrpcService} annotation and add them as a hooks to server
   * configuration.
   */
  @Nonnull
  public ApplicationServerBuilder bindAnnotatedServices(@Nonnull String basePackage) {
    final Reflections reflections = new Reflections(basePackage);
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

  @Nonnull
  public ApplicationServerBuilder initInterceptor(@Nonnull String basePackage) {
    final ConfigurationBuilder configurationBuilder = new ConfigurationBuilder()
        .setUrls(ClasspathHelper.forPackage(basePackage))
        .setScanners(new MethodAnnotationsScanner());

    final Reflections reflections = new Reflections(configurationBuilder);
    final List<String> servicesToExcludeFromInterception = reflections
        .getMethodsAnnotatedWith(NoAuthRequired.class)
        .stream()
        .map(method -> method.getDeclaringClass().getName() + "." + method.getName())
        .map(String::toLowerCase)
        .collect(Collectors.toList());

    authInterceptor.setServicesToExclude(servicesToExcludeFromInterception);

    return this;
  }

  public ApplicationServerBuilder initLogger() {
    PropertyConfigurator.configure(configService.getLog4jPropertyFilePath());
    return this;
  }

  public Server build() throws IOException {
    final int port = configService.getApplicationConfig().getGrpcServerConfig().getPort();
    final ServerBuilder<?> serverBuilder = ServerBuilder
        .forPort(port)
        .executor(executorService)
        .intercept(authInterceptor);

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
