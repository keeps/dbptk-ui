/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.api.v1.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipFile;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;

import com.databasepreservation.common.api.exceptions.IllegalAccessException;
import com.databasepreservation.common.api.utils.ExtraMediaType;
import com.databasepreservation.common.api.utils.HandlebarsUtils;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.LargeObjectConsolidateProperty;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerCell;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerLobStoreType;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerType;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.index.utils.IterableIndexResult;
import com.databasepreservation.common.utils.FilenameUtils;
import com.databasepreservation.common.utils.LobManagerUtils;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public abstract class ZipOutputStream extends CSVOutputStream {

  private final CollectionStatus configurationCollection;
  private final ViewerDatabase database;
  private final TableStatus configTable;
  private final String zipFilename;
  private final String csvFilename;
  private final List<String> fieldsToReturn;
  private final boolean exportDescriptions;

  public ZipOutputStream(CollectionStatus configurationCollection, ViewerDatabase database, TableStatus configTable,
    String zipFilename, String csvFilename, List<String> fieldsToReturn, boolean exportDescriptions) {
    super(zipFilename, ',');
    this.configurationCollection = configurationCollection;
    this.database = database;
    this.configTable = configTable;
    this.zipFilename = zipFilename;
    this.csvFilename = csvFilename;
    this.fieldsToReturn = fieldsToReturn;
    this.exportDescriptions = exportDescriptions;
  }

  public CollectionStatus getConfigurationCollection() {
    return configurationCollection;
  }

  public ViewerDatabase getDatabase() {
    return database;
  }

  public TableStatus getConfigTable() {
    return configTable;
  }

  public String getZipFilename() {
    return zipFilename;
  }

  public String getCsvFilename() {
    return csvFilename;
  }

  public List<String> getFieldsToReturn() {
    return fieldsToReturn;
  }

  public boolean isExportDescriptions() {
    return exportDescriptions;
  }

  @Override
  public String getFileName() {
    return getZipFilename();
  }

  @Override
  public String getMediaType() {
    return ExtraMediaType.APPLICATION_ZIP;
  }

  @Override
  public abstract void consumeOutputStream(OutputStream out) throws IOException;

  protected ColumnStatus findLobColumn(final List<ColumnStatus> columns, final String cell) {
    for (ColumnStatus column : columns) {
      if (column.getId().equals(cell)) {
        return column;
      }
    }
    return null;
  }

  protected void writeToZipFile(ZipFile siardArchive, ZipArchiveOutputStream out, ViewerRow row,
    Map<String, IterableIndexResult> nestedRows, List<ColumnStatus> binaryColumns,
    Map<ColumnStatus, ColumnStatus> nestedBinaryColumnsMap) throws IOException, IllegalAccessException {
    writeToZipFile(siardArchive, out, row, nestedRows, binaryColumns, nestedBinaryColumnsMap, false);
  }

  protected void writeToZipFile(ZipFile siardArchive, ZipArchiveOutputStream out, ViewerRow row,
    Map<String, IterableIndexResult> nestedRows, List<ColumnStatus> binaryColumns,
    Map<ColumnStatus, ColumnStatus> nestedBinaryColumnsMap, boolean isSiardDK)
    throws IOException, IllegalAccessException {

    Set<Map.Entry<String, ViewerCell>> cellsEntrySet = new HashSet<>(row.getCells().entrySet());

    for (Map.Entry<String, ViewerCell> cellEntry : cellsEntrySet) {
      final ColumnStatus binaryColumn = findLobColumn(binaryColumns, cellEntry.getKey());

      if (binaryColumn != null) {
        if (isSiardDK) {
          handleWriteSIARDDKLobs(out, cellEntry.getValue(), binaryColumn, row);
        } else {
          if (ViewerType.dbTypes.CLOB.equals(binaryColumn.getType())) {
            handleWriteClob(out, binaryColumn, row);
          } else if (configurationCollection.getConsolidateProperty()
            .equals(LargeObjectConsolidateProperty.CONSOLIDATED)) {
            handleWriteConsolidateLobs(out, binaryColumn, row);
          } else {
            if (ViewerLobStoreType.EXTERNALLY.equals(
              row.getCells().get(configTable.getColumnByIndex(binaryColumn.getColumnIndex()).getId()).getStoreType())) {
              handleWriteExternalLobs(out, binaryColumn, row, cellEntry.getValue());
            } else {
              handleWriteInternalLobs(out, siardArchive, binaryColumn, row);
            }
          }
        }
      }
    }

    Map<ViewerRow, Map<String, ViewerCell>> nestedCellsEntrySet = new HashMap<>();
    for (ViewerRow nestedRow : row.getNestedRowList()) {
      if (!nestedCellsEntrySet.containsKey(nestedRow.getNestedUUID())) {
        HashMap<String, ViewerCell> nestedCells = new HashMap<>(nestedRow.getCells());
        nestedCellsEntrySet.put(nestedRow, nestedCells);
      }
    }

    for (Map.Entry<ViewerRow, Map<String, ViewerCell>> nestedRowViewerCells : nestedCellsEntrySet.entrySet()) {
      for (Map.Entry<String, ViewerCell> nestedViewerCell : nestedRowViewerCells.getValue().entrySet()) {
        Map.Entry<ColumnStatus, ColumnStatus> columnMapping = null;
        for (Map.Entry<ColumnStatus, ColumnStatus> columnMappingEntry : nestedBinaryColumnsMap.entrySet()) {
          if (nestedRowViewerCells.getKey().getTableId()
            .equals(columnMappingEntry.getKey().getNestedColumns().getOriginalTable())
            && nestedViewerCell.getKey().equals("nst_" + columnMappingEntry.getValue().getId())) {
            columnMapping = columnMappingEntry;
            break;
          }
        }
        if (columnMapping != null) {
          for (ViewerRow originalRow : nestedRows.get(columnMapping.getKey().getId())) {
            ViewerCell originalCell = originalRow.getCells().get(columnMapping.getValue().getId());
            if (originalCell != null) {
              if (isSiardDK) {
                handleWriteSIARDDKLobs(out, originalCell, columnMapping.getKey(), row);
              } else {
                if (ViewerType.dbTypes.CLOB.equals(columnMapping.getValue().getType())) {
                  handleWriteNestedClob(out, columnMapping.getValue(), originalRow, columnMapping.getKey());
                } else if (configurationCollection.getConsolidateProperty()
                  .equals(LargeObjectConsolidateProperty.CONSOLIDATED)) {
                  handleWriteNestedConsolidateLobs(out, columnMapping.getValue(), originalRow);
                } else {
                  if (ViewerLobStoreType.EXTERNALLY.equals(
                    originalRow.getCells().get(getConfigurationCollection().getTableStatusByTableId(originalRow.getTableId())
                      .getColumnByIndex(columnMapping.getValue().getColumnIndex()).getId()).getStoreType())) {
                    handleWriteNestedExternalLobs(out, columnMapping.getValue(), originalRow, originalCell);
                  } else {
                    handleWriteNestedInternalLobs(out, siardArchive, columnMapping.getValue(), originalRow);
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  private void handleWriteConsolidateLobs(ZipArchiveOutputStream out, ColumnStatus binaryColumn, ViewerRow row)
    throws IOException, IllegalAccessException {
    final Path consolidatedPath = LobManagerUtils.getConsolidatedPath(ViewerFactory.getViewerConfiguration(),
      database.getUuid(), configTable.getId(), binaryColumn.getColumnIndex(), row.getUuid());

    InputStream in = new FileInputStream(consolidatedPath.toFile());
    final String templateFilename = FilenameUtils.getTemplateFilename(row, configTable, binaryColumn,
      consolidatedPath.getFileName().toString());
    addEntryToZip(out, in, templateFilename);
  }

  private void handleWriteNestedConsolidateLobs(ZipArchiveOutputStream out, ColumnStatus originalBinaryColumn,
    ViewerRow originalRow) throws IOException, IllegalAccessException {
    final Path consolidatedPath = LobManagerUtils.getConsolidatedPath(ViewerFactory.getViewerConfiguration(),
      database.getUuid(), getConfigurationCollection().getTableStatusByTableId(originalRow.getTableId()).getId(),
      originalBinaryColumn.getColumnIndex(), originalRow.getUuid());

    InputStream in = new FileInputStream(consolidatedPath.toFile());
    final String templateFilename = FilenameUtils.getTemplateFilename(originalRow,
      getConfigurationCollection().getTableStatusByTableId(originalRow.getTableId()), originalBinaryColumn,
      consolidatedPath.getFileName().toString());
    addEntryToZip(out, in, templateFilename);
  }

  private void handleWriteInternalLobs(ZipArchiveOutputStream out, ZipFile siardArchive, ColumnStatus binaryColumn,
    ViewerRow row) throws IOException {
    final String templateFilename = FilenameUtils.getTemplateFilename(row, configTable, binaryColumn);

    if (LobManagerUtils.isLobEmbedded(configTable, row, binaryColumn.getColumnIndex())) {
      String lobCellValue = LobManagerUtils.getLobCellValue(configTable, row, binaryColumn.getColumnIndex());
      lobCellValue = lobCellValue.replace(ViewerConstants.SIARD_EMBEDDED_LOB_PREFIX, "");
      String decodedString = new String(Base64.decodeBase64(lobCellValue.getBytes()));

      addEntryToZip(out, new BufferedInputStream(new ByteArrayInputStream(decodedString.getBytes())), templateFilename);
    } else {
      final InputStream in = siardArchive.getInputStream(
        siardArchive.getEntry(LobManagerUtils.getZipFilePath(configTable, binaryColumn.getColumnIndex(), row)));
      addEntryToZip(out, in, templateFilename);
    }
  }

  private void handleWriteNestedInternalLobs(ZipArchiveOutputStream out, ZipFile siardArchive,
    ColumnStatus originalBinaryColumn, ViewerRow originalRow) throws IOException {
    TableStatus originalConfigTable = getConfigurationCollection().getTableStatusByTableId(originalRow.getTableId());

    final String templateFilename = FilenameUtils.getTemplateFilename(originalRow, originalConfigTable,
      originalBinaryColumn);

    if (LobManagerUtils.isLobEmbedded(originalConfigTable, originalRow, originalBinaryColumn.getColumnIndex())) {
      String lobCellValue = LobManagerUtils.getLobCellValue(originalConfigTable, originalRow,
        originalBinaryColumn.getColumnIndex());
      lobCellValue = lobCellValue.replace(ViewerConstants.SIARD_EMBEDDED_LOB_PREFIX, "");
      String decodedString = new String(Base64.decodeBase64(lobCellValue.getBytes()));

      addEntryToZip(out, new BufferedInputStream(new ByteArrayInputStream(decodedString.getBytes())), templateFilename);
    } else {
      final InputStream in = siardArchive.getInputStream(siardArchive.getEntry(
        LobManagerUtils.getZipFilePath(originalConfigTable, originalBinaryColumn.getColumnIndex(), originalRow)));
      addEntryToZip(out, in, templateFilename);
    }
  }

  private void handleWriteExternalLobs(ZipArchiveOutputStream out, ColumnStatus binaryColumn, ViewerRow row,
    ViewerCell cell) throws IOException {
    final String lobLocation = cell.getValue();
    final Path lobPath = Paths.get(lobLocation);
    final Path completeLobPath = ViewerFactory.getViewerConfiguration().getSIARDFilesPath().resolve(lobPath);

    final String templateFilename = FilenameUtils.getTemplateFilename(row, configTable, binaryColumn,
      completeLobPath.getFileName().toString());
    InputStream inputStream = Files.newInputStream(completeLobPath);
    addEntryToZip(out, inputStream, templateFilename);
  }

  private void handleWriteNestedExternalLobs(ZipArchiveOutputStream out, ColumnStatus originalBinaryColumn,
    ViewerRow originalRow, ViewerCell originalCell) throws IOException {
    final String lobLocation = originalCell.getValue();
    final Path lobPath = Paths.get(lobLocation);
    final Path completeLobPath = ViewerFactory.getViewerConfiguration().getSIARDFilesPath().resolve(lobPath);

    final String templateFilename = FilenameUtils.getTemplateFilename(originalRow,
      getConfigurationCollection().getTableStatusByTableId(originalRow.getTableId()), originalBinaryColumn,
      completeLobPath.getFileName().toString());
    InputStream inputStream = Files.newInputStream(completeLobPath);
    addEntryToZip(out, inputStream, templateFilename);
  }

  private void handleWriteSIARDDKLobs(ZipArchiveOutputStream out, ViewerCell cell, ColumnStatus binaryColumn,
    ViewerRow row) throws IOException {
    final String lobLocation = cell.getValue();
    final Path lobPath = Paths.get(lobLocation);

    if (lobPath.toFile().isDirectory()) {
      int index = 0;
      for (File file : Objects.requireNonNull(lobPath.toFile().listFiles())) {
        InputStream inputStream = Files.newInputStream(file.toPath());
        String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1);
        String templateFilename = row.getUuid() + "_" + index + "_"
          + FilenameUtils.getTemplateFilename(row, configTable, binaryColumn, lobPath.getFileName().toString());
        if (cell.getFileExtension() != null && !cell.getFileExtension().isBlank()) {
          templateFilename = templateFilename.replace(ExtraMediaType.ZIP_FILE_EXTENSION, "." + extension);
        } else {
          templateFilename = templateFilename + "." + extension;
        }
        addEntryToZip(out, inputStream, templateFilename);
        index++;
      }
    } else {
      InputStream inputStream = Files.newInputStream(lobPath);
      final String templateFilename = row.getUuid() + "_0_"
        + FilenameUtils.getTemplateFilename(row, configTable, binaryColumn,
        lobPath.getFileName().toString());
      addEntryToZip(out, inputStream, templateFilename);
    }
  }

  private void handleWriteClob(ZipArchiveOutputStream out, ColumnStatus binaryColumn, ViewerRow row)
    throws IOException {

    String handlebarsFilename = HandlebarsUtils.applyExportTemplate(row, configTable, binaryColumn.getColumnIndex());

    if (ViewerStringUtils.isBlank(handlebarsFilename)) {
      handlebarsFilename = "file_" + binaryColumn.getCustomName();
    }

    ByteArrayInputStream in = new ByteArrayInputStream(row.getCells().get(binaryColumn.getId()).getValue().getBytes());

    addEntryToZip(out, in, handlebarsFilename);
  }

  private void handleWriteNestedClob(ZipArchiveOutputStream out, ColumnStatus originalBinaryColumn,
    ViewerRow originalRow, ColumnStatus transformBinaryColumn) throws IOException {

    String handlebarsFilename = HandlebarsUtils.applyExportTemplate(originalRow,
      configurationCollection.getTableStatusByTableId(originalRow.getTableId()), originalBinaryColumn.getColumnIndex());

    if (ViewerStringUtils.isBlank(handlebarsFilename)) {
      handlebarsFilename = "file_" + transformBinaryColumn.getCustomName();
    }

    ByteArrayInputStream in = new ByteArrayInputStream(
      originalRow.getCells().get(originalBinaryColumn.getId()).getValue().getBytes());

    addEntryToZip(out, in, handlebarsFilename);
  }

  private void addEntryToZip(ZipArchiveOutputStream out, InputStream in, String templateFilename) throws IOException {
    out.putArchiveEntry(new ZipArchiveEntry(ViewerConstants.INTERNAL_ZIP_LOB_FOLDER + templateFilename));
    IOUtils.copy(in, out);
    in.close();
    out.closeArchiveEntry();
  }
}
