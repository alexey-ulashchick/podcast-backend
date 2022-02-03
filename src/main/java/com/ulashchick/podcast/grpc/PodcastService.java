package com.ulashchick.podcast.grpc;

import com.google.inject.Inject;
import com.ulashchick.podcast.common.annotations.GrpcService;
import com.ulashchick.podcast.thirdpartyapi.podcastindex.PodcastIndexClient;
import io.reactivex.Single;
import protos.com.ulashchick.podcast.podcast.*;

import javax.annotation.Nonnull;
import java.util.List;
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
        .map(this::toProtoFeeds)
        .map(recentFeeds -> RecentFeedsResponse.newBuilder().addAllFeeds(recentFeeds).build());
  }

  @Override
  public Single<SearchResponse> search(Single<SearchRequest> request) {
    return request
        .map(SearchRequest::getQuery)
        .flatMap(podcastIndexClient::searchFeeds)
        .map(this::toProtoFeeds)
        .map(feeds -> SearchResponse.newBuilder().addAllFeeds(feeds).build());
  }

  @Override
  public Single<SubscribeMeToResponse> subscribeMeTo(Single<SubscribeMeToRequest> request) {
    return super.subscribeMeTo(request);
  }

  @Override
  public Single<UnsubsribeMeFromResponse> unsubsribeMeFrom(Single<UnsubsribeMeFromRequest> request) {
    return super.unsubsribeMeFrom(request);
  }

  @Override
  public Single<ListMySubscriptionsResponse> listMySubscriptions(Single<ListMySubscriptionsRequest> request) {
    return super.listMySubscriptions(request);
  }

  private List<Feed> toProtoFeeds(@Nonnull List<com.ulashchick.podcast.thirdpartyapi.podcastindex.pojo.Feed> feeds) {
    return feeds.stream()
        .map(feed -> Feed.newBuilder()
            .setId(feed.getId())
            .setTitle(feed.getTitle())
            .setDescription(feed.getDescription())
            .setImage(feed.getImage())
            .setLanguage(feed.getLanguage())
            .build())
        .collect(Collectors.toList());
  }


}
