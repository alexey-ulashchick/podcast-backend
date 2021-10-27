package com.ulashchick.dashboard;

import com.google.inject.Inject;
import com.ulashchick.dashboard.common.DependencyManager;
import com.ulashchick.dashboard.common.ApplicationServerBuilder;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

public class Application {

    @Inject
    ApplicationServerBuilder applicationServerBuilder;

    @Inject
    Logger logger;

    @Inject
    ExecutorService executorService;

    private final String basePackage = getClass().getPackage().getName();

    private void run() throws IOException, InterruptedException {
        applicationServerBuilder.forServer()
                .addServices(basePackage)
                .addInterceptors(basePackage)
                .build()
                .start()
                .awaitTermination();
    }

    public static void main(String[] args) {
        final Application application = DependencyManager.init().getInstance(Application.class);
        try {
            application.run();
        } catch (InterruptedException| IOException e) {
            Thread.currentThread().interrupt();
            application.logger.info("Process has been interrupted.");
            application.executorService.shutdown();
        }
    }


}
