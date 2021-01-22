package com.ulashchick.dashboard.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ulashchick.dashboard.auth.config.pojo.ApplicationConfig;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;

@Singleton
public class ConfigService {

  @Inject
  EnvironmentService environmentService;

  @Inject
  Logger logger;

  private static final String RESOURCES_PATH = "src/main/resources";
  private static final String LOG_4J_PROP = "log4j.properties";
  private static final String APP_CONFIG = "app-config.yaml";

  private ApplicationConfig applicationConfig;

  public ConfigService() {
  }

  public String getLog4jPropertyFilePath() {
    final String env = environmentService.getCurrentEnvironmentAsString();
    return String.format("%s/%s/%s", RESOURCES_PATH, env, LOG_4J_PROP);
  }

  public ApplicationConfig getApplicationConfig() throws IOException {
    if (applicationConfig != null) {
      return applicationConfig;
    }

    final String env = environmentService.getCurrentEnvironmentAsString();
    final String appConfigPath = String.format("%s/%s/%s", RESOURCES_PATH, env, APP_CONFIG);
    final File file = new File(appConfigPath);
    final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

    applicationConfig = objectMapper.readValue(file, ApplicationConfig.class);

    return applicationConfig;
  }

  public List<InetSocketAddress> getCassandraEndpoints() throws IOException {
    return getApplicationConfig()
        .getCassandraConfig()
        .stream()
        .map(casConfig -> new InetSocketAddress(casConfig.getHost(), casConfig.getPort()))
        .collect(Collectors.toList());
  }

}
