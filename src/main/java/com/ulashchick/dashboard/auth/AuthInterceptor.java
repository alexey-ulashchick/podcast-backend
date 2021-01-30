package com.ulashchick.dashboard.auth;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nonnull;
import org.slf4j.Logger;

@Singleton
public class AuthInterceptor implements ServerInterceptor {

  public static final Context.Key<UUID> UUID_KEY = Context.key("uuid");
  public static final Metadata.Key<String> AUTHORIZATION_METADATA_KEY = Metadata.Key
      .of("Authorization", ASCII_STRING_MARSHALLER);

  public static final Metadata.Key<String> REFRESH_TOKEN_HEADER = Metadata.Key
      .of("RefreshToken", ASCII_STRING_MARSHALLER);

  public static final String TOKEN_TYPE = "Bearer ";

  @Inject
  private Logger logger;

  @Inject
  private JwtService jwtService;

  private List<String> servicesToExclude;

  public void setServicesToExclude(List<String> servicesToExclude) {
    this.servicesToExclude = servicesToExclude;
  }

  @Override
  @Nonnull
  public <R, T> Listener<R> interceptCall(@Nonnull ServerCall<R, T> call,
                                          @Nonnull Metadata headers,
                                          @Nonnull ServerCallHandler<R, T> next) {

    final MethodDescriptor<R, T> methodDescriptor = call.getMethodDescriptor();
    final String fullJavaMethodName = getFullJavaMethodName(methodDescriptor);
    final boolean allowRequest = servicesToExclude.stream().anyMatch(fullJavaMethodName::contains);

    if (allowRequest) {
      return Contexts.interceptCall(Context.current(), call, headers, next);
    }

    try {
      final String authorizationHeader = AuthInterceptor.getAuthorizationHeader(headers);
      final String rawToken = AuthInterceptor.validatePrefixAndReturnRawToken(authorizationHeader);
      final UUID uuid = jwtService.validateAndGetUUID(rawToken);
      final Context context = Context.current().withValue(UUID_KEY, uuid);

      return Contexts.interceptCall(context, setRefreshToken(call, uuid), headers, next);
    } catch (Exception e) {
      logger.error("Cannot authenticate user", e);
      call.close(Status.UNAUTHENTICATED, headers);
      return new Listener<R>() {
      };
    }
  }

  @Nonnull
  private <R, T> ServerCall<R, T> setRefreshToken(@Nonnull ServerCall<R, T> call,
                                                  @Nonnull UUID uuid) {
    return new SimpleForwardingServerCall<R, T>(call) {
      @Override
      public void sendHeaders(Metadata responseHeaders) {
        responseHeaders.put(REFRESH_TOKEN_HEADER, jwtService.createToken(uuid));
        super.sendHeaders(responseHeaders);
      }
    };
  }

  @Nonnull
  private static String getAuthorizationHeader(@Nonnull Metadata headers) {
    return Optional
        .ofNullable(headers.get(AUTHORIZATION_METADATA_KEY))
        .orElseThrow(() -> new JWTVerificationException("Authorization token is missing"));
  }

  @Nonnull
  private static String validatePrefixAndReturnRawToken(@Nonnull String headerValue) {
    return Optional
        .of(headerValue)
        .filter(token -> token.startsWith(TOKEN_TYPE))
        .map(token -> token.substring(TOKEN_TYPE.length()).trim())
        .orElseThrow(() -> new JWTVerificationException("Unknown authorization type"));
  }

  @Nonnull
  private static String getFullJavaMethodName(@Nonnull MethodDescriptor<?, ?> methodDescriptor) {
    return String
        .format("%s.%s", methodDescriptor.getServiceName(), methodDescriptor.getBareMethodName())
        .toLowerCase();
  }

}
