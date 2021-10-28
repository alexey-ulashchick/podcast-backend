package com.ulashchick.dashboard.auth;

import com.ulashchick.dashboard.common.DependencyManager;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class DependencyManagerTest {

  @Test
  void testInitialization() {
    assertThat(DependencyManager.getAllBindings()).isNotEmpty();
  }

}
