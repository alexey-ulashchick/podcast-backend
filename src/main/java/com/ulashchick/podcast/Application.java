package com.ulashchick.podcast;

import com.google.inject.Inject;
import com.ulashchick.podcast.common.ApplicationServerBuilder;
import com.ulashchick.podcast.common.DependencyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    private final ApplicationServerBuilder applicationServerBuilder;
    private final ExecutorService executorService;

    @Inject
    public Application(@Nonnull ApplicationServerBuilder applicationServerBuilder,
                       @Nonnull ExecutorService executorService) {
        this.applicationServerBuilder = applicationServerBuilder;
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
        final Application application = DependencyManager.getInstance(Application.class);
        try {
            application.run();
        } catch (InterruptedException| IOException e) {
            Thread.currentThread().interrupt();
            logger.info("Process has been interrupted.");
            application.executorService.shutdown();
        }
    }


}
