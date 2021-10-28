package com.ulashchick.podcast.common.config;

import com.ulashchick.podcast.TestParameterResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import static com.google.common.truth.Truth.assertThat;

@ExtendWith(TestParameterResolver.class)
class EnvironmentServiceTest {

  private final EnvironmentService environmentService;

  public EnvironmentServiceTest(EnvironmentService environmentService) {
    this.environmentService = Mockito.spy(environmentService);
  }

  @Test
  void testEnvValues() {
    assertThat(EnvironmentService.Environment.DEV.getLabel()).isEqualTo("DEV");
    assertThat(EnvironmentService.Environment.TEST.getLabel()).isEqualTo("TEST");
    assertThat(EnvironmentService.Environment.PROD.getLabel()).isEqualTo("PROD");
  }

  @Test
  void getCurrentEnvironmentWhenValueSet() {
    Mockito.doReturn("TEST").when(environmentService).readEnvVariable(Mockito.anyString());

    assertThat(environmentService.getCurrentEnvironment()).isEqualTo(EnvironmentService.Environment.TEST);
    assertThat(environmentService.getCurrentEnvironmentAsString()).ignoringCase().isEqualTo("TEST");
  }

  @Test
  void getCurrentEnvironmentWhenValueNotSet() {
    Mockito.doReturn(null).when(environmentService).readEnvVariable(Mockito.anyString());

    assertThat(environmentService.getCurrentEnvironment()).isEqualTo(EnvironmentService.Environment.DEV);
    assertThat(environmentService.getCurrentEnvironmentAsString()).ignoringCase().isEqualTo("DEV");
  }
}
