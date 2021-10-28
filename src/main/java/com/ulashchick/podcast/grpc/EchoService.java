package com.ulashchick.podcast.grpc;

import com.ulashchick.podcast.common.annotations.GrpcService;
import io.reactivex.Single;
import protos.com.ulashchick.dashboard.echo.EchoRequest;
import protos.com.ulashchick.dashboard.echo.EchoResponse;
import protos.com.ulashchick.dashboard.echo.RxEchoServiceGrpc;

@GrpcService
public class EchoService extends RxEchoServiceGrpc.EchoServiceImplBase {

  @Override
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
