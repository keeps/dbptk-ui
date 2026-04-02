package com.databasepreservation.common.server.batch.steps.extraction;

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

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerCell;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.exceptions.DataTransformationException;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class AsyncLobTextExtractionProcessor implements ItemProcessor<ViewerRow, RowLobTextUpdate> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AsyncLobTextExtractionProcessor.class);

  private final TableStatus tableStatus;
  private final JobContext jobContext;
  private final List<ColumnStatus> columnsToExtract;
  private final List<ColumnStatus> columnsToCleanup;
  private final FileSystem siardZipFs;
  private final String dbVersion;
  private final LobTextExtractor extractorStrategy;

  public AsyncLobTextExtractionProcessor(JobContext context, String tableId, FileSystem siardZipFs, String dbVersion,
    LobTextExtractor extractorStrategy) {
    this.siardZipFs = siardZipFs;
    this.dbVersion = dbVersion;
    this.jobContext = context;
    this.extractorStrategy = extractorStrategy;

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

    ViewerTable viewerTable = jobContext.getViewerDatabase().getMetadata().getTable(tableStatus.getUuid());
    String schemaName = (viewerTable != null && viewerTable.getSchemaName() != null) ? viewerTable.getSchemaName()
      : ViewerConstants.SIARDDK_DEFAULT_SCHEMA_NAME;
    String tableName = (viewerTable != null) ? viewerTable.getName() : tableStatus.getName();
    String databaseUUID = jobContext.getDatabaseUUID();

    for (ColumnStatus lobColumn : columnsToCleanup) {
      updateItem.markForClear(lobColumn.getId());
    }

    for (ColumnStatus lobColumn : columnsToExtract) {
      ViewerCell lobCell = row.getCells().get(lobColumn.getId());

      if (lobCell != null && lobCell.getValue() != null && !lobCell.getValue().isBlank()
        && !lobCell.getValue().startsWith(ViewerConstants.SIARD_EMBEDDED_LOB_PREFIX)) {

        Path completeLobPath = resolveLobPath(lobColumn, lobCell);

        LobTextExtractor.ExtractionContext ctx = new LobTextExtractor.ExtractionContext(databaseUUID, schemaName,
          tableName, row.getUuid(), lobColumn.getColumnIndex());

        StringBuilder extractedTextAggregator = new StringBuilder();

        try (Stream<Path> walkStream = Files.walk(completeLobPath, FileVisitOption.FOLLOW_LINKS)) {
          Set<Path> allLobFilePaths = walkStream.filter(file -> !Files.isDirectory(file)).collect(Collectors.toSet());

          for (Path lobFilePath : allLobFilePaths) {
            try {
              String text = extractorStrategy.extractText(lobFilePath, ctx);
              if (text != null && !text.isBlank()) {
                extractedTextAggregator.append(text).append("\n");
              }
            } catch (Exception e) {
              LOGGER.error("Failed to extract text from LOB {} on row {}", lobFilePath, updateItem.getUuid(), e);
              throw new DataTransformationException("Text extraction failed for LOB: " + lobFilePath, e);
            }
          }
        }

        String finalExtractedText = extractedTextAggregator.toString().trim();
        if (!finalExtractedText.isEmpty()) {
          updateItem.addText(lobColumn.getId(), finalExtractedText);
        }
      }
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
}
