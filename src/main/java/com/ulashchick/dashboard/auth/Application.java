package com.ulashchick.dashboard.auth;

import java.io.IOException;

public class Application {

  public static void main(String[] args) throws IOException, InterruptedException {
    DependencyManager.init();
    ApplicationServerBuilder.newServer()
        .forPort(5005)
        .bindAnnotatedServices()
        .build()
        .start()
        .awaitTermination();
  }

}
