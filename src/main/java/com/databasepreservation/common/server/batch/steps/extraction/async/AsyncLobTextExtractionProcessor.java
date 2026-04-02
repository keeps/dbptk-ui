package com.databasepreservation.common.server.batch.steps.extraction.async;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerCell;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.exceptions.DataTransformationException;
import com.databasepreservation.common.server.batch.steps.extraction.LobTextExtractionStepUtils;
import com.databasepreservation.common.server.batch.steps.extraction.RowLobTextUpdate;

public class AsyncLobTextExtractionProcessor implements ItemProcessor<ViewerRow, RowLobTextUpdate> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AsyncLobTextExtractionProcessor.class);

  private final String tableId;
  private final TableStatus tableStatus;
  private final JobContext jobContext;

  private final List<ColumnStatus> columnsToExtract;
  private final List<ColumnStatus> columnsToCleanup;

  private final FileSystem siardZipFs;
  private final String dbVersion;
  private final String externalIdPattern;

  private final HttpClient httpClient;
  private final String tikaURL;
  private final String tikaVolumePathConfig;
  private final String externalOcrServiceUrl;
  private final String externalOcrAuthHeader;

  public AsyncLobTextExtractionProcessor(JobContext context, String tableId, FileSystem siardZipFs, String dbVersion) {
    this.tableId = tableId;
    this.siardZipFs = siardZipFs;
    this.dbVersion = dbVersion;
    this.jobContext = context;

    this.tableStatus = context.getCollectionStatus().getTables().stream().filter(t -> t.getId().equals(tableId))
      .findFirst().orElseThrow(() -> new IllegalStateException("Table not found in collection status: " + tableId));

    this.columnsToExtract = tableStatus.getColumns().stream()
      .filter(c -> LobTextExtractionStepUtils.shouldProcess(c) && !LobTextExtractionStepUtils.isMarkedForCleanup(c))
      .collect(Collectors.toList());

    this.columnsToCleanup = tableStatus.getColumns().stream().filter(LobTextExtractionStepUtils::isMarkedForCleanup)
      .collect(Collectors.toList());

    ViewerConfiguration config = ViewerConfiguration.getInstance();
    this.externalIdPattern = config.getViewerConfigurationAsString(null, "ocr.external.service.id.pattern");

    this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).version(HttpClient.Version.HTTP_2)
      .build();
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
  public RowLobTextUpdate process(ViewerRow row) throws Exception {
    RowLobTextUpdate updateItem = new RowLobTextUpdate(row.getUuid());
    updateItem.setTableId(row.getTableId());

    ViewerTable viewerTable = jobContext.getViewerDatabase().getMetadata().getTable(tableStatus.getUuid());
    String schemaName = (viewerTable != null && viewerTable.getSchemaName() != null) ? viewerTable.getSchemaName()
      : ViewerConstants.SIARDDK_DEFAULT_SCHEMA_NAME;
    String tableName = (viewerTable != null) ? viewerTable.getName() : tableStatus.getName();
    String databaseUUID = jobContext.getDatabaseUUID();

    for (ColumnStatus lobColumn : columnsToExtract) {
      ViewerCell lobCell = row.getCells().get(lobColumn.getId());

      if (lobCell != null && lobCell.getValue() != null && !lobCell.getValue().isBlank()
        && !lobCell.getValue().startsWith(ViewerConstants.SIARD_EMBEDDED_LOB_PREFIX)) {

        Path completeLobPath = resolveLobPath(lobColumn, lobCell);
        String fileName = completeLobPath.getFileName().toString();
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
          extension = fileName.substring(dotIndex).toLowerCase();
        }
        if (extension.equals(".tif"))
          extension = ".tiff";

        String extractionId = null;
        if (externalIdPattern != null && !externalIdPattern.isBlank()) {
          extractionId = externalIdPattern.replace("{db}", databaseUUID).replace("{collection}", databaseUUID)
            .replace("{schema}", schemaName).replace("{table}", tableName).replace("{row}", row.getUuid())
            .replace("{col}", String.valueOf(lobColumn.getColumnIndex())).replace("{ext}", extension);
        }

        Set<Path> allLobFilePaths;
        try (Stream<Path> walkStream = Files.walk(completeLobPath, FileVisitOption.FOLLOW_LINKS)) {
          allLobFilePaths = walkStream.filter(file -> !Files.isDirectory(file)).collect(Collectors.toSet());
        }

        for (Path lobFilePath : allLobFilePaths) {
          updateItem.addTarget(lobColumn.getId(),
            new RowLobTextUpdate.LobExtractionTarget(lobFilePath.toString(), extractionId));
        }
      }
    }

    for (ColumnStatus lobColumn : columnsToCleanup) {
      updateItem.markForClear(lobColumn.getId());
    }

    // Perform extraction synchronously within this worker thread
    updateItem.getTargetsToExtract().forEach((columnId, targets) -> {
      StringBuilder extractedTextAggregator = new StringBuilder();

      for (RowLobTextUpdate.LobExtractionTarget target : targets) {
        try {
          String text = performExtraction(target);
          if (text != null && !text.isBlank()) {
            extractedTextAggregator.append(text).append("\n");
          }
        } catch (Exception e) {
          LOGGER.error("Failed to extract text from LOB {} on row {}", target.getPhysicalPath(), updateItem.getUuid(),
            e);
        }
      }

      String finalExtractedText = extractedTextAggregator.toString().trim();
      if (!finalExtractedText.isEmpty()) {
        updateItem.addText(columnId, finalExtractedText);
      }
    });

    return updateItem.hasUpdates() ? updateItem : null;
  }

  private String performExtraction(RowLobTextUpdate.LobExtractionTarget target) throws Exception {
    String physicalPath = target.getPhysicalPath().toLowerCase();
    boolean isTiff = physicalPath.endsWith(".tif") || physicalPath.endsWith(".tiff");

    if (isTiff && externalOcrServiceUrl != null && !externalOcrServiceUrl.isBlank()) {
      return extractTextViaExternalService(target.getExternalRecordId());
    } else {
      return extractTextViaTikaLocal(Paths.get(target.getPhysicalPath()));
    }
  }

  private String extractTextViaExternalService(String externalRecordId) throws Exception {
    if (externalRecordId == null || externalRecordId.isBlank()) {
      throw new DataTransformationException("External Record ID is missing but external OCR service is configured.");
    }
    String encodedId = URLEncoder.encode(externalRecordId, StandardCharsets.UTF_8);
    String requestUrl;
    if (externalOcrServiceUrl.contains("{id}")) {
      requestUrl = externalOcrServiceUrl.replace("{id}", encodedId);
    } else {
      requestUrl = externalOcrServiceUrl.endsWith("/") || externalOcrServiceUrl.endsWith("=")
        ? externalOcrServiceUrl + encodedId
        : externalOcrServiceUrl + "/" + encodedId;
    }

    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(URI.create(requestUrl))
      .timeout(Duration.ofMinutes(10)).GET();
    if (externalOcrAuthHeader != null)
      requestBuilder.header("Authorization", externalOcrAuthHeader);

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

    if (tikaVolumePathConfig != null && !tikaVolumePathConfig.isBlank()
      && lobFilePath.startsWith(tikaVolumePathConfig)) {
      Path tikaVolumePath = Paths.get(tikaVolumePathConfig);
      requestBuilder.header("fetcherName", "fsf").header("fetchKey", tikaVolumePath.relativize(lobFilePath).toString())
        .PUT(HttpRequest.BodyPublishers.noBody());
    } else {
      requestBuilder.PUT(HttpRequest.BodyPublishers.ofInputStream(() -> {
        try {
          return java.nio.file.Files.newInputStream(lobFilePath);
        } catch (java.io.IOException e) {
          throw new java.io.UncheckedIOException("Failed to read LOB file for Tika extraction: " + lobFilePath, e);
        }
      }));
    }

    HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    return response.body();
  }

  private Path resolveLobPath(ColumnStatus lobColumn, ViewerCell lobCell) {
    if (lobColumn.isExternalLob()) {
      Path lobPath = Paths.get(lobCell.getValue());
      return ViewerFactory.getViewerConfiguration().getSIARDFilesPath().resolve(lobPath);
    } else {
      if (LobTextExtractionStepUtils.isDKVersion(this.dbVersion)) {
        return Paths.get(lobCell.getValue());
      } else {
        String siardSchemaFolder = tableStatus.getSchemaFolder();
        String siardTableFolder = tableStatus.getTableFolder();
        String siardLobFolder = ViewerConstants.SIARD_LOB_FOLDER_PREFIX + (lobColumn.getColumnIndex() + 1);
        String zipFileEntry = "/content/" + siardSchemaFolder + "/" + siardTableFolder + "/" + siardLobFolder + "/"
          + lobCell.getValue();

        if (siardZipFs == null) {
          throw new IllegalStateException("SIARD Zip FileSystem is null but required for resolving ZIP entries.");
        }
        return siardZipFs.getPath(zipFileEntry);
      }
    }
  }
}