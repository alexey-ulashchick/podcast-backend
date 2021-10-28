package com.ulashchick.dashboard.common;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.ulashchick.dashboard.BaseModule;

import javax.annotation.Nonnull;

public class DependencyManager {

  private DependencyManager() {
  }

  @Nonnull
  public static Injector getInjector() {
    return Holder.injector;
  }

  private static class Holder {
    private static final Injector injector = Guice.createInjector(
        new BaseModule()
    );
  }

}
