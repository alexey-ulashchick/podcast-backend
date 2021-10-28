package com.ulashchick.podcast.auth;

import com.ulashchick.podcast.common.DependencyManager;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class DependencyManagerTest {

  @Test
  void testInitialization() {
    assertThat(DependencyManager.getAllBindings()).isNotEmpty();
  }

}
