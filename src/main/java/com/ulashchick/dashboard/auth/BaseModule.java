package com.ulashchick.dashboard.auth;

import com.google.inject.AbstractModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Logger.class).toInstance(LoggerFactory.getLogger("Application Logger"));
  }
}
