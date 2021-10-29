package com.ulashchick.podcast.grpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Suppliers;
import com.google.common.primitives.Bytes;
import com.google.inject.Inject;
import com.ulashchick.podcast.common.annotations.GrpcService;
import com.ulashchick.podcast.common.config.EnvironmentService;
import com.ulashchick.podcast.grpc.pojo.RecentFeeds;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.reactivex.Single;
import protos.com.ulashchick.application.podcast.Feed;
import protos.com.ulashchick.application.podcast.RecentFeedsRequest;
import protos.com.ulashchick.application.podcast.RecentFeedsResponse;
import protos.com.ulashchick.application.podcast.RxPodcastServiceGrpc;

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
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@GrpcService
public class PodcastService extends RxPodcastServiceGrpc.PodcastServiceImplBase {

  private static final String API_KEY = "PODCAST_INDEX_API_KEY";
  private static final String API_SECRET = "PODCAST_INDEX_API_SECRET";
  private static final String URL = "https://api.podcastindex.org/api/1.0/recent/feeds";
  private static final long TIMEOUT = 15;

  private final Supplier<HttpClient> clientSupplier;
  private final EnvironmentService environmentService;

  @Inject
  PodcastService(@Nonnull ExecutorService executorService,
                 @Nonnull EnvironmentService environmentService) {
    this.environmentService = environmentService;
    this.clientSupplier = Suppliers.memoize(() -> HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(20))
        .executor(executorService)
        .build()
    )::get;
  }

  @Override
  public Single<RecentFeedsResponse> recentFeeds(Single<RecentFeedsRequest> recentFeedsRequestSingle) {
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    calendar.clear();
    calendar.setTime(new Date());

    final String apiHeaderTime = String.valueOf(calendar.getTimeInMillis() / 1000L);
    final String apiKey = getApiKey();
    final String apiSecret = getApiSecret();
    final ObjectMapper objectMapper = new ObjectMapper();
    final HttpRequest request = HttpRequest.newBuilder()
        .timeout(Duration.ofSeconds(TIMEOUT))
        .header("Accept", "application/json")
        .header("Content-Type", "application/json; utf-8")
        .header("X-Auth-Date", apiHeaderTime)
        .header("X-Auth-Key", apiKey)
        .header("Authorization", getAuthorizationHeaderValue(apiKey, apiSecret, apiHeaderTime))
        .header("User-Agent", "https://github.com/alexey-ulashchick/podcast-backend")
        .GET()
        .uri(URI.create(URL))
        .build();


    return Single.fromFuture(clientSupplier.get().sendAsync(request, HttpResponse.BodyHandlers.ofString()))
        .map(response -> objectMapper.readValue(response.body(), RecentFeeds.class))
        .map(recentFeeds -> RecentFeedsResponse.newBuilder()
            .addAllFeeds(recentFeeds.getFeeds()
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

  private String getApiKey() {
    return environmentService.readEnvVariable(API_KEY);
  }

  private String getApiSecret() {
    return environmentService.readEnvVariable(API_SECRET);
  }

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
