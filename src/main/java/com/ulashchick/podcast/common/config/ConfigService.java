package com.ulashchick.podcast.common.config;

import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Suppliers;
import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ulashchick.podcast.common.config.pojo.ApplicationConfig;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Singleton
public class ConfigService {
    private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);

    private static final String APP_CONFIG = "app-config.yaml";
    private static final String CQL_INIT_DIR = "cql-init";

    private final EnvironmentService environmentService;
    private final Supplier<ApplicationConfig> applicationConfigSupplier;

    @Inject
    public ConfigService(@Nonnull EnvironmentService environmentService) {
        this.environmentService = environmentService;
        this.applicationConfigSupplier = Suppliers.memoize(() -> {
            final String appConfigPath = getFullPath(environmentService.getCurrentEnvironmentAsString(), APP_CONFIG);
            final InputStream yamlResourceStream = getClass().getClassLoader().getResourceAsStream(appConfigPath);
            final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

            try {
                return objectMapper.readValue(yamlResourceStream, ApplicationConfig.class);
            } catch (IOException e) {
                logger.error("Cannot read application config", e);
                return null;
            }
        });
    }

    public ApplicationConfig getApplicationConfig() {
        return Objects.requireNonNull(applicationConfigSupplier.get(), "No application config");
    }

    public List<InetSocketAddress> getCassandraEndpoints() {
        return getApplicationConfig()
                .getCassandraConfig()
                .stream()
                .map(casConfig -> new InetSocketAddress(casConfig.getHost(), casConfig.getPort()))
                .collect(Collectors.toList());
    }

    @Nonnull
    public List<SimpleStatement> getCassandraInitStatements() {
        final String cqlInitPath = getFullPath(environmentService.getCurrentEnvironmentAsString(), CQL_INIT_DIR);
        final ConfigurationBuilder configurationBuilder = new ConfigurationBuilder()
                .addUrls(getClass().getClassLoader().getResource(cqlInitPath))
                .setScanners(Scanners.Resources);

        final Reflections reflections = new Reflections(configurationBuilder);
        final Pattern pattern = Pattern.compile(".*\\.cql");

        return reflections.getResources(pattern)
                .stream()
                .sorted()
                .map(resource -> resource.startsWith(cqlInitPath) ? resource : getFullPath(cqlInitPath, resource))
                .map(fullResourcePath -> Optional.ofNullable(readResourceToString(fullResourcePath)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(str -> !str.trim().isEmpty())
                .map(SimpleStatement::newInstance)
                .collect(Collectors.toList());
    }

    public String getGoogleClientId() {
        return environmentService.readEnvVariable("GOOGLE_CLIENT_ID");
    }

    public String getJwtSecret() {
        return environmentService.readEnvVariable("JWT_SECRET");
    }

    @Nullable
    private String readResourceToString(@Nonnull String resource) {
        logger.info("Loading: {}", resource);

        final InputStream inputStream = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(resource));

        try (Reader reader = new InputStreamReader(inputStream)) {
            return CharStreams.toString(reader);
        } catch (IOException e) {
            logger.error("Fail to load resource from {}", resource, e);
            return null;
        }
    }

    @Nonnull
    private String getFullPath(@Nonnull String prefix, @Nonnull String fileName) {
        return String.format("%s/%s", prefix, fileName);
    }


}
