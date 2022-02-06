package com.ulashchick.podcast.common.config;

import com.google.inject.Singleton;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;

@Singleton
public class EnvironmentService {
  private static final Logger logger = LoggerFactory.getLogger(EnvironmentService.class);
  private static final String PODCAST_INDEX_KEY = "PODCAST_INDEX_KEY";
  private static final String PODCAST_INDEX_SECRET = "PODCAST_INDEX_SECRET";


  private final Dotenv dotenv;

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
    dotenv = Dotenv.configure().ignoreIfMalformed().ignoreIfMissing().load();
  }

  /**
   * Checks current system environment through system ENV environment variable. When not set,
   * default environment will be set to {@code E                                         nvironment.DEV}.
   */
  @Nonnull
  public Environment getCurrentEnvironment() {
    final String env = readEnvVariable("ENV");
    return Arrays.stream(Environment.values())
        .filter(item -> item.label.equalsIgnoreCase(env))
        .findFirst()
        .orElse(Environment.DEV);
  }

  @Nonnull
  public String getCurrentEnvironmentAsString() {
    return getCurrentEnvironment().getLabel().toLowerCase();
  }

  @Nonnull
  public String readEnvVariable(@Nonnull String variable) {
      final String value = Objects.requireNonNullElse(dotenv.get(variable), "");

      if (value.isEmpty()) {
          logger.warn("Environment variable {} has not been set.", variable);
      }

      return value;
  }

  @Nonnull
  public String getPodcastIndexKey() {
    return readEnvVariable(PODCAST_INDEX_KEY);
  }

  @Nonnull
  public String getPodcastIndexSecret() {
    return readEnvVariable(PODCAST_INDEX_SECRET);
  }

}
