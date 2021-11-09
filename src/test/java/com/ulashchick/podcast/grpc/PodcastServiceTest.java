package com.ulashchick.podcast.grpc;

import com.ulashchick.podcast.TestParameterResolver;
import io.reactivex.Single;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import protos.com.ulashchick.podcast.podcast.RecentFeedsRequest;
import protos.com.ulashchick.podcast.podcast.RecentFeedsResponse;

import javax.annotation.Nonnull;

import static com.google.common.truth.Truth.assertThat;

@ExtendWith(TestParameterResolver.class)
class PodcastServiceTest {

  private final PodcastService podcastService;

  public PodcastServiceTest(@Nonnull PodcastService podcastService) {
    this.podcastService = podcastService;
  }

  @Test
  void testGetFeed() {
    final RecentFeedsRequest request = RecentFeedsRequest.getDefaultInstance();
    final RecentFeedsResponse recentFeedsResponse = podcastService.recentFeeds(Single.just(request)).blockingGet();

    assertThat(recentFeedsResponse.getFeedsCount()).isAtLeast(1);
  }
}
