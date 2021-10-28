package com.ulashchick.podcast.auth;

import com.google.inject.Inject;
import com.ulashchick.podcast.common.annotations.GrpcService;
import io.reactivex.Single;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;

import protos.com.ulashchick.application.podcast.RecentFeedsRequest;
import protos.com.ulashchick.application.podcast.RecentFeedsResponse;
import protos.com.ulashchick.application.podcast.RxPodcastServiceGrpc;

import javax.annotation.Nonnull;

@GrpcService
public class PodcastService extends RxPodcastServiceGrpc.PodcastServiceImplBase {

  private final ExecutorService executorService;

  @Inject
  PodcastService(@Nonnull ExecutorService executorService) {
    this.executorService = executorService;
  }

  @Override
  public Single<RecentFeedsResponse> recentFeeds(Single<RecentFeedsRequest> recentFeedsRequestSingle) {
    final HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://foo.com/"))
            .timeout(Duration.ofMillis(15_000))
            .header("Content-Type", "application/json")
            .GET()
            .build();

    final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(20))
            .executor(executorService)
            .build();

    return Single.fromFuture(client.sendAsync(request, HttpResponse.BodyHandlers.ofString()))
            .map(response -> RecentFeedsResponse.newBuilder().build());
  }

}
