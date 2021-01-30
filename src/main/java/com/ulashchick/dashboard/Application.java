package com.ulashchick.dashboard;

import com.google.inject.Inject;
import com.ulashchick.dashboard.common.DependencyManager;
import com.ulashchick.dashboard.common.ApplicationServerBuilder;
import java.io.IOException;

public class Application {

  @Inject
  ApplicationServerBuilder applicationServerBuilder;

  public void run() throws IOException, InterruptedException {
    applicationServerBuilder
        .initLogger()
        .bindAnnotatedServices()
        .initInterceptor()
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
