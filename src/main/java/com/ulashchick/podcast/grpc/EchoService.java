package com.ulashchick.podcast.grpc;

import com.ulashchick.podcast.common.annotations.GrpcService;
import com.ulashchick.podcast.common.annotations.NoAuthRequired;
import io.reactivex.Single;
import protos.com.ulashchick.podcast.echo.EchoRequest;
import protos.com.ulashchick.podcast.echo.EchoResponse;
import protos.com.ulashchick.podcast.echo.RxEchoServiceGrpc;

@GrpcService
public class EchoService extends RxEchoServiceGrpc.EchoServiceImplBase {

  @Override
  @NoAuthRequired
  public Single<EchoResponse> echo(Single<EchoRequest> request) {
    return request
        .map(EchoRequest::getMessage)
        .map(message -> "Echo: " + message)
        .map(responseMessage -> EchoResponse
            .newBuilder()
            .setMessage(responseMessage)
            .build()
        );
  }

}
