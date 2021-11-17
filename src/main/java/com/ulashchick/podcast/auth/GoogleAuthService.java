package com.ulashchick.podcast.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier.Builder;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.common.base.Suppliers;
import com.google.inject.Inject;
import com.ulashchick.podcast.common.config.ConfigService;
import com.ulashchick.podcast.common.exceptions.ApplicationException;
import protos.com.ulashchick.podcast.auth.UserProfile;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Supplier;

public class GoogleAuthService {

    private final ConfigService configService;
    private final Supplier<GoogleIdTokenVerifier> googleTokenVerifierSupplier;

    @Inject
    public GoogleAuthService(@Nonnull ConfigService configService) {
        this.configService = configService;
        this.googleTokenVerifierSupplier = Suppliers.memoize(this::getGoogleTokenVerifier)::get;
    }

    @Nonnull
    public UserProfile verifyAndDecode(@Nonnull String googleTokenId) {
        try {
            return Optional.ofNullable(googleTokenVerifierSupplier.get().verify(googleTokenId))
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

        return new Builder(transport, gsonFactory)
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }

}
