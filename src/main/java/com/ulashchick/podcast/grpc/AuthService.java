package com.ulashchick.podcast.grpc;

import com.google.inject.Inject;
import com.ulashchick.podcast.auth.GoogleAuthService;
import com.ulashchick.podcast.auth.JwtService;
import com.ulashchick.podcast.common.annotations.GrpcService;
import com.ulashchick.podcast.common.annotations.NoAuthRequired;
import com.ulashchick.podcast.common.persistance.CassandraClient;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.reactivex.Single;
import protos.com.ulashchick.podcast.auth.RxAuthServiceGrpc;
import protos.com.ulashchick.podcast.auth.SignInUserRequest;
import protos.com.ulashchick.podcast.auth.SignInUserResponse;

import javax.annotation.Nonnull;


@GrpcService
public class AuthService extends RxAuthServiceGrpc.AuthServiceImplBase {

  private final CassandraClient cassandraClient;
  private final GoogleAuthService googleAuthService;
  private final JwtService jwtService;

  @Inject
  public AuthService(@Nonnull CassandraClient cassandraClient,
                     @Nonnull GoogleAuthService googleAuthService,
                     @Nonnull JwtService jwtService) {
    this.cassandraClient = cassandraClient;
    this.googleAuthService = googleAuthService;
    this.jwtService = jwtService;
  }

  @Override
  @Nonnull
  @NoAuthRequired
  public Single<SignInUserResponse> signIn(@Nonnull Single<SignInUserRequest> request) {
    return request
        .map(SignInUserRequest::getIdToken)
        .filter(token -> !token.isEmpty())
        .switchIfEmpty(Single.error(new StatusRuntimeException(Status.INVALID_ARGUMENT)))
        .map(googleAuthService::verifyAndDecode)
        .flatMap(cassandraClient::upsertUser)
        .map(jwtService::createToken)
        .map(jwtToken -> SignInUserResponse
            .newBuilder()
            .setJwtToken(jwtToken)
            .build()
        );
  }

}
