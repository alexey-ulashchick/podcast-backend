package com.ulashchick.podcast.grpc;

import com.ulashchick.podcast.TestParameterResolver;
import io.reactivex.Single;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import protos.com.ulashchick.dashboard.echo.EchoRequest;
import protos.com.ulashchick.dashboard.echo.EchoResponse;

import static com.google.common.truth.Truth.assertThat;

@ExtendWith(TestParameterResolver.class)
class EchoServiceTest {

  private final static String TEST_MESSAGE = "test";

  private final EchoService echoService;

  public EchoServiceTest(EchoService echoService) {
    this.echoService = echoService;
  }

  @Test
  void testEcho() {
    final Single<EchoRequest> request = Single.just(EchoRequest.newBuilder().setMessage(TEST_MESSAGE).build());
    final EchoResponse response = echoService.echo(request).blockingGet();

    assertThat(response.getMessage()).endsWith(TEST_MESSAGE);
  }
}
