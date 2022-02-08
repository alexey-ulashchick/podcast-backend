package com.ulashchick.podcast.grpc;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.ulashchick.podcast.AbstractIntegrationTest;
import com.ulashchick.podcast.auth.BearerToken;
import com.ulashchick.podcast.auth.JwtService;
import com.ulashchick.podcast.common.DependencyManager;
import com.ulashchick.podcast.common.persistance.CassandraClient;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import org.junit.jupiter.api.Test;
import protos.com.ulashchick.podcast.podcast.*;

import java.util.UUID;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


class PodcastServiceIntegrationAuthTest extends AbstractIntegrationTest {
  private static final UUID USER_ID = Uuids.timeBased();
  public static final long FEED_ID = 123_456L;

  @Test
  @SuppressWarnings({"squid:S5960"})
  void testPodcastServiceDoesNotWorkWithoutAuth() {
    final PodcastServiceGrpc.PodcastServiceBlockingStub podcastServiceBlockingStub = PodcastServiceGrpc
        .newBlockingStub(grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build()));

    final ListMySubscriptionsRequest request = ListMySubscriptionsRequest.getDefaultInstance();

    final StatusRuntimeException e = assertThrows(StatusRuntimeException.class,
        () -> podcastServiceBlockingStub.listMySubscriptions(request));

    assertThat(e.getMessage()).contains("UNAUTHENTICATED");
  }

  @Test
  void testGettingListOfSubscriptionsWhenAuthorized() {
    logger.info("Adding subscription into DB for userId={}, feedId={}", USER_ID, FEED_ID);

    DependencyManager.getInstance(CassandraClient.class)
        .addSubscription(USER_ID, FEED_ID)
        .blockingAwait();

    final PodcastServiceGrpc.PodcastServiceBlockingStub podcastServiceBlockingStub = setupPodcastServiceWithAuth();
    final ListMySubscriptionsRequest request = ListMySubscriptionsRequest.getDefaultInstance();
    final ListMySubscriptionsResponse response = podcastServiceBlockingStub.listMySubscriptions(request);
    final Feed feed = Feed.newBuilder().setId(FEED_ID).build();

    assertThat(response.getFeedsList()).containsExactly(feed);
  }

  @Test
  void testSubscriptionsAndUnsubscribes() {
    final PodcastServiceGrpc.PodcastServiceBlockingStub podcastServiceBlockingStub = setupPodcastServiceWithAuth();

    podcastServiceBlockingStub.subscribeMeTo(SubscribeMeToRequest.newBuilder().setFeedId(FEED_ID).buildPartial());
    podcastServiceBlockingStub.subscribeMeTo(SubscribeMeToRequest.newBuilder().setFeedId(FEED_ID + 1).buildPartial());

    final ListMySubscriptionsResponse response = podcastServiceBlockingStub
        .listMySubscriptions(ListMySubscriptionsRequest.getDefaultInstance());

    assertThat(response.getFeedsList()).hasSize(2);
    assertThat(response.getFeedsList()).contains(Feed.newBuilder().setId(FEED_ID).build());
    assertThat(response.getFeedsList()).contains(Feed.newBuilder().setId(FEED_ID + 1).build());

    podcastServiceBlockingStub.unsubsribeMeFrom(UnsubsribeMeFromRequest.newBuilder().setFeedId(FEED_ID).build());
    podcastServiceBlockingStub.unsubsribeMeFrom(UnsubsribeMeFromRequest.newBuilder().setFeedId(FEED_ID + 1).build());

    final ListMySubscriptionsResponse responseAfter = podcastServiceBlockingStub
        .listMySubscriptions(ListMySubscriptionsRequest.getDefaultInstance());

    assertThat(responseAfter.getFeedsList()).isEmpty();
  }

  private PodcastServiceGrpc.PodcastServiceBlockingStub setupPodcastServiceWithAuth() {
    final String jwtToken = DependencyManager.getInstance(JwtService.class).createToken(USER_ID);
    final BearerToken bearerToken = new BearerToken(jwtToken);

    return PodcastServiceGrpc
        .newBlockingStub(grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build()))
        .withCallCredentials(bearerToken);
  }
}
