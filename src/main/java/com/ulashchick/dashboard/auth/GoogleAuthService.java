package com.ulashchick.dashboard.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier.Builder;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.inject.Inject;
import com.ulashchick.dashboard.common.config.ConfigService;
import com.ulashchick.dashboard.common.exceptions.ApplicationException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;
import javax.annotation.Nonnull;
import protos.com.dashboard.ulashchick.auth.UserProfile;

public class GoogleAuthService {

  @Inject
  private ConfigService configService;

  private GoogleIdTokenVerifier googleIdTokenVerifier;

  @Nonnull
  public UserProfile verifyAndDecode(@Nonnull String googleTokenId) {
    final GoogleIdTokenVerifier verifier = Optional
        .ofNullable(googleIdTokenVerifier)
        .orElseGet(this::getGoogleTokenVerifier);

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
  private UserProfile fromPayload(@Nonnull Payload payload) {
    return UserProfile.newBuilder()
        .setEmail(payload.getEmail())
        .setFirstName((String) payload.get("given_name"))
        .setLastName((String) payload.get("family_name"))
        .setPictureUrl((String) payload.get("picture"))
        .build();
  }

  @Nonnull
  private GoogleIdTokenVerifier getGoogleTokenVerifier() {
    final ApacheHttpTransport transport = new ApacheHttpTransport();
    final GsonFactory gsonFactory = GsonFactory.getDefaultInstance();
    final String googleClientId = configService.getGoogleClientId();

    final GoogleIdTokenVerifier tokenVerifier = new Builder(transport, gsonFactory)
        .setAudience(Collections.singletonList(googleClientId))
        .build();

    this.googleIdTokenVerifier = tokenVerifier;

    return tokenVerifier;
  }

}
