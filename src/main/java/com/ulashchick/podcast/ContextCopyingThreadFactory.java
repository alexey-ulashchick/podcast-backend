package com.ulashchick.podcast;

import org.apache.logging.log4j.ThreadContext;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class ContextCopyingThreadFactory implements ThreadFactory {

  private final ThreadFactory threadFactory = Executors.defaultThreadFactory();

  @Override
  @Nonnull
  public Thread newThread(@Nonnull Runnable runnable) {
    final Runnable runnableWithCopiedContext = copyContextToRunnable(runnable);

    return threadFactory.newThread(runnableWithCopiedContext);
  }

  @Nonnull
  private Runnable copyContextToRunnable(Runnable runnable) {
    final Map<String, String> context = ThreadContext.getContext();
    return () -> {
      ThreadContext.putAll(context);
      runnable.run();
    };
  }

}
