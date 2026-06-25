package com.databasepreservation.common.server.batch.steps.extraction;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.ViewerConstants;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class LocalTikaExtractor implements LobTextExtractor {
  private final HttpClient httpClient;
  private final String tikaURL;
  private final String tikaVolumePathConfig;
  private final long timeoutSeconds;
  private final int tikaRetries;
  private final long tikaRetryDelay;

  public LocalTikaExtractor(HttpClient httpClient, String tikaURL, String tikaVolumePathConfig, long timeoutSeconds,
    int tikaRetries, long tikaRetryDelay) {
    if (tikaURL == null || tikaURL.isBlank()) {
      throw new IllegalStateException("Tika server URL is not configured.");
    }
    this.httpClient = httpClient;
    this.tikaURL = tikaURL;
    this.tikaVolumePathConfig = tikaVolumePathConfig;
    this.timeoutSeconds = timeoutSeconds;
    this.tikaRetries = tikaRetries;
    this.tikaRetryDelay = tikaRetryDelay;
  }

  @Override
  public String extractText(Path lobPath, ExtractionContext context) throws Exception {
    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
      .uri(URI.create(tikaURL + ViewerConstants.TIKA_EXTRACT_ENDPOINT)).header("Accept", "text/plain")
      .timeout(Duration.ofSeconds(timeoutSeconds));

    if (tikaVolumePathConfig != null && !tikaVolumePathConfig.isBlank() && lobPath.startsWith(tikaVolumePathConfig)) {
      Path tikaVolumePath = Paths.get(tikaVolumePathConfig);
      requestBuilder.header("fetcherName", "fsf").header("fetchKey", tikaVolumePath.relativize(lobPath).toString())
        .PUT(HttpRequest.BodyPublishers.noBody());
    } else {
      requestBuilder.PUT(HttpRequest.BodyPublishers.ofInputStream(() -> {
        try {
          return Files.newInputStream(lobPath);
        } catch (IOException e) {
          throw new UncheckedIOException("Failed to read LOB file for Tika extraction: " + lobPath, e);
        }
      }));
    }

    RetryPolicy<Object> retryPolicy = RetryPolicy.builder().handle(IOException.class)
      .withDelay(Duration.ofSeconds(tikaRetryDelay)).withMaxRetries(tikaRetries)
      .onFailedAttempt(e -> LoggerFactory.getLogger(LocalTikaExtractor.class)
        .warn("({}/10): Tika extraction request failed, retrying.", e.getAttemptCount()))
      .build();
    HttpResponse<String> response = Failsafe.with(retryPolicy)
      .get(() -> httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)));

    return response.body();
  }
}
