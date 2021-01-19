package com.ulashchick.dashboard.auth;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class DependencyManager {

  private static Injector injector;

  private DependencyManager() {
  }

  @Nonnull
  public static Injector init(Module... modules) {
    validateInjector(false);

    final Stream<Module> moduleStream = Arrays.stream(modules).sequential();
    final Stream<Module> baseModule = Stream.of(new BaseModule());
    final List<Module> moduleList = Stream.concat(moduleStream, baseModule)
        .collect(Collectors.toList());

    injector = Guice.createInjector(moduleList);
    return injector;
  }

  @Nonnull
  public static Injector getInjector() {
    validateInjector(true);
    return injector;
  }

  private static void validateInjector(boolean shouldExists) {
    boolean isExists = !Objects.isNull(injector);

    if (isExists && !shouldExists) {
      throw new IllegalStateException("Injector already has been created.");
    } else if (!isExists && shouldExists) {
      throw new IllegalStateException("Injector has not been created yet.");
    }
  }

}
