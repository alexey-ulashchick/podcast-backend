package com.ulashchick.podcast.thirdpartyapi.podcastindex.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Getter
@AllArgsConstructor
@Jacksonized
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class RecentFeeds {

  private final List<Feed> feeds;

}
