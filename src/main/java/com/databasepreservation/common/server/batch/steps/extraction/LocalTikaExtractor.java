package com.databasepreservation.common.server.batch.steps.extraction;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import com.databasepreservation.common.client.ViewerConstants;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class LocalTikaExtractor implements LobTextExtractor {
  private final HttpClient httpClient;
  private final String tikaURL;
  private final String tikaVolumePathConfig;
  private final long timeoutSeconds;

  public LocalTikaExtractor(HttpClient httpClient, String tikaURL, String tikaVolumePathConfig, long timeoutSeconds) {
    if (tikaURL == null || tikaURL.isBlank()) {
      throw new IllegalStateException("Tika server URL is not configured.");
    }
    this.httpClient = httpClient;
    this.tikaURL = tikaURL;
    this.tikaVolumePathConfig = tikaVolumePathConfig;
    this.timeoutSeconds = timeoutSeconds;
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

    HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    return response.body();
  }
}
