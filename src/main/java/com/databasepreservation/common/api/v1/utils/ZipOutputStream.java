package com.databasepreservation.common.api.v1.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.IOUtils;
import org.roda.core.data.v2.index.sublist.Sublist;

import com.databasepreservation.common.api.utils.ExtraMediaType;
import com.databasepreservation.common.api.utils.HandlebarsUtils;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.configuration.collection.ViewerCollectionConfiguration;
import com.databasepreservation.common.client.models.configuration.collection.ViewerColumnConfiguration;
import com.databasepreservation.common.client.models.configuration.collection.LargeObjectConsolidateProperty;
import com.databasepreservation.common.client.models.configuration.collection.ViewerTableConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerCell;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.index.utils.IterableIndexResult;
import com.databasepreservation.common.utils.LobPathManager;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ZipOutputStream extends CSVOutputStream {
  private final ViewerCollectionConfiguration configurationCollection;
  private final String databaseUUID;
  private final ViewerDatabase database;
  private final ViewerTableConfiguration configTable;
  private final String zipFilename;
  private final String csvFilename;
  private final IterableIndexResult viewerRows;
  private final IterableIndexResult viewerRowsClone;
  private final List<String> fieldsToReturn;
  private Sublist sublist;
  private final boolean exportDescriptions;

  public ZipOutputStream(final ViewerCollectionConfiguration configurationCollection, final String databaseUUID,
                         final ViewerDatabase database, final ViewerTableConfiguration configTable, final IterableIndexResult viewerRows,
                         final IterableIndexResult viewerRowsClone, final String zipFilename, final String csvFilename,
                         List<String> fieldsToReturn, Sublist sublist, boolean exportDescriptions, String fieldsToHeader) {
    super(zipFilename, ',');
    this.configurationCollection = configurationCollection;
    this.databaseUUID = databaseUUID;
    this.database = database;
    this.configTable = configTable;
    this.zipFilename = zipFilename;
    this.csvFilename = csvFilename;
    this.viewerRows = viewerRows;
    this.fieldsToReturn = Stream.of(fieldsToHeader.split(",")).collect(Collectors.toList());
    this.viewerRowsClone = viewerRowsClone;
    this.sublist = sublist;
    this.exportDescriptions = exportDescriptions;
  }

  @Override
  public void consumeOutputStream(OutputStream out) throws IOException {
    ZipFile siardArchive = new ZipFile(database.getPath());

    boolean all = false;
    if (sublist == null) {
      sublist = Sublist.NONE;
      all = true;
    }
    Iterator<ViewerRow> iterator = viewerRows.iterator();
    int nIndex = 0;

    int maxIndex = sublist.getFirstElementIndex() + sublist.getMaximumElementCount();

    try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(out)) {
      zipArchiveOutputStream.setUseZip64(Zip64Mode.AsNeeded);
      zipArchiveOutputStream.setMethod(ZipArchiveOutputStream.DEFLATED);

      final List<ViewerColumnConfiguration> binaryColumns = configTable.getBinaryColumns();
      while (iterator.hasNext() && (nIndex < maxIndex || all)) {
        ViewerRow row = iterator.next();
        if (nIndex < (sublist.getFirstElementIndex())) {
          nIndex++;
          continue;
        } else {
          writeToZipFile(siardArchive, zipArchiveOutputStream, row, binaryColumns);
        }
        nIndex++;
      }

      nIndex = 0;
      final ByteArrayOutputStream byteArrayOutputStream = writeCSVFile(nIndex, all);
      zipArchiveOutputStream.putArchiveEntry(new ZipArchiveEntry(csvFilename));
      zipArchiveOutputStream.write(byteArrayOutputStream.toByteArray());
      byteArrayOutputStream.close();
      zipArchiveOutputStream.closeArchiveEntry();

      zipArchiveOutputStream.finish();
      zipArchiveOutputStream.flush();
    }
  }

  @Override
  public String getFileName() {
    return this.zipFilename;
  }

  @Override
  public String getMediaType() {
    return ExtraMediaType.APPLICATION_ZIP;
  }

  private ViewerColumnConfiguration findBinaryColumn(final List<ViewerColumnConfiguration> columns, final String cell) {
    for (ViewerColumnConfiguration column : columns) {
      if (column.getId().equals(cell)) {
        return column;
      }
    }
    return null;
  }

  private void writeToZipFile(ZipFile siardArchive, ZipArchiveOutputStream out, ViewerRow row,
    List<ViewerColumnConfiguration> binaryColumns) throws IOException {

    for (Map.Entry<String, ViewerCell> cellEntry : row.getCells().entrySet()) {
      final ViewerColumnConfiguration binaryColumn = findBinaryColumn(binaryColumns, cellEntry.getKey());

      if (binaryColumn != null) {
        if (configurationCollection.getConsolidateProperty().equals(LargeObjectConsolidateProperty.CONSOLIDATED)) {
          handleWriteConsolidateLobs(out, binaryColumn, row);
        } else {
          if (configTable.getColumnByIndex(binaryColumn.getColumnIndex()).isExternalLob()) {
            handleWriteExternalLobs(out, binaryColumn, row, cellEntry.getValue());
          } else {
            handleWriteInternalLobs(out, siardArchive, binaryColumn, row, cellEntry.getValue());
          }
        }
      }
    }
  }

  private ByteArrayOutputStream writeCSVFile(int nIndex, boolean all) throws IOException {
    ByteArrayOutputStream listBytes = new ByteArrayOutputStream();
    try (final OutputStreamWriter writer = new OutputStreamWriter(listBytes)) {
      CSVPrinter printer = null;
      boolean isFirst = true;

      int maxIndex = sublist.getFirstElementIndex() + sublist.getMaximumElementCount();

      Iterator<ViewerRow> iterator = viewerRowsClone.iterator();
      while (iterator.hasNext() && (nIndex < maxIndex || all)) {
        ViewerRow row = iterator.next();
        if (nIndex < sublist.getFirstElementIndex()) {
          nIndex++;
          continue;
        } else {
          if (isFirst) {
            printer = new CSVPrinter(writer, getFormat()
              .withHeader(configTable.getCSVHeaders(fieldsToReturn, exportDescriptions).toArray(new String[0])));
            isFirst = false;
          }

          printer.printRecord(HandlebarsUtils.getCellValues(row, configTable, fieldsToReturn));
        }
        nIndex++;
      }
      viewerRowsClone.close();
    }

    return listBytes;
  }

  private void handleWriteConsolidateLobs(ZipArchiveOutputStream out, ViewerColumnConfiguration binaryColumn, ViewerRow row)
    throws IOException {
    final Path consolidatedPath = LobPathManager.getConsolidatedPath(ViewerFactory.getViewerConfiguration(),
      databaseUUID, configTable.getId(), binaryColumn.getColumnIndex(), row.getUuid());

    InputStream in = new FileInputStream(consolidatedPath.toFile());
    final String templateFilename = getTemplateFilename(row, binaryColumn, consolidatedPath.getFileName().toString());
    addEntryToZip(out, in, templateFilename);
  }

  private void handleWriteInternalLobs(ZipArchiveOutputStream out, ZipFile siardArchive, ViewerColumnConfiguration binaryColumn,
    ViewerRow row, ViewerCell cell) throws IOException {
    final String templateFilename = getTemplateFilename(row, binaryColumn, cell.getValue());
    final InputStream in = siardArchive.getInputStream(
      siardArchive.getEntry(LobPathManager.getZipFilePath(configTable, binaryColumn.getColumnIndex(), row)));
    addEntryToZip(out, in, templateFilename);
  }

  private void handleWriteExternalLobs(ZipArchiveOutputStream out, ViewerColumnConfiguration binaryColumn, ViewerRow row,
                                       ViewerCell cell) throws IOException {
    final String lobLocation = cell.getValue();
    final Path lobPath = Paths.get(lobLocation);
    final Path completeLobPath = ViewerFactory.getViewerConfiguration().getSIARDFilesPath().resolve(lobPath);

    final String templateFilename = getTemplateFilename(row, binaryColumn, completeLobPath.getFileName().toString());
    InputStream inputStream = new FileInputStream(lobPath.toFile());
    addEntryToZip(out, inputStream, templateFilename);
  }

  private String getTemplateFilename(ViewerRow row, ViewerColumnConfiguration binaryColumn, String defaultValue) {
    String handlebarsFilename = HandlebarsUtils.applyExportTemplate(row, configTable,
      binaryColumn.getColumnIndex());
    if (ViewerStringUtils.isBlank(handlebarsFilename)) {
      handlebarsFilename = defaultValue;
    }
    return handlebarsFilename;
  }

  private void addEntryToZip(ZipArchiveOutputStream out, InputStream in, String templateFilename) throws IOException {
    out.putArchiveEntry(new ZipArchiveEntry(ViewerConstants.INTERNAL_ZIP_LOB_FOLDER + templateFilename));
    IOUtils.copy(in, out);
    in.close();
    out.closeArchiveEntry();
  }
}
