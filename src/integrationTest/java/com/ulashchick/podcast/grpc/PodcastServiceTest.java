package com.ulashchick.podcast.grpc;

import com.ulashchick.podcast.TestParameterResolver;
import com.ulashchick.podcast.common.config.EnvironmentService;
import io.reactivex.Single;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import protos.com.ulashchick.podcast.podcast.*;

import javax.annotation.Nonnull;

import static com.google.common.truth.Truth.assertThat;

@ExtendWith(TestParameterResolver.class)
class PodcastServiceTest {

  private final PodcastService podcastService;
  private final EnvironmentService environmentService;

  public PodcastServiceTest(@Nonnull PodcastService podcastService,
                            @Nonnull EnvironmentService environmentService) {
    this.podcastService = podcastService;
    this.environmentService = environmentService;
  }

  @Test
  void testGetFeed() {
    assertThat(environmentService.readEnvVariable("PODCAST_INDEX_KEY")).isNotEmpty();
    assertThat(environmentService.readEnvVariable("PODCAST_INDEX_SECRET")).isNotEmpty();

    final RecentFeedsRequest request = RecentFeedsRequest.newBuilder().setMax(10).build();
    final RecentFeedsResponse recentFeedsResponse = podcastService.recentFeeds(Single.just(request)).blockingGet();

    assertThat(recentFeedsResponse.getFeedsCount()).isEqualTo(10);
  }

  @Test
  void testSearch() {
    final SearchRequest request = SearchRequest.newBuilder().setQuery("РАДИО-Т").build();
    final SearchResponse searchResponse = podcastService.search(Single.just(request)).blockingGet();

    assertThat(searchResponse.getFeedsCount()).isAtLeast(1);

    final Feed feed = searchResponse.getFeeds(0);

    assertThat(feed.getLanguage()).isEqualTo("ru");
    assertThat(feed.getTitle()).isEqualTo("Радио-Т");
  }
}
