package com.ulashchick.dashboard.auth.config;

import com.google.inject.Singleton;
import java.util.Arrays;

@Singleton
public class EnvironmentService {

  public enum Environment {
    DEV("DEV"),
    TEST("TEST"),
    PROD("PROD");

    private final String label;

    Environment(String label) {
      this.label = label;
    }

    public String getLabel() {
      return label;
    }
  }

  private Environment currentEnvironment;

  public EnvironmentService() {
  }

  /**
   * Checks current system environment through system ENV environment variable. When not set,
   * default environment will be set to {@code Environment.DEV}.
   */
  public Environment getCurrentEnvironment() {
    return currentEnvironment != null ? currentEnvironment : checkCurrentEnvironment();
  }

  public String getCurrentEnvironmentAsString() {
    return getCurrentEnvironment().getLabel().toLowerCase();
  }

  private Environment checkCurrentEnvironment() {
    final String env = System.getenv("ENV");

    currentEnvironment = Arrays.stream(Environment.values())
        .filter(item -> item.label.equalsIgnoreCase(env))
        .findFirst()
        .orElse(Environment.DEV);

    return currentEnvironment;
  }

}
