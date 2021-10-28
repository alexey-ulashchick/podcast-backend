package com.ulashchick.dashboard;

import com.google.inject.ConfigurationException;
import com.ulashchick.dashboard.common.DependencyManager;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class TestParameterResolver implements ParameterResolver {
  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    final Class<?> type = parameterContext.getParameter().getType();
    try {
      DependencyManager.getInjector().getInstance(type);
      return true;
    } catch (ConfigurationException e) {
      e.printStackTrace();
      return false;
    }

  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    final Class<?> type = parameterContext.getParameter().getType();
    return DependencyManager.getInjector().getInstance(type);
  }
}
