package com.ulashchick.dashboard.auth;

import com.google.inject.Inject;
import com.ulashchick.dashboard.auth.config.pojo.ApplicationConfig;
import com.ulashchick.dashboard.auth.config.ConfigService;
import java.io.IOException;
import org.apache.log4j.PropertyConfigurator;

public class Application {

  @Inject
  ConfigService configService;

  @Inject
  ApplicationServerBuilder applicationServerBuilder;

  public void run() throws IOException, InterruptedException {
    PropertyConfigurator.configure(configService.getLog4jPropertyFilePath());

    final ApplicationConfig applicationConfig = configService.getApplicationConfig();

    applicationServerBuilder
        .forPort(applicationConfig.getGrpcServerConfig().getPort())
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
