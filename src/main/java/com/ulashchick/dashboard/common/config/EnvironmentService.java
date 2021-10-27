package com.ulashchick.dashboard.common.config;

import com.google.inject.Singleton;

import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

    /**
     * Checks current system environment through system ENV environment variable. When not set,
     * default environment will be set to {@code Environment.DEV}.
     */
    public Environment getCurrentEnvironment() {
        return Arrays.stream(Environment.values())
                .filter(item -> item.label.equalsIgnoreCase(readEnvVariable("ENV")))
                .findFirst()
                .orElse(Environment.DEV);
    }

    public String getCurrentEnvironmentAsString() {
        return getCurrentEnvironment().getLabel().toLowerCase();
    }

    @Nullable
    public String readEnvVariable(@Nonnull String variable) {
        final String envValue = System.getenv(variable);
        return envValue.isEmpty() ? null : envValue;
    }

}
