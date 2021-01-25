package com.ulashchick.dashboard.auth;

import com.google.inject.Inject;
import java.io.IOException;

public class Application {

  @Inject
  ApplicationServerBuilder applicationServerBuilder;

  public void run() throws IOException, InterruptedException {
    applicationServerBuilder
        .initLogger()
        .bindAnnotatedServices()
        .initCassandraClient()
        .build()
        .start()
        .awaitTermination();
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    DependencyManager
        .init()
        .getInstance(Application.class)
        .run();
  }

}
