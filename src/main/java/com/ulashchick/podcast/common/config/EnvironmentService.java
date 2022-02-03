package com.ulashchick.podcast.common.config;

import com.google.inject.Singleton;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

@Singleton
public class EnvironmentService {
    private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);
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
        dotenv = Dotenv.configure().load();
    }

    /**
     * Checks current system environment through system ENV environment variable. When not set,
     * default environment will be set to {@code Environment.DEV}.
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
        return dotenv.get(variable);
    }

}
