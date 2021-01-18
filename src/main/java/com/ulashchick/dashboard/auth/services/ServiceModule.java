package com.ulashchick.dashboard.auth.services;

import com.google.inject.AbstractModule;

public class ServiceModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(AuthService.class);
  }

}
