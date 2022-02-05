package com.ulashchick.podcast.auth;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;

import java.util.concurrent.Executor;

public class BearerToken extends CallCredentials {

  private final String value;

  public BearerToken(String value) {
    this.value = value;
  }

  @Override
  public void applyRequestMetadata(RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
    appExecutor.execute(() -> {
      try {
        Metadata headers = new Metadata();
        headers.put(AuthInterceptor.AUTHORIZATION_METADATA_KEY, String.format("%s %s", AuthInterceptor.TOKEN_TYPE, value));
        applier.apply(headers);
      } catch (Exception e) {
        applier.fail(Status.UNAUTHENTICATED.withCause(e));
      }
    });
  }

  @Override
  public void thisUsesUnstableApi() {
    // No op.
  }
}
