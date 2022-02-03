package com.ulashchick.podcast.grpc;

import com.google.inject.Inject;
import com.ulashchick.podcast.common.annotations.GrpcService;
import com.ulashchick.podcast.thirdpartyapi.podcastindex.PodcastIndexClient;
import io.reactivex.Single;
import protos.com.ulashchick.podcast.podcast.Feed;
import protos.com.ulashchick.podcast.podcast.RecentFeedsRequest;
import protos.com.ulashchick.podcast.podcast.RecentFeedsResponse;
import protos.com.ulashchick.podcast.podcast.RxPodcastServiceGrpc;

import javax.annotation.Nonnull;
import java.util.stream.Collectors;

@GrpcService
public class PodcastService extends RxPodcastServiceGrpc.PodcastServiceImplBase {

  private final PodcastIndexClient podcastIndexClient;

  @Inject
  PodcastService(@Nonnull PodcastIndexClient podcastIndexClient) {
    this.podcastIndexClient = podcastIndexClient;
  }

  @Override
  public Single<RecentFeedsResponse> recentFeeds(Single<RecentFeedsRequest> recentFeedsRequestSingle) {
    return recentFeedsRequestSingle
        .map(RecentFeedsRequest::getMax)
        .flatMap(podcastIndexClient::getRecentFeeds)
        .map(recentFeeds -> RecentFeedsResponse.newBuilder()
            .addAllFeeds(recentFeeds
                .stream()
                .map(feed -> Feed.newBuilder()
                    .setId(feed.getId())
                    .setTitle(feed.getTitle())
                    .setDescription(feed.getDescription())
                    .setImage(feed.getImage())
                    .setLanguage(feed.getLanguage())
                    .build())
                .collect(Collectors.toList()))
            .build());
  }

}
