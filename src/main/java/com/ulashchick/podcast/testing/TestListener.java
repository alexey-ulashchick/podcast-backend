package com.ulashchick.podcast.testing;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestListener implements TestExecutionListener {

  private static final Logger logger = LoggerFactory.getLogger(TestListener.class);

  @Override
  public void executionStarted(TestIdentifier testIdentifier) {
    logger.info("<<<--- Starting: {} --->>>", testIdentifier.getDisplayName());
  }

  @Override
  public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
    logger.info("<<<--- Finishing: {} --->>>", testIdentifier.getDisplayName());
  }

}
