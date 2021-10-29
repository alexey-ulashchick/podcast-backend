package com.ulashchick.podcast.common.config.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Getter
@AllArgsConstructor
@Jacksonized
@Builder
public class ApplicationConfig {

  private final List<CassandraConfig> cassandraConfig;
  private final GrpcServerConfig grpcServerConfig;

}
