package com.ulashchick.dashboard;

import com.google.inject.AbstractModule;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Logger.class).toInstance(LoggerFactory.getLogger("Application Logger"));
    bind(ExecutorService.class).toInstance(
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
  }
}
