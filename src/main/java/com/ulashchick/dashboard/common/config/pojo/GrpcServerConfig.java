package com.ulashchick.dashboard.common.config.pojo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = GrpcServerConfig.Builder.class)
public class GrpcServerConfig {

  private final int port;

  public GrpcServerConfig(int port) {
    this.port = port;
  }

  public int getPort() {
    return port;
  }

  @JsonPOJOBuilder
  public static class Builder {

    private int port;

    public Builder withPort(int port) {
      this.port = port;
      return this;
    }

    public GrpcServerConfig build() {
      return new GrpcServerConfig(port);
    }
  }
}
