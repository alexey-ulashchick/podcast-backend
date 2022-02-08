package com.ulashchick.podcast.common.persistance;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.ulashchick.podcast.common.DependencyManager;
import com.ulashchick.podcast.common.config.ConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import protos.com.ulashchick.podcast.auth.UserProfile;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;

import static com.google.common.truth.Truth.assertThat;

@ExtendWith(MockitoExtension.class)
@Testcontainers
class CassandraClientTest {

  private static final UUID USER_ID = Uuids.timeBased();
  private static final long SUBSCRIPTION_ID = 1_123L;

  @Container
  @SuppressWarnings({"rawtypes"})
  private final CassandraContainer cassandra = new CassandraContainer(DockerImageName.parse("cassandra:latest"));

  @Spy
  private final ConfigService configService = DependencyManager.getInstance(ConfigService.class);

  private CassandraClient cassandraClient;

  @BeforeEach
  void beforeEach() {
    Mockito.when(configService.getCassandraEndpoints())
        .thenReturn(List.of(new InetSocketAddress(cassandra.getHost(), cassandra.getFirstMappedPort())));

    DependencyManager.overrideForTest(ConfigService.class, configService);

    cassandraClient = DependencyManager.getInstance(CassandraClient.class);
    cassandraClient.init();
  }

  @Test
  void testSubscription() {
    cassandraClient.addSubscription(USER_ID, SUBSCRIPTION_ID).blockingAwait();

    assertThat(cassandraClient.getSubscriptionsList(USER_ID).blockingGet()).containsExactly(SUBSCRIPTION_ID);

    cassandraClient.removeSubscription(USER_ID, SUBSCRIPTION_ID).blockingAwait();

    assertThat(cassandraClient.getSubscriptionsList(USER_ID).blockingGet()).isEmpty();
  }

  @Test
  void firstUserSignIn() {
    final UserProfile userProfile = UserProfile.newBuilder()
        .setFirstName("FIRST_NAME")
        .setLastName("LAST_NAME")
        .setEmail("test@test.com")
        .setPictureUrl("https://test.com")
        .build();

    final UUID uuid = cassandraClient.upsertUser(userProfile).blockingGet();
    final UUID userUuid = cassandraClient.getUserUUIDByEmail(userProfile.getEmail()).blockingGet();

    assertThat(uuid).isEqualTo(userUuid);
  }

  @Test
  void checkUserUpsertionDoesNotChangeUUID() {
    final UserProfile initialProfile = UserProfile.newBuilder()
        .setFirstName("FIRST_NAME")
        .setLastName("LAST_NAME")
        .setEmail("test@test.com")
        .setPictureUrl("https://test.com")
        .build();

    final UserProfile alteredProfile = initialProfile.toBuilder()
        .setLastName("TEST")
        .build();

    final UUID initialUUID = cassandraClient.upsertUser(initialProfile).blockingGet();

    assertThat(cassandraClient.getUserProfileByEmail(initialProfile.getEmail()).blockingGet()).isEqualTo(initialProfile);

    final UUID updatedUUID = cassandraClient.upsertUser(alteredProfile).blockingGet();

    assertThat(initialUUID).isEqualTo(updatedUUID);
    assertThat(cassandraClient.getUserProfileByEmail(initialProfile.getEmail()).blockingGet()).isEqualTo(alteredProfile);
    assertThat(cassandraClient.getUserUUIDByEmail(alteredProfile.getEmail()).blockingGet()).isEqualTo(initialUUID);
  }
}
