package com.ulashchick.dashboard.common.config;

import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteSource;
import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ulashchick.dashboard.common.config.pojo.ApplicationConfig;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;

@Singleton
public class ConfigService {

  @Inject
  EnvironmentService environmentService;

  @Inject
  Logger logger;

  private static final String LOG_4J_PROP = "log4j.properties";
  private static final String APP_CONFIG = "app-config.yaml";
  private static final String CQL_INIT_DIR = "cql-init";

  private ApplicationConfig applicationConfig;

  public ConfigService() {
  }

  public String getLog4jPropertyFilePath() {
    final String env = environmentService.getCurrentEnvironmentAsString();
    return String.format("%s/%s", env, LOG_4J_PROP);
  }

  public ApplicationConfig getApplicationConfig() throws IOException {
    if (applicationConfig != null) {
      return applicationConfig;
    }

    final String env = environmentService.getCurrentEnvironmentAsString();
    final String appConfigPath = String.format("%s/%s", env, APP_CONFIG);
    final InputStream yamlResourceStream = getClass().getClassLoader().getResourceAsStream(appConfigPath);
    final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

    applicationConfig = objectMapper.readValue(yamlResourceStream, ApplicationConfig.class);

    return applicationConfig;
  }

  public List<InetSocketAddress> getCassandraEndpoints() throws IOException {
    return getApplicationConfig()
        .getCassandraConfig()
        .stream()
        .map(casConfig -> new InetSocketAddress(casConfig.getHost(), casConfig.getPort()))
        .collect(Collectors.toList());
  }

  @Nonnull
  public List<SimpleStatement> getCassandraInitStatements() {
    final String env = environmentService.getCurrentEnvironmentAsString();
    final String cqlInitPath = String.format("%s/%s", env, CQL_INIT_DIR);
    final ConfigurationBuilder cb = new ConfigurationBuilder()
            .addUrls(getClass().getClassLoader().getResource(cqlInitPath))
            .setScanners(new ResourcesScanner());

    final Reflections reflections = new Reflections(cb);

    return reflections.getResources(str -> str.endsWith("cql"))
            .stream()
            .sorted()
            .map(resource -> resource.startsWith(cqlInitPath) ? resource : cqlInitPath + "/" + resource)
            .map(fullResourcePath -> Optional.ofNullable(readResourceToString(fullResourcePath)))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(str -> !str.trim().isEmpty())
            .map(SimpleStatement::newInstance)
            .collect(Collectors.toList());
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

  public String getGoogleClientId() {
    return environmentService.readEnvVariable("GOOGLE_CLIENT_ID");
  }

  public String getJwtSecret() {
    return environmentService.readEnvVariable("JWT_SECRET");
  }
}
