package com.ulashchick.podcast.common.config.pojo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.List;
import javax.annotation.Nonnull;

@JsonDeserialize(builder = ApplicationConfig.Builder.class)
public class ApplicationConfig {

  private final List<CassandraConfig> cassandraConfig;
  private final GrpcServerConfig grpcServerConfig;

  public ApplicationConfig(List<CassandraConfig> cassandraConfig,
                           GrpcServerConfig grpcServerConfig) {

    this.cassandraConfig = cassandraConfig;
    this.grpcServerConfig = grpcServerConfig;
  }

  public List<CassandraConfig> getCassandraConfig() {
    return cassandraConfig;
  }

  public GrpcServerConfig getGrpcServerConfig() {
    return grpcServerConfig;
  }

  @JsonPOJOBuilder
  public static class Builder {

    private List<CassandraConfig> cassandraConfig;
    private GrpcServerConfig grpcServerConfig;

    public Builder withCassandraConfig(@Nonnull List<CassandraConfig> cassandraConfig) {
      this.cassandraConfig = cassandraConfig;
      return this;
    }

    public Builder withGrpcServerConfig(@Nonnull GrpcServerConfig grpcServerConfig) {
      this.grpcServerConfig = grpcServerConfig;
      return this;
    }

    public ApplicationConfig build() {
      return new ApplicationConfig(cassandraConfig, grpcServerConfig);
    }
  }
}
