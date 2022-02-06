package com.ulashchick.podcast;

import com.google.inject.AbstractModule;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BaseModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(ExecutorService.class).toInstance(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
  }
}
