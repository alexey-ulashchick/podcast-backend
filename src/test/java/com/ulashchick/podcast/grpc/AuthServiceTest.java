package com.ulashchick.podcast.grpc;

import com.ulashchick.podcast.auth.GoogleAuthService;
import com.ulashchick.podcast.auth.JwtService;
import com.ulashchick.podcast.common.persistance.CassandraClient;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protos.com.ulashchick.podcast.auth.SignInUserRequest;
import protos.com.ulashchick.podcast.auth.SignInUserResponse;
import protos.com.ulashchick.podcast.auth.UserProfile;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  private static final Logger logger = LoggerFactory.getLogger(AuthServiceTest.class);

  private final static String TOKEN = "ABC";
  private final static String JWT_TOKEN = "JWT_TOKEN";

  @Mock
  private CassandraClient cassandraClient;

  @Mock
  private GoogleAuthService googleAuthService;

  @Mock
  private JwtService jwtService;

  private AuthService authService;

  @BeforeEach
  void beforeEach() {
    this.authService = new AuthService(cassandraClient, googleAuthService, jwtService);
  }

  @Test
  void successSignIn() {
    final SignInUserRequest signInRequest = SignInUserRequest.newBuilder().setIdToken(TOKEN).build();
    final UserProfile userProfile = UserProfile.getDefaultInstance();
    final UUID uuid = UUID.randomUUID();

    logger.info("Sign in request has been prepared");

    Mockito.when(googleAuthService.verifyAndDecode(Mockito.anyString())).thenReturn(userProfile);
    Mockito.when(cassandraClient.upsertUser(Mockito.any())).thenReturn(Single.just(uuid));
    Mockito.when(jwtService.createToken(Mockito.any())).thenReturn(JWT_TOKEN);

    final TestObserver<String> test = authService.signIn(Single.just(signInRequest))
        .map(SignInUserResponse::getJwtToken)
        .test();

    test.awaitTerminalEvent(1, TimeUnit.SECONDS);
    test.assertValue(JWT_TOKEN);

    Mockito.verify(googleAuthService, Mockito.times(1)).verifyAndDecode(Mockito.anyString());
    Mockito.verify(cassandraClient, Mockito.times(1)).upsertUser(Mockito.any());
    Mockito.verify(jwtService, Mockito.times(1)).createToken(Mockito.any());
  }

  @Test
  void failWhenTokenIsMissing() {
    final SignInUserRequest signInUserRequest = SignInUserRequest.getDefaultInstance();
    final TestObserver<SignInUserResponse> test = authService.signIn(Single.just(signInUserRequest)).test();

    test.awaitTerminalEvent(1, TimeUnit.SECONDS);
    test.assertError(throwable -> throwable instanceof StatusRuntimeException);
    test.assertError(throwable -> ((StatusRuntimeException) throwable).getStatus() == Status.INVALID_ARGUMENT);
  }
}
