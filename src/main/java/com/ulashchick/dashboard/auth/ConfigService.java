package com.ulashchick.dashboard.auth;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ConfigService {

  @Inject
  EnvironmentService environment;

  private static final String RESOURCES_PATH = "src/main/resources";
  private static final String LOG_4J_PROP = "log4j.properties";

  public ConfigService() {
  }

  public String getLog4jPropertyFilePath() {
    final String env = environment.getCurrentEnvironment().getLabel().toLowerCase();
    return String.format("%s/%s/%s", RESOURCES_PATH, env, LOG_4J_PROP);
  }

}
