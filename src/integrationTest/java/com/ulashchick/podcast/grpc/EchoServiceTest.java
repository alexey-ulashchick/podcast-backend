package com.ulashchick.podcast.grpc;

import com.ulashchick.podcast.TestParameterResolver;
import com.ulashchick.podcast.common.ApplicationServerBuilder;
import com.ulashchick.podcast.common.DependencyManager;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import protos.com.ulashchick.podcast.echo.EchoRequest;
import protos.com.ulashchick.podcast.echo.EchoResponse;
import protos.com.ulashchick.podcast.echo.EchoServiceGrpc;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

@ExtendWith(TestParameterResolver.class)
class EchoServiceTest {

  @Rule
  public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

  @Test
  void authorizationIsNotRequired() throws IOException {
    final String basePackage = getClass().getPackage().getName();

    ApplicationServerBuilder applicationServerBuilder = DependencyManager.getInstance(ApplicationServerBuilder.class);

    final Server server = applicationServerBuilder.forTest("test-server")
        .addServices(basePackage)
        .addInterceptors(basePackage)
        .build()
        .start();

    grpcCleanup.register(server);

    final EchoServiceGrpc.EchoServiceBlockingStub echoServiceBlockingStub = EchoServiceGrpc
        .newBlockingStub(grpcCleanup.register(InProcessChannelBuilder.forName("test-server").directExecutor().build()));

    EchoResponse hello = echoServiceBlockingStub.echo(EchoRequest.newBuilder().setMessage("Hello").build());

    assertThat(hello.getMessage()).endsWith("Hello");
  }
}
