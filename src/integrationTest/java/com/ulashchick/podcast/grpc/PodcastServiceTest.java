package com.ulashchick.podcast.grpc;

import com.ulashchick.podcast.TestParameterResolver;
import com.ulashchick.podcast.common.config.EnvironmentService;
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
  private final EnvironmentService environmentService;

  public PodcastServiceTest(@Nonnull PodcastService podcastService, @Nonnull EnvironmentService environmentService) {
    this.podcastService = podcastService;
    this.environmentService = environmentService;
  }

  @Test
  void testGetFeed() {
    assertThat(environmentService.readEnvVariable("PODCAST_INDEX_KEY")).isNotEmpty();
    assertThat(environmentService.readEnvVariable("PODCAST_INDEX_SECRET")).isNotEmpty();

    final RecentFeedsRequest request = RecentFeedsRequest.getDefaultInstance();
    final RecentFeedsResponse recentFeedsResponse = podcastService.recentFeeds(Single.just(request)).blockingGet();

    assertThat(recentFeedsResponse.getFeedsCount()).isAtLeast(1);
  }
}
