package com.ulashchick.podcast.common;

import com.google.inject.*;
import com.google.inject.util.Modules;
import com.ulashchick.podcast.BaseModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Map;

public class DependencyManager {
  public static final Logger logger = LoggerFactory.getLogger(DependencyManager.class);

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

    logger.info("{} class implementation has been overridden. New DI injector has been created",
        clazz.getCanonicalName());
  }

  private static class Holder {
    private static Injector injector = Guice.createInjector(
        new BaseModule()
    );
  }

}
