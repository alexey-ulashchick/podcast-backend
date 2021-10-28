package com.ulashchick.dashboard;

import com.google.inject.Inject;
import com.ulashchick.dashboard.common.DependencyManager;
import com.ulashchick.dashboard.common.ApplicationServerBuilder;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

public class Application {

    private final ApplicationServerBuilder applicationServerBuilder;
    private final Logger logger;
    private final ExecutorService executorService;

    @Inject
    public Application(@Nonnull ApplicationServerBuilder applicationServerBuilder,
                       @Nonnull Logger logger,
                       @Nonnull ExecutorService executorService) {
        this.applicationServerBuilder = applicationServerBuilder;
        this.logger = logger;
        this.executorService = executorService;
    }

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
        final Application application = DependencyManager.getInjector().getInstance(Application.class);
        try {
            application.run();
        } catch (InterruptedException| IOException e) {
            Thread.currentThread().interrupt();
            application.logger.info("Process has been interrupted.");
            application.executorService.shutdown();
        }
    }


}
