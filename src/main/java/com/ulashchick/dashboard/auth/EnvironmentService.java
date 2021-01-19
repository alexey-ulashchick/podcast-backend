package com.ulashchick.dashboard.auth;

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

  public EnvironmentService() {
  }

  /**
   * Checks current system environment through system ENV environment variable. When not set,
   * default environment will be set to {@code Environment.DEV}.
   */
  public Environment getCurrentEnvironment() {
    final String env = System.getenv("ENV");

    return Arrays.stream(Environment.values())
        .filter(item -> item.label.equalsIgnoreCase(env))
        .findFirst()
        .orElse(Environment.DEV);
  }
}
