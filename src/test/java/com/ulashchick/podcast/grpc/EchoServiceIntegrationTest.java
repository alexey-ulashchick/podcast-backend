package com.ulashchick.podcast.grpc;

import com.ulashchick.podcast.AbstractIntegrationTest;
import com.ulashchick.podcast.testing.TestParameterResolver;
import io.grpc.inprocess.InProcessChannelBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import protos.com.ulashchick.podcast.echo.EchoRequest;
import protos.com.ulashchick.podcast.echo.EchoResponse;
import protos.com.ulashchick.podcast.echo.EchoServiceGrpc;

import static com.google.common.truth.Truth.assertThat;

@ExtendWith(TestParameterResolver.class)
class EchoServiceIntegrationTest extends AbstractIntegrationTest {

  @Test
  void authorizationIsNotRequired() {
    final EchoServiceGrpc.EchoServiceBlockingStub echoServiceBlockingStub = EchoServiceGrpc
        .newBlockingStub(grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build()));

    EchoResponse hello = echoServiceBlockingStub.echo(EchoRequest.newBuilder().setMessage("Hello").build());

    assertThat(hello.getMessage()).endsWith("Hello");
  }
}
