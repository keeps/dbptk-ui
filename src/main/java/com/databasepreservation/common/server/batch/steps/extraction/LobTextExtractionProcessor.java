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
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.batch.context.JobContext;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class LobTextExtractionProcessor implements ItemProcessor<ViewerRow, RowLobTextUpdate> {

  private static final Logger LOGGER = LoggerFactory.getLogger(LobTextExtractionProcessor.class);

  private final String tableId;
  private final TableStatus tableStatus;
  private final JobContext jobContext;

  private final List<ColumnStatus> columnsToExtract;
  private final List<ColumnStatus> columnsToCleanup;

  private final FileSystem siardZipFs;
  private final String dbVersion;

  private final String externalIdPattern;

  public LobTextExtractionProcessor(JobContext context, String tableId, FileSystem siardZipFs, String dbVersion) {
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

    // Read the template pattern from configuration (e.g.,
    // "{db}:{collection}:{schema}:{table}:{row}:{col}{ext}")
    this.externalIdPattern = ViewerConfiguration.getInstance().getViewerConfigurationAsString(null,
      "ocr.external.service.id.pattern");
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

        if (extension.equals(".tif")) {
          extension = ".tiff";
        }

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