package com.ulashchick.dashboard.common.config.pojo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = CassandraConfig.Builder.class)
public class CassandraConfig {

  private final String host;
  private final int port;

  private CassandraConfig(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  @JsonPOJOBuilder
  public static class Builder {

    private String host;
    private int port;

    public Builder withHost(String host) {
      this.host = host;
      return this;
    }

    public Builder withPort(int port) {
      this.port = port;
      return this;
    }

    public CassandraConfig build() {
      return new CassandraConfig(host, port);
    }
  }

}
