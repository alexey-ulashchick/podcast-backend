package com.ulashchick.dashboard.auth;

import com.google.inject.Inject;
import com.ulashchick.dashboard.common.annotations.GrpcService;
import com.ulashchick.dashboard.common.annotations.NoAuthRequired;
import com.ulashchick.dashboard.common.persistance.CassandraClient;
import io.reactivex.Single;
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
  @NoAuthRequired
  public Single<SignInUserResponse> signIn(Single<SignInUserRequest> request) {
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
