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
import protos.com.ulashchick.podcast.podcast.Feed;
import protos.com.ulashchick.podcast.podcast.ListMySubscriptionsRequest;
import protos.com.ulashchick.podcast.podcast.ListMySubscriptionsResponse;
import protos.com.ulashchick.podcast.podcast.PodcastServiceGrpc;

import java.util.UUID;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class AuthorizationTest extends AbstractIntegrationTest {
  private static final UUID USER_ID = Uuids.timeBased();

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
    final CassandraClient cassandraClient = DependencyManager.getInstance(CassandraClient.class);
    cassandraClient.addSubscription(USER_ID, 123_456L).blockingAwait();

    final String jwtToken = DependencyManager.getInstance(JwtService.class).createToken(USER_ID);
    final BearerToken bearerToken = new BearerToken(jwtToken);

    final PodcastServiceGrpc.PodcastServiceBlockingStub podcastServiceBlockingStub = PodcastServiceGrpc
        .newBlockingStub(grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build()))
        .withCallCredentials(bearerToken);

    final ListMySubscriptionsRequest request = ListMySubscriptionsRequest.getDefaultInstance();
    final ListMySubscriptionsResponse response = podcastServiceBlockingStub.listMySubscriptions(request);

    final Feed feed = Feed.newBuilder().setId(123_456L).build();

    assertThat(response.getFeedsList()).containsExactly(feed);
  }
}
