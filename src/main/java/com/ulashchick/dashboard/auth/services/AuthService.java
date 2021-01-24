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
  CassandraClient cassandraClient;

  @Override
  public Single<SignInUserResponse> signIn(Single<SignInUserRequest> request) {
    return request
        .map(r -> String.format("%s-%s-%s", r.getEmail(), r.getFirstName(), r.getLastName()))
        .map(message -> SignInUserResponse.newBuilder().setJwtToken(message).build());
  }
}
