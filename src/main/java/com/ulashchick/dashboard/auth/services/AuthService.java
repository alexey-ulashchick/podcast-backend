package com.ulashchick.dashboard.auth.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.inject.Inject;
import com.ulashchick.dashboard.auth.annotations.GrpcService;
import com.ulashchick.dashboard.auth.config.EnvironmentService;
import com.ulashchick.dashboard.auth.exceptions.ApplicationException;
import com.ulashchick.dashboard.auth.persistance.CassandraClient;
import io.reactivex.Single;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import protos.com.dashboard.ulashchick.auth.RxAuthServiceGrpc;
import protos.com.dashboard.ulashchick.auth.SignInUserRequest;
import protos.com.dashboard.ulashchick.auth.SignInUserResponse;
import protos.com.dashboard.ulashchick.auth.UserProfile;

@GrpcService
public class AuthService extends RxAuthServiceGrpc.AuthServiceImplBase {

  @Inject
  private CassandraClient cassandraClient;

  @Inject
  private EnvironmentService environmentService;

  @Inject
  private Logger logger;

  @Override
  public Single<SignInUserResponse> signIn(Single<SignInUserRequest> request) {
    return request
        .map(SignInUserRequest::getIdToken)
        .map(this::decodeToken)
        .flatMap(cassandraClient::upsertUser)
        .map(UUID::toString)
        .map(uuid -> SignInUserResponse.newBuilder().setJwtToken(uuid).build());
  }

  private UserProfile decodeToken(String googleTokenId) {
    final GoogleIdTokenVerifier verifier = getGoogleTokenVerifier();

    try {
      return Optional.ofNullable(verifier.verify(googleTokenId))
          .map(GoogleIdToken::getPayload)
          .filter(Payload::getEmailVerified)
          .map(this::fromPayload)
          .orElseThrow(() -> new GeneralSecurityException("Invalid tokenId / unverified email"));
    } catch (GeneralSecurityException | IOException e) {
      throw new ApplicationException("Cannot encode tokenId", e.getCause());
    }
  }

  @Nonnull
  private UserProfile fromPayload(Payload payload) {
    return UserProfile.newBuilder()
        .setEmail(payload.getEmail())
        .setFirstName((String) payload.get("given_name"))
        .setLastName((String) payload.get("family_name"))
        .setPictureUrl((String) payload.get("picture"))
        .build();
  }

  @Nonnull
  private GoogleIdTokenVerifier getGoogleTokenVerifier() {
    final ApacheHttpTransport apacheHttpTransport = new ApacheHttpTransport();
    final GsonFactory gsonFactory = GsonFactory.getDefaultInstance();
    final String googleClientId = environmentService.readEnvVariable("GOOGLE_CLIENT_ID");

    return new GoogleIdTokenVerifier.Builder(apacheHttpTransport, gsonFactory)
        .setAudience(Collections.singletonList(googleClientId))
        .build();
  }
}
