package com.databasepreservation.common.server.batch.steps.extraction;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;

import com.databasepreservation.common.server.batch.exceptions.DataTransformationException;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ExternalServiceExtractor implements LobTextExtractor {
  private final HttpClient httpClient;
  private final String externalOcrServiceUrl;
  private final String externalIdPattern;
  private final String authHeader;
  private final long timeoutSeconds;

  public ExternalServiceExtractor(HttpClient httpClient, String externalOcrServiceUrl, String externalIdPattern,
    String user, String password, long timeoutSeconds) {
    this.httpClient = httpClient;
    this.externalOcrServiceUrl = externalOcrServiceUrl;
    this.externalIdPattern = externalIdPattern;
    this.timeoutSeconds = timeoutSeconds;

    if (user != null && !user.isBlank() && password != null && !password.isBlank()) {
      String auth = user + ":" + password;
      this.authHeader = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    } else {
      this.authHeader = null;
    }
  }

  @Override
  public String extractText(Path lobPath, ExtractionContext context) throws Exception {
    String extension = getExtension(lobPath);
    String extractionId = generateExtractionId(context, extension);

    String encodedId = URLEncoder.encode(extractionId, StandardCharsets.UTF_8);
    String requestUrl = externalOcrServiceUrl.contains("{id}") ? externalOcrServiceUrl.replace("{id}", encodedId)
      : (externalOcrServiceUrl.endsWith("/") || externalOcrServiceUrl.endsWith("=") ? externalOcrServiceUrl + encodedId
        : externalOcrServiceUrl + "/" + encodedId);

    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(URI.create(requestUrl))
      .timeout(Duration.ofSeconds(timeoutSeconds)).GET();

    if (authHeader != null) {
      requestBuilder.header("Authorization", authHeader);
    }

    HttpResponse<String> response = httpClient.send(requestBuilder.build(),
      HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    if (response.statusCode() >= 200 && response.statusCode() < 300) {
      return response.body();
    }
    throw new DataTransformationException("External OCR Service failed with status: " + response.statusCode());
  }

  private String generateExtractionId(ExtractionContext ctx, String extension) {
    if (externalIdPattern == null || externalIdPattern.isBlank())
      return "";
    return externalIdPattern.replace("{db}", ctx.databaseUUID()).replace("{collection}", ctx.databaseUUID())
      .replace("{schema}", ctx.schemaName()).replace("{table}", ctx.tableName()).replace("{row}", ctx.rowUuid())
      .replace("{col}", String.valueOf(ctx.columnIndex())).replace("{ext}", extension);
  }

  private String getExtension(Path path) {
    String fileName = path.getFileName().toString();
    int dotIndex = fileName.lastIndexOf('.');
    if (dotIndex > 0) {
      return fileName.substring(dotIndex).toLowerCase();
    }
    return "";
  }
}
