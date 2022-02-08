package com.ulashchick.podcast.common;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class DependencyManagerTest {

  @Test
  void testInitialization() {
    assertThat(DependencyManager.getAllBindings()).isNotEmpty();
  }

}
