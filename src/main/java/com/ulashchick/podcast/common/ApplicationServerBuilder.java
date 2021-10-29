package com.ulashchick.podcast.common;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ulashchick.podcast.auth.AuthInterceptor;
import com.ulashchick.podcast.common.annotations.GrpcService;
import com.ulashchick.podcast.common.annotations.NoAuthRequired;
import com.ulashchick.podcast.common.config.ConfigService;
import com.ulashchick.podcast.common.persistance.CassandraClient;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.apache.log4j.PropertyConfigurator;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * Class responsible for preparing {@link io.grpc.Server}.
 */
@Singleton
public class ApplicationServerBuilder {

    private final Logger logger;
    private final CassandraClient cassandraClient;
    private final ConfigService configService;
    private final AuthInterceptor authInterceptor;
    private final ExecutorService executorService;

    @Inject
    public ApplicationServerBuilder(Logger logger,
                                    CassandraClient cassandraClient,
                                    ConfigService configService,
                                    AuthInterceptor authInterceptor,
                                    ExecutorService executorService) {
        this.logger = logger;
        this.cassandraClient = cassandraClient;
        this.configService = configService;
        this.authInterceptor = authInterceptor;
        this.executorService = executorService;

        final String configPath = configService.getLog4jPropertyFilePath();
        final InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(configPath);

        PropertyConfigurator.configure(resourceAsStream);
    }

    public MyServerBuilder forServer() {
        final int port = configService.getApplicationConfig().getGrpcServerConfig().getPort();
        return forServer(ServerBuilder.forPort(port));
    }

    public MyServerBuilder forTest(@Nonnull String serverName) {
        return forServer(InProcessServerBuilder.forName(serverName));
    }

    private MyServerBuilder forServer(ServerBuilder<?> serverBuilder) {
        return new MyServerBuilder(serverBuilder, logger, cassandraClient, authInterceptor, executorService);
    }

    public static class MyServerBuilder {

        private final ServerBuilder<?> serverBuilder;
        private final Logger logger;
        private final CassandraClient cassandraClient;
        private final AuthInterceptor authInterceptor;
        private final ExecutorService executorService;

        public MyServerBuilder(@Nonnull ServerBuilder<?> serverBuilder,
                               @Nonnull Logger logger,
                               @Nonnull CassandraClient cassandraClient,
                               @Nonnull AuthInterceptor authInterceptor,
                               @Nonnull ExecutorService executorService) {
            this.serverBuilder = serverBuilder;
            this.logger = logger;
            this.cassandraClient = cassandraClient;
            this.authInterceptor = authInterceptor;
            this.executorService = executorService;
        }

        /**
         * Scans loaded services for {@link GrpcService} annotation and add them as a hooks to server
         * configuration.
         */
        @Nonnull
        public MyServerBuilder addServices(@Nonnull String basePackage) {
            new Reflections(basePackage).getTypesAnnotatedWith(GrpcService.class)
                    .stream()
                    .map(this::toBindableServiceOrNull)
                    .filter(Objects::nonNull)
                    .map(DependencyManager::getInstance)
                    .forEach(service -> {
                        logger.info("Binding GrpcService: {}", service.getClass().getName());
                        serverBuilder.addService(service);
                    });

            return this;
        }

        @Nonnull
        public MyServerBuilder addInterceptors(@Nonnull String basePackage) {
            final ConfigurationBuilder configurationBuilder = new ConfigurationBuilder()
                    .setUrls(ClasspathHelper.forPackage(basePackage))
                    .setScanners(Scanners.MethodsAnnotated);

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

        @Nonnull
        public Server build() {
            cassandraClient.init();
            serverBuilder
                    .executor(executorService)
                    .intercept(authInterceptor);

            return serverBuilder.build();
        }

        @Nullable
        @SuppressWarnings("unchecked")
        private Class<BindableService> toBindableServiceOrNull(Class<?> klass) {
            return BindableService.class.isAssignableFrom(klass)
                    ? (Class<BindableService>) klass
                    : null;
        }
    }

}