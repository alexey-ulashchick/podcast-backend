package com.ulashchick.podcast.grpc.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@AllArgsConstructor
@Jacksonized
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Feed {

  private final long id;
  private final String title;
  private final String description;
  private final String image;
  private final String language;

}
