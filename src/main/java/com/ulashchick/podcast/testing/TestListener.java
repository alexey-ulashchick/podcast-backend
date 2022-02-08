package com.ulashchick.podcast.testing;

import com.ulashchick.podcast.logging.AllureLogAppender;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.ThreadContext;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public class TestListener implements TestExecutionListener {

  private static final Logger logger = LoggerFactory.getLogger(TestListener.class);
  public static final String TEST_ID = "testId";

  @Override
  public void executionStarted(TestIdentifier testIdentifier) {
    if (!testIdentifier.isTest()) {
      return;
    }

    ThreadContext.put(TEST_ID, getTestName(testIdentifier));
    logger.info("Test started");
  }

  @Override
  public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
    if (!testIdentifier.isTest()) {
      return;
    }

    logger.info("Test finished: {}", testExecutionResult.getStatus());

    final String logs = String.join("", AllureLogAppender.LOGS.get(getTestName(testIdentifier)));
    Allure.addAttachment("log", logs);
    AllureLogAppender.LOGS.remove(getTestName(testIdentifier));
  }

  @Nonnull
  private String getTestName(TestIdentifier testIdentifier) {
    return testIdentifier.getSource()
        .map(MethodSource.class::cast)
        .map(methodSource -> methodSource.getClassName() + "." + methodSource.getMethodName() + "[" + methodSource.getMethodParameterTypes() + "]")
        .orElse("unknown");
  }

}
