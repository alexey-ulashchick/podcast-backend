package com.ulashchick.podcast.thirdpartyapi.podcastindex;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Suppliers;
import com.google.common.primitives.Bytes;
import com.google.inject.Inject;
import com.ulashchick.podcast.common.config.EnvironmentService;
import com.ulashchick.podcast.thirdpartyapi.podcastindex.pojo.Feed;
import com.ulashchick.podcast.thirdpartyapi.podcastindex.pojo.RecentFeeds;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.reactivex.Single;

import javax.annotation.Nonnull;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PodcastIndexClient {
  private static final String URL_PREFIX = "https://api.podcastindex.org/api/1.0/";
  private static final String RECENT_FEED_URL_SUFFIX = "recent/feeds";

  private static final long TIMEOUT_S = 15;
  private final Supplier<HttpClient> clientSupplier;
  private final EnvironmentService environmentService;

  @Inject
  PodcastIndexClient(@Nonnull ExecutorService executorService,
                     @Nonnull EnvironmentService environmentService) {
    this.environmentService = environmentService;
    this.clientSupplier = Suppliers.memoize(() -> HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(TIMEOUT_S))
        .executor(executorService)
        .build()
    )::get;
  }

  @Nonnull
  public Single<List<Feed>> getRecentFeeds(int limit) {
    final ObjectMapper objectMapper = new ObjectMapper();


    final HttpRequest httpRequest = buildRequestHeader()
        .GET()
        .uri(URI.create(String.format("%s/%s?max=%d", URL_PREFIX, RECENT_FEED_URL_SUFFIX, limit)))
        .build();

    return Single.fromFuture(clientSupplier.get().sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString()))
        .map(response -> objectMapper.readValue(response.body(), RecentFeeds.class))
        .map(RecentFeeds::getFeeds);
  }

  @Nonnull
  private HttpRequest.Builder buildRequestHeader() {
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    calendar.clear();
    calendar.setTime(new Date());

    final String apiHeaderTime = String.valueOf(calendar.getTimeInMillis() / 1_000L);
    final String apiKey = environmentService.getPodcastIndexKey();
    final String apiSecret = environmentService.getPodcastIndexSecret();

    return HttpRequest.newBuilder()
        .timeout(Duration.ofSeconds(TIMEOUT_S))
        .header("Accept", "application/json")
        .header("Content-Type", "application/json; utf-8")
        .header("X-Auth-Date", apiHeaderTime)
        .header("X-Auth-Key", apiKey)
        .header("Authorization", getAuthorizationHeaderValue(apiKey, apiSecret, apiHeaderTime))
        .header("User-Agent", "https://github.com/alexey-ulashchick/podcast-backend");
  }

  @Nonnull
  private String getAuthorizationHeaderValue(@Nonnull String apiKey,
                                             @Nonnull String apiSecret,
                                             @Nonnull String apiHeaderTime) {
    try {
      final MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
      messageDigest.update((apiKey + apiSecret + apiHeaderTime).getBytes(StandardCharsets.UTF_8));

      return Bytes.asList(messageDigest.digest())
          .stream()
          .map(b -> String.format("%02x", b))
          .collect(Collectors.joining());

    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      throw new StatusRuntimeException(Status.INTERNAL);
    }
  }
}
