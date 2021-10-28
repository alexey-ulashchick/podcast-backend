package com.ulashchick.podcast.grpc;

import com.google.inject.Inject;
import com.ulashchick.podcast.auth.GoogleAuthService;
import com.ulashchick.podcast.auth.JwtService;
import com.ulashchick.podcast.common.annotations.GrpcService;
import com.ulashchick.podcast.common.annotations.NoAuthRequired;
import com.ulashchick.podcast.common.persistance.CassandraClient;
import io.reactivex.Single;
import javax.annotation.Nonnull;
import protos.com.ulashchick.dashboard.auth.RxAuthServiceGrpc;
import protos.com.ulashchick.dashboard.auth.SignInUserRequest;
import protos.com.ulashchick.dashboard.auth.SignInUserResponse;

@GrpcService
public class AuthService extends RxAuthServiceGrpc.AuthServiceImplBase {

  @Inject
  private CassandraClient cassandraClient;

  @Inject
  private GoogleAuthService googleAuthService;

  @Inject
  private JwtService jwtService;

  @Override
  @Nonnull
  @NoAuthRequired
  public Single<SignInUserResponse> signIn(@Nonnull Single<SignInUserRequest> request) {
    return request
        .map(SignInUserRequest::getIdToken)
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
