package com.ulashchick.dashboard.auth;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;

public class DependencyManager {

  private static Injector injector;

  private DependencyManager() {
  }

  public static void init() {
    if (Objects.isNull(injector)) {
      throw new IllegalStateException("Injector already has been created.");
    }

    injector = Guice.createInjector();
  }

  @Nonnull
  public static Injector getInjector() {
    return Optional.ofNullable(injector)
        .orElseThrow(() -> new IllegalStateException("Injector has not been created yet."));
  }
}
