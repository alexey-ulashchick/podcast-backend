package com.ulashchick.podcast.common;

import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
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

  private static class Holder {
    private static final Injector injector = Guice.createInjector(
        new BaseModule()
    );
  }

}
