package com.ulashchick.podcast;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.checkerframework.common.value.qual.IntRange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.truth.Truth.assertThat;

class HttpPerformanceTest {

  private static final int RESPONSE_DELAY_S = 5;
  private static final int REQUEST_COUNT = 5;

  private final HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
  private final AtomicInteger reqNumber = new AtomicInteger();
  private final ScheduledExecutorService responseExecutorService = Executors.newScheduledThreadPool(1);
  private final ExecutorService httpExecutorService = Executors.newSingleThreadExecutor();

  private final URI uri = new URI("http://localhost:8080/test");
  private final HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

  HttpPerformanceTest() throws IOException, URISyntaxException {
  }

  @BeforeEach
  void before() {
    server.setExecutor(httpExecutorService);
    server.createContext("/test", exchange -> {
      final String response = "OK_" + reqNumber.getAndIncrement();
      responseExecutorService.schedule(() ->
          sendResponse(200, response, exchange), RESPONSE_DELAY_S, TimeUnit.SECONDS);
    });

    server.start();
  }

  @AfterEach
  void after() {
    server.stop(0);
  }

  @Test
  void testStandardHttpClient() throws ExecutionException, InterruptedException {
    final ExecutorService executorService = Executors.newSingleThreadExecutor();
    final Instant start = Instant.now();

    final List<CompletableFuture<HttpResponse<String>>> futures = IntStream.range(0, REQUEST_COUNT)
        .boxed()
        .map(unused -> HttpClient
            .newBuilder()
            .executor(executorService)
            .build()
            .sendAsync(request, HttpResponse.BodyHandlers.ofString()))
        .collect(Collectors.toList());

    final Set<String> commonResponse = CompletableFuture
        .allOf(futures.toArray(CompletableFuture[]::new))
        .handle((unused, ex) -> futures.stream()
            .map(future -> Optional.ofNullable(future.getNow(null)))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(HttpResponse::body)
            .collect(Collectors.toSet())
        )
        .get();

    final Instant end = Instant.now();
    final long durationSeconds = Duration.between(start, end).getSeconds();
    final Set<String> expectedResponses = IntStream.range(0, REQUEST_COUNT)
        .boxed()
        .map(value -> "OK_" + value)
        .collect(Collectors.toSet());

    assertThat(commonResponse).containsExactlyElementsIn(expectedResponses);
    assertThat(durationSeconds).isAtLeast(RESPONSE_DELAY_S - 1);
    assertThat(durationSeconds).isAtMost(RESPONSE_DELAY_S + 1);
  }

  private void sendResponse(int code,
                            @Nonnull String body,
                            @Nonnull HttpExchange httpExchange) {
    try {
      httpExchange.sendResponseHeaders(code, body.length());
      OutputStream responseBody = httpExchange.getResponseBody();
      responseBody.write(body.getBytes());
      responseBody.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
