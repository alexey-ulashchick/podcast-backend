package com.ulashchick.dashboard;

import com.google.inject.Inject;
import com.ulashchick.dashboard.common.DependencyManager;
import com.ulashchick.dashboard.common.ApplicationServerBuilder;

import java.io.IOException;

public class Application {

    @Inject
    ApplicationServerBuilder applicationServerBuilder;

    private final String basePackage = getClass().getPackage().getName();

    public void run() throws IOException, InterruptedException {
        applicationServerBuilder.initLogger();
        applicationServerBuilder.forServer()
                .addServices(basePackage)
                .addInterceptors(basePackage)
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
