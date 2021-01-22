package com.ulashchick.dashboard.auth.services;

import com.google.inject.Inject;
import com.ulashchick.dashboard.auth.annotations.GrpcService;
import com.ulashchick.dashboard.auth.persistance.CassandraClient;
import io.reactivex.Single;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.RandomStringUtils;
import protos.com.dashboard.ulashchick.auth.RegisterUserRequest;
import protos.com.dashboard.ulashchick.auth.RegisterUserResponse;
import protos.com.dashboard.ulashchick.auth.RxAuthServiceGrpc;
import protos.com.dashboard.ulashchick.auth.ValidateUserRequest;
import protos.com.dashboard.ulashchick.auth.ValidateUserResponse;

@GrpcService
public class AuthService extends RxAuthServiceGrpc.AuthServiceImplBase {

  @Inject
  CassandraClient cassandraClient;

  @Override
  public Single<RegisterUserResponse> registerUser(Single<RegisterUserRequest> request) {
    return request
        .map(RegisterUserRequest::getEmail)
        .map(this::createUserWithEmailReturnTempPassword)
        .map(tempPassword -> RegisterUserResponse.getDefaultInstance());
  }

  @Override
  public Single<ValidateUserResponse> validateUser(Single<ValidateUserRequest> request) {
    return super.validateUser(request);
  }

  /**
   * Generate random password and creates user with provided email and generated password.
   * Returns back single of generated raw password. This might be send as a part of the return
   * link with generated email.
   */
  private Single<String> createUserWithEmailReturnTempPassword(@Nonnull String email) {
    final String tempPassword = RandomStringUtils.random(16, true, true);

    return cassandraClient.insertIntoUserByEmail(email, tempPassword, false)
        .map(r -> tempPassword);
  }
}
