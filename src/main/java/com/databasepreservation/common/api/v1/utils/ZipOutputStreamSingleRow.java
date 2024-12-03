/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.api.v1.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipFile;

import com.databasepreservation.common.client.ViewerConstants;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.csv.CSVPrinter;

import com.databasepreservation.common.api.utils.HandlebarsUtils;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerRow;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ZipOutputStreamSingleRow extends ZipOutputStream {
  private final ViewerRow row;

  public ZipOutputStreamSingleRow(CollectionStatus configurationCollection, ViewerDatabase database,
    TableStatus configTable, ViewerRow row, String zipFilename, String csvFilename, List<String> fieldsToReturn,
    boolean exportDescriptions) {
    super(configurationCollection, database, configTable, zipFilename, csvFilename, fieldsToReturn, exportDescriptions);
    this.row = row;
  }

  @Override
  public void consumeOutputStream(OutputStream out) throws IOException {
    try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(out)) {
      zipArchiveOutputStream.setUseZip64(Zip64Mode.AsNeeded);
      zipArchiveOutputStream.setMethod(ZipArchiveOutputStream.DEFLATED);

      final List<ColumnStatus> binaryColumns = getConfigTable().getLobColumns();

      if (getDatabase().getVersion().equals(ViewerConstants.SIARD_DK_1007)
        || getDatabase().getVersion().equals(ViewerConstants.SIARD_DK_128)) {
        writeToZipFile(null, zipArchiveOutputStream, row, binaryColumns, true);
      } else {
        writeToZipFile(new ZipFile(getDatabase().getPath()), zipArchiveOutputStream, row, binaryColumns);
      }

      final ByteArrayOutputStream byteArrayOutputStream = writeCSVFile();
      zipArchiveOutputStream.putArchiveEntry(new ZipArchiveEntry(getCsvFilename()));
      zipArchiveOutputStream.write(byteArrayOutputStream.toByteArray());
      byteArrayOutputStream.close();
      zipArchiveOutputStream.closeArchiveEntry();

      zipArchiveOutputStream.finish();
      zipArchiveOutputStream.flush();
    }
  }

  private ByteArrayOutputStream writeCSVFile() throws IOException {
    ByteArrayOutputStream listBytes = new ByteArrayOutputStream();
    try (final OutputStreamWriter writer = new OutputStreamWriter(listBytes)) {
      CSVPrinter printer = new CSVPrinter(writer, getFormat().withHeader(
        getConfigTable().getCSVHeaders(getFieldsToReturn(), isExportDescriptions()).toArray(new String[0])));
      printer.printRecord(HandlebarsUtils.getCellValues(row, getConfigTable(), getFieldsToReturn()));
    }
    return listBytes;
  }

  @Override
  public Date getLastModified() {
    return null;
  }

  @Override
  public long getSize() {
    return -1;
  }
}
