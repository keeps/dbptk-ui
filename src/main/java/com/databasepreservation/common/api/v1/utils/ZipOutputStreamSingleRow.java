package com.databasepreservation.common.api.v1.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.IOUtils;

import com.databasepreservation.common.api.utils.ExtraMediaType;
import com.databasepreservation.common.api.utils.HandlebarsUtils;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerCell;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.utils.LobPathManager;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ZipOutputStreamSingleRow extends CSVOutputStream {
  private final String databaseUUID;
  private final ViewerDatabase database;
  private final TableStatus configTable;
  private final String zipFilename;
  private final String csvFilename;
  private final ViewerRow row;
  private final List<String> fieldsToReturn;
  private final boolean exportDescriptions;

  public ZipOutputStreamSingleRow(ViewerDatabase database, TableStatus configTable, ViewerRow row, String zipFilename,
    String csvFilename, List<String> fieldsToReturn, boolean exportDescriptions) {
    super(zipFilename, ',');
    this.databaseUUID = database.getUuid();
    this.database = database;
    this.configTable = configTable;
    this.row = row;
    this.zipFilename = zipFilename;
    this.csvFilename = csvFilename;
    this.fieldsToReturn = fieldsToReturn;
    this.exportDescriptions = exportDescriptions;
  }

  @Override
  public void consumeOutputStream(OutputStream out) throws IOException {
    try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(out)) {
      zipArchiveOutputStream.setUseZip64(Zip64Mode.AsNeeded);
      zipArchiveOutputStream.setMethod(ZipArchiveOutputStream.DEFLATED);

      final List<ColumnStatus> binaryColumns = configTable.getBinaryColumns();
      writeToZipFile(new ZipFile(database.getPath()), zipArchiveOutputStream, row, binaryColumns);

      final ByteArrayOutputStream byteArrayOutputStream = writeCSVFile();
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

  private ColumnStatus findBinaryColumn(final List<ColumnStatus> columns, final String cell) {
    for (ColumnStatus column : columns) {
      if (column.getId().equals(cell)) {
        return column;
      }
    }
    return null;
  }

  private void writeToZipFile(ZipFile siardArchive, ZipArchiveOutputStream out, ViewerRow row, List<ColumnStatus> binaryColumns)
    throws IOException {
    for (Map.Entry<String, ViewerCell> cellEntry : row.getCells().entrySet()) {
      final ColumnStatus binaryColumn = findBinaryColumn(binaryColumns, cellEntry.getKey());

      if (binaryColumn != null) {
        final InputStream siardArchiveInputStream = siardArchive.getInputStream(siardArchive.getEntry(
            LobPathManager.getZipFilePath(database, configTable.getUuid(), binaryColumn.getColumnIndex(), row.getUuid())));

        out.putArchiveEntry(
          new ZipArchiveEntry(ViewerConstants.INTERNAL_ZIP_LOB_FOLDER + cellEntry.getValue().getValue()));
        IOUtils.copy(siardArchiveInputStream, out);
        siardArchiveInputStream.close();
        out.closeArchiveEntry();
      }
    }
  }

  private ByteArrayOutputStream writeCSVFile() throws IOException {
    ByteArrayOutputStream listBytes = new ByteArrayOutputStream();
    try (final OutputStreamWriter writer = new OutputStreamWriter(listBytes)) {
      CSVPrinter printer = new CSVPrinter(writer,
        getFormat().withHeader(configTable.getCSVHeaders(fieldsToReturn, exportDescriptions).toArray(new String[0])));
      printer.printRecord(HandlebarsUtils.getCellValues(row, configTable, fieldsToReturn));
    }
    return listBytes;
  }
}
