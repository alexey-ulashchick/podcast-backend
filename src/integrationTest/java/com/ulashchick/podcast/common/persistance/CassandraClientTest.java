package com.ulashchick.podcast.common.persistance;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.ulashchick.podcast.TestParameterResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

import static com.google.common.truth.Truth.assertThat;

@ExtendWith(TestParameterResolver.class)
public class CassandraClientTest {

  private static final UUID USER_ID = Uuids.timeBased();
  private static final long SUBSCRIPTION_ID = 1_123L;

  private final CassandraClient cassandraClient;

  public CassandraClientTest(@Nonnull CassandraClient cassandraClient) {
    this.cassandraClient = cassandraClient;
    this.cassandraClient.init();
  }

  @Test
  void testSubscription() {
     cassandraClient.addSubscription(USER_ID, SUBSCRIPTION_ID).blockingAwait();

    List<Long> longs = cassandraClient.getSubscriptionsList(USER_ID).blockingGet();

    assertThat(longs).containsExactly(SUBSCRIPTION_ID);

    cassandraClient.removeSubscription(USER_ID, SUBSCRIPTION_ID).blockingAwait();

    List<Long> noSubscriptions = cassandraClient.getSubscriptionsList(USER_ID).blockingGet();

    assertThat(noSubscriptions).isEmpty();
  }
}
