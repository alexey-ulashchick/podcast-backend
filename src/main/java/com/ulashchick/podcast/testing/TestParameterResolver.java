package com.ulashchick.podcast.testing;

import com.google.inject.ConfigurationException;
import com.ulashchick.podcast.common.DependencyManager;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class TestParameterResolver implements ParameterResolver {
  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    final Class<?> type = parameterContext.getParameter().getType();
    try {
      DependencyManager.getInstance(type);
      return true;
    } catch (ConfigurationException e) {
      e.printStackTrace();
      return false;
    }

  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    final Class<?> type = parameterContext.getParameter().getType();
    return DependencyManager.getInstance(type);
  }
}
