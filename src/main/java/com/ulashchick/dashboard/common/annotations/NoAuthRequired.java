package com.ulashchick.dashboard.common.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation helps identify method of GRPC Service with allows access without authorization.
 * Methods located with this annotation will be exclusion list of request interceptor in {@see
 * ApplicationServerBuilder#initInterceptor}.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface NoAuthRequired {

}
