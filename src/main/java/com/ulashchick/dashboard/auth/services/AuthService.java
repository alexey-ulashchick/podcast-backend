package com.ulashchick.dashboard.auth.services;

import com.google.inject.Inject;
import com.ulashchick.dashboard.auth.annotations.GrpcService;
import com.ulashchick.dashboard.auth.persistance.CassandraClient;
import io.reactivex.Single;
import protos.com.dashboard.ulashchick.auth.RxAuthServiceGrpc;
import protos.com.dashboard.ulashchick.auth.SignInUserRequest;
import protos.com.dashboard.ulashchick.auth.SignInUserResponse;

@GrpcService
public class AuthService extends RxAuthServiceGrpc.AuthServiceImplBase {

  @Inject
  private CassandraClient cassandraClient;

  @Inject
  private GoogleAuthService googleAuthService;

  @Inject
  private JwtService jwtService;

  @Override
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
