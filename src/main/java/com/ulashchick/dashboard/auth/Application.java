package com.ulashchick.dashboard.auth;

import com.google.inject.Inject;
import java.io.IOException;
import org.apache.log4j.PropertyConfigurator;

public class Application {

  @Inject
  ConfigService configService;

  @Inject
  ApplicationServerBuilder applicationServerBuilder;

  public void run() throws IOException, InterruptedException {
    PropertyConfigurator.configure(configService.getLog4jPropertyFilePath());

    applicationServerBuilder
        .forPort(5005)
        .bindAnnotatedServices()
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
