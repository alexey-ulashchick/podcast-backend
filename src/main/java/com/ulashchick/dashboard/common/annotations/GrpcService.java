package com.ulashchick.dashboard.common.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation helps identify GRPC Services. Services located with this annotation will be
 * automatically added to server configuration in {@see ApplicationServerBuilder#bindAnnotatedServices}.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface GrpcService {

}
