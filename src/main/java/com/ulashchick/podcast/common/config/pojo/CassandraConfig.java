package com.ulashchick.podcast.common.config.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@AllArgsConstructor
@Jacksonized
@Builder
public class CassandraConfig {

  private final String host;
  private final int port;

}
