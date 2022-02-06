package com.ulashchick.podcast.grpc;

import com.google.inject.Inject;
import com.ulashchick.podcast.auth.AuthInterceptor;
import com.ulashchick.podcast.common.annotations.GrpcService;
import com.ulashchick.podcast.common.persistance.CassandraClient;
import com.ulashchick.podcast.thirdpartyapi.podcastindex.PodcastIndexClient;
import io.reactivex.Single;
import protos.com.ulashchick.podcast.podcast.*;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@GrpcService
public class PodcastService extends RxPodcastServiceGrpc.PodcastServiceImplBase {

  private final PodcastIndexClient podcastIndexClient;
  private final CassandraClient cassandraClient;

  @Inject
  PodcastService(@Nonnull PodcastIndexClient podcastIndexClient,
                 @Nonnull CassandraClient cassandraClient) {
    this.podcastIndexClient = podcastIndexClient;
    this.cassandraClient = cassandraClient;
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
    final UUID uuid = AuthInterceptor.UUID_KEY.get();

    return request
        .flatMap(unused -> cassandraClient.getSubscriptionsList(uuid))
        .map(listSubscriptionIds -> listSubscriptionIds.stream()
            .map(id -> Feed.newBuilder()
                .setId(id)
                .build())
            .collect(Collectors.toList()))
        .map(feedsList -> ListMySubscriptionsResponse.newBuilder()
            .addAllFeeds(feedsList)
            .build());
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
