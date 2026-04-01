package com.databasepreservation.common.server.batch.steps.extraction;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.exceptions.DataTransformationException;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SolrLobTextItemWriter implements ItemWriter<RowLobTextUpdate> {
  private static final Logger LOGGER = LoggerFactory.getLogger(SolrLobTextItemWriter.class);

  private final DatabaseRowsSolrManager solrManager;
  private final JobContext jobContext;
  private final HttpClient httpClient;

  private final String tikaURL;
  private final String tikaVolumePathConfig;

  private final String externalOcrServiceUrl;
  private final String externalOcrAuthHeader;

  public SolrLobTextItemWriter(DatabaseRowsSolrManager solrManager, JobContext jobContext) {
    this.solrManager = solrManager;
    this.jobContext = jobContext;

    this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).version(HttpClient.Version.HTTP_2)
      .build();

    ViewerConfiguration config = ViewerConfiguration.getInstance();

    this.tikaURL = config.getViewerConfigurationAsString(null, ViewerConstants.PROPERTY_OCR_TIKA_URL);
    this.tikaVolumePathConfig = config.getViewerConfigurationAsString(null,
      ViewerConstants.PROPERTY_OCR_TIKA_VOLUME_PATH);

    this.externalOcrServiceUrl = config.getViewerConfigurationAsString(null, "ocr.external.service.url");
    String externalUser = config.getViewerConfigurationAsString(null, "ocr.external.service.user");
    String externalPass = config.getViewerConfigurationAsString(null, "ocr.external.service.password");

    if (externalUser != null && !externalUser.isBlank() && externalPass != null && !externalPass.isBlank()) {
      String auth = externalUser + ":" + externalPass;
      this.externalOcrAuthHeader = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    } else {
      this.externalOcrAuthHeader = null;
    }

    if (this.tikaURL == null || this.tikaURL.isBlank()) {
      throw new IllegalStateException("Tika server URL is not configured. Cannot process local LOBs.");
    }
  }

  @Override
  public void write(Chunk<? extends RowLobTextUpdate> chunk) throws Exception {
    if (chunk == null || chunk.isEmpty())
      return;

    int chunkSize = chunk.getItems().size();
    ForkJoinPool customThreadPool = new ForkJoinPool(chunkSize);

    try {
      customThreadPool.submit(() -> {
        chunk.getItems().parallelStream().forEach(update -> {
          update.getTargetsToExtract().forEach((columnId, targets) -> {
            StringBuilder extractedTextAggregator = new StringBuilder();

            for (RowLobTextUpdate.LobExtractionTarget target : targets) {
              try {
                String physicalPath = target.getPhysicalPath().toLowerCase();
                boolean isTiff = physicalPath.endsWith(".tif") || physicalPath.endsWith(".tiff");
                String text;

                if (isTiff && externalOcrServiceUrl != null && !externalOcrServiceUrl.isBlank()) {
                  text = extractTextViaExternalService(target.getExternalRecordId());
                } else {
                  text = extractTextViaTikaLocal(Paths.get(target.getPhysicalPath()));
                }

                if (text != null && !text.isBlank()) {
                  extractedTextAggregator.append(text).append("\n");
                }
              } catch (Exception e) {
                LOGGER.error("Failed to extract text from LOB {} on row {}", target.getPhysicalPath(), update.getUuid(),
                  e);
              }
            }

            String finalExtractedText = extractedTextAggregator.toString().trim();
            if (!finalExtractedText.isEmpty()) {
              update.addText(columnId, finalExtractedText);
            }
          });
        });
      }).get();
    } finally {
      customThreadPool.shutdown();
    }

    persistToSolr(chunk);
  }

  private String extractTextViaExternalService(String externalRecordId) throws Exception {
    String encodedId = URLEncoder.encode(externalRecordId, StandardCharsets.UTF_8);

    // Replace the {id} placeholder with the actual encoded ID
    String requestUrl = externalOcrServiceUrl.contains("{id}") ? externalOcrServiceUrl.replace("{id}", encodedId)
      : externalOcrServiceUrl + encodedId;

    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(URI.create(requestUrl))
      .timeout(Duration.ofMinutes(10)).GET();

    if (externalOcrAuthHeader != null) {
      requestBuilder.header("Authorization", externalOcrAuthHeader);
    }

    HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() >= 200 && response.statusCode() < 300) {
      return response.body();
    }

    throw new DataTransformationException("External OCR Service failed with status: " + response.statusCode());
  }

  private String extractTextViaTikaLocal(Path lobFilePath) throws Exception {
    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
      .uri(URI.create(tikaURL + ViewerConstants.TIKA_EXTRACT_ENDPOINT)).header("Accept", "text/plain")
      .timeout(Duration.ofMinutes(5));

    try {
      if (tikaVolumePathConfig != null && !tikaVolumePathConfig.isBlank()
        && lobFilePath.startsWith(tikaVolumePathConfig)) {
        Path tikaVolumePath = Paths.get(tikaVolumePathConfig);
        requestBuilder.header("fetcherName", "fsf")
          .header("fetchKey", tikaVolumePath.relativize(lobFilePath).toString())
          .PUT(HttpRequest.BodyPublishers.noBody());
      } else {
        requestBuilder.PUT(HttpRequest.BodyPublishers.ofFile(lobFilePath));
      }

      HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
      return response.body();

    } catch (Exception e) {
      throw new DataTransformationException("Tika local extraction failed for path: " + lobFilePath, e);
    }
  }

  private void persistToSolr(Chunk<? extends RowLobTextUpdate> chunk) throws Exception {
    String databaseUUID = jobContext.getDatabaseUUID();

    for (RowLobTextUpdate update : chunk.getItems()) {
      for (String columnId : update.getColumnsToClear()) {
        solrManager.clearExtractedLobTextField(databaseUUID, update.getUuid(), columnId);

        ColumnStatus columnStatus = jobContext.getCollectionStatus().getColumnByTableIdAndColumn(update.getTableId(),
          columnId);
        columnStatus.getLobTextExtractionStatus().setExtractedAndIndexedText(false);
      }

      for (Map.Entry<String, String> entry : update.getExtractedTexts().entrySet()) {
        String columnId = entry.getKey();
        String text = entry.getValue();

        solrManager.clearExtractedLobTextField(databaseUUID, update.getUuid(), columnId);
        solrManager.addExtractedTextField(databaseUUID, update.getUuid(), columnId, text);

        ColumnStatus columnStatus = jobContext.getCollectionStatus().getColumnByTableIdAndColumn(update.getTableId(),
          columnId);
        columnStatus.getLobTextExtractionStatus().setExtractedAndIndexedText(true);
      }
    }
  }
}
