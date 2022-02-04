package com.ulashchick.podcast.common;

import com.google.inject.*;
import com.google.inject.util.Modules;
import com.ulashchick.podcast.BaseModule;

import javax.annotation.Nonnull;
import java.util.Map;

public class DependencyManager {

  private DependencyManager() {
  }

  @Nonnull
  public static <T> T getInstance(Class<T> type) {
    return Holder.injector.getInstance(type);
  }

  @Nonnull
  @SuppressWarnings({"squid:S1452"})
  public static Map<Key<?>, Binding<?>> getAllBindings() {
    return Holder.injector.getAllBindings();
  }

  public static synchronized <T> void overrideForTest(Class<T> clazz, T instance) {
    AbstractModule abstractModule = new AbstractModule() {
      @Override
      protected void configure() {
        bind(clazz).toInstance(instance);
      }
    };
    Holder.injector = Guice.createInjector(Modules.override(new BaseModule()).with(abstractModule));
  }

  private static class Holder {
    private static Injector injector = Guice.createInjector(
        new BaseModule()
    );
  }

}
