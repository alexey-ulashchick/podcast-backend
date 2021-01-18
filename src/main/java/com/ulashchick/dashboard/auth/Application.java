package com.ulashchick.dashboard.auth;

import java.io.IOException;
import org.apache.log4j.PropertyConfigurator;

public class Application {

  public static void main(String[] args) throws IOException, InterruptedException {
    PropertyConfigurator.configure("src/main/resources/log4j.properties");
    DependencyManager.init();
    DependencyManager.getInjector()
        .getInstance(ApplicationServerBuilder.class)
        .forPort(5005)
        .bindAnnotatedServices()
        .build()
        .start()
        .awaitTermination();
  }

}
