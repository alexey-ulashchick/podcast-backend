package com.ulashchick.dashboard.auth.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ulashchick.dashboard.auth.config.ConfigService;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nonnull;

@Singleton
public class JwtService {

  @Inject
  private ConfigService configService;

  private Algorithm algorithm;

  private static final String CLAIM_NAME = "user-uuid";
  private static final int EXPIRATION_TIME_SECONDS = 60 * 60 * 24; // 24 hours

  public String createToken(@Nonnull UUID uuid) {
    final Instant expiresAt = Instant.now().plusSeconds(EXPIRATION_TIME_SECONDS);

    return JWT.create()
        .withClaim(CLAIM_NAME, uuid.toString())
        .withExpiresAt(Date.from(expiresAt))
        .sign(getAlgorithm());
  }

  private Algorithm getAlgorithm() {
    algorithm = Optional
        .ofNullable(algorithm)
        .orElseGet(() -> Algorithm.HMAC256(configService.getJwtSecret()));

    return algorithm;
  }

}
