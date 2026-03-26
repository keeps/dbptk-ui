package com.databasepreservation.common.server.batch.steps.extraction;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerCell;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.exceptions.DataTransformationException;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class LobTextExtractionProcessor implements ItemProcessor<ViewerRow, RowLobTextUpdate> {

  private static final Logger LOGGER = LoggerFactory.getLogger(LobTextExtractionProcessor.class);

  private final String tableId;
  private final TableStatus tableStatus;

  private final List<ColumnStatus> columnsToExtract;
  private final List<ColumnStatus> columnsToCleanup;

  private final FileSystem siardZipFs;
  private final String dbVersion;

  private final RestTemplate tikaTemplate;
  private final String tikaURL;
  private final String tikaVolumePathConfig;

  public LobTextExtractionProcessor(JobContext context, String tableId, FileSystem siardZipFs, String dbVersion) {
    this.tableId = tableId;
    this.siardZipFs = siardZipFs;
    this.dbVersion = dbVersion;

    this.tikaTemplate = new RestTemplate();
    this.tikaTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
    this.tikaURL = ViewerConfiguration.getInstance().getViewerConfigurationAsString(null,
      ViewerConstants.PROPERTY_OCR_TIKA_URL);
    this.tikaVolumePathConfig = ViewerConfiguration.getInstance().getViewerConfigurationAsString(null,
      ViewerConstants.PROPERTY_OCR_TIKA_VOLUME_PATH);

    if (this.tikaURL == null || this.tikaURL.isBlank()) {
      throw new IllegalStateException("Tika server URL is not configured. Cannot process LOBs.");
    }

    this.tableStatus = context.getCollectionStatus().getTables().stream().filter(t -> t.getId().equals(tableId))
      .findFirst().orElseThrow(() -> new IllegalStateException("Table not found in collection status: " + tableId));

    this.columnsToExtract = tableStatus.getColumns().stream()
      .filter(c -> LobTextExtractionStepUtils.shouldProcess(c) && !LobTextExtractionStepUtils.isMarkedForCleanup(c))
      .collect(Collectors.toList());

    this.columnsToCleanup = tableStatus.getColumns().stream().filter(LobTextExtractionStepUtils::isMarkedForCleanup)
      .collect(Collectors.toList());
  }

  @Override
  public RowLobTextUpdate process(ViewerRow row) throws Exception {
    RowLobTextUpdate updateItem = new RowLobTextUpdate(row.getUuid());

    updateItem.setTableId(row.getTableId());

    for (ColumnStatus lobColumn : columnsToExtract) {
      ViewerCell lobCell = row.getCells().get(lobColumn.getId());

      if (lobCell != null && lobCell.getValue() != null && !lobCell.getValue().isBlank()
        && !lobCell.getValue().startsWith(ViewerConstants.SIARD_EMBEDDED_LOB_PREFIX)) {

        Path completeLobPath = resolveLobPath(lobColumn, lobCell);

        Set<Path> allLobFilePaths;
        try (Stream<Path> walkStream = Files.walk(completeLobPath, FileVisitOption.FOLLOW_LINKS)) {
          allLobFilePaths = walkStream.filter(file -> !Files.isDirectory(file)).collect(Collectors.toSet());
        }

        StringBuilder extractedTextAggregator = new StringBuilder();

        for (Path lobFilePath : allLobFilePaths) {
          String text = extractTextViaTika(lobFilePath);
          if (text != null && !text.isBlank()) {
            extractedTextAggregator.append(text).append("\n");
          }
        }

        String finalExtractedText = extractedTextAggregator.toString().trim();
        if (!finalExtractedText.isEmpty()) {
          updateItem.addText(lobColumn.getId(), finalExtractedText);
        }
      }
    }

    for (ColumnStatus lobColumn : columnsToCleanup) {
      updateItem.markForClear(lobColumn.getId());
    }

    return updateItem.hasUpdates() ? updateItem : null;
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

  private String extractTextViaTika(Path lobFilePath) throws DataTransformationException {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(List.of(MediaType.TEXT_PLAIN));
    ResponseEntity<String> tikaResponse;

    try {
      if (tikaVolumePathConfig == null || tikaVolumePathConfig.isBlank()
        || !lobFilePath.startsWith(tikaVolumePathConfig)) {
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add(ViewerConstants.TIKA_REQUEST_FILE_PARAMETER, new FileSystemResource(lobFilePath));
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        tikaResponse = tikaTemplate.exchange(tikaURL + ViewerConstants.TIKA_FORM_ENDPOINT, HttpMethod.POST, entity,
          String.class);
      } else {
        Path tikaVolumePath = Paths.get(tikaVolumePathConfig);
        headers.add("fetcherName", "fsf");
        headers.add("fetchKey", tikaVolumePath.relativize(lobFilePath).toString());
        HttpEntity<Object> entity = new HttpEntity<>(null, headers);

        tikaResponse = tikaTemplate.exchange(tikaURL + ViewerConstants.TIKA_EXTRACT_ENDPOINT, HttpMethod.PUT, entity,
          String.class);
      }
      return tikaResponse.getBody();

    } catch (Exception e) {
      LOGGER.error("Failed to extract text from LOB {} using Tika at {}", lobFilePath, tikaURL, e);
      throw new DataTransformationException("Tika extraction failed for path: " + lobFilePath, e);
    }
  }
}
