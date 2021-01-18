package com.ulashchick.dashboard.auth.services;

import com.ulashchick.dashboard.auth.annotations.GrpcService;
import io.reactivex.Single;
import protos.com.dashboard.ulashchick.auth.RegisterUserRequest;
import protos.com.dashboard.ulashchick.auth.RegisterUserResponse;
import protos.com.dashboard.ulashchick.auth.RxAuthServiceGrpc;
import protos.com.dashboard.ulashchick.auth.ValidateUserRequest;
import protos.com.dashboard.ulashchick.auth.ValidateUserResponse;

@GrpcService
public class AuthService extends RxAuthServiceGrpc.AuthServiceImplBase {

  @Override
  public Single<RegisterUserResponse> registerUser(Single<RegisterUserRequest> request) {
    return super.registerUser(request);
  }

  @Override
  public Single<ValidateUserResponse> validateUser(Single<ValidateUserRequest> request) {
    return super.validateUser(request);
  }
}
