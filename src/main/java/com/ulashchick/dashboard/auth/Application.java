package com.ulashchick.dashboard.auth;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.ulashchick.dashboard.auth.services.ServiceModule;
import java.io.IOException;

public class Application {

  public static void main(String[] args) throws IOException, InterruptedException {
    final Injector injector = Guice.createInjector(new ServiceModule());

    ApplicationServerBuilder.newServer()
        .forPort(5005)
        .bindAnnotatedServices(injector)
        .build()
        .start()
        .awaitTermination();
  }

}
