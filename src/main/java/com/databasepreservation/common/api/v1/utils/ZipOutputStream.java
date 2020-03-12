package com.databasepreservation.common.api.v1.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.IOUtils;
import org.roda.core.data.v2.index.sublist.Sublist;

import com.databasepreservation.common.api.utils.ExtraMediaType;
import com.databasepreservation.common.api.utils.HandlebarsUtils;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerCell;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.index.utils.IterableIndexResult;
import com.databasepreservation.common.utils.LobPathManager;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ZipOutputStream extends CSVOutputStream {
  private final String databaseUUID;
  private final TableStatus configTable;
  private final String zipFilename;
  private final String csvFilename;
  private final IterableIndexResult viewerRows;
  private final IterableIndexResult viewerRowsClone;
  private final List<String> fieldsToReturn;
  private Sublist sublist;
  private final boolean exportDescriptions;

  public ZipOutputStream(final String databaseUUID, final TableStatus configTable, final IterableIndexResult viewerRows,
    final IterableIndexResult viewerRowsClone, final String zipFilename, final String csvFilename,
    List<String> fieldsToReturn, Sublist sublist, boolean exportDescriptions, String fieldsToHeader) {
    super(zipFilename, ',');
    this.databaseUUID = databaseUUID;
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

      final List<ColumnStatus> binaryColumns = configTable.getBinaryColumns();
      while (iterator.hasNext() && (nIndex < maxIndex || all)) {
        ViewerRow row = iterator.next();
        if (nIndex < (sublist.getFirstElementIndex())) {
          nIndex++;
          continue;
        } else {
          writeToZipFile(zipArchiveOutputStream, row, binaryColumns);
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

  private ColumnStatus findBinaryColumn(final List<ColumnStatus> columns, final String cell) {
    for (ColumnStatus column : columns) {
      if (column.getId().equals(cell)) {
        return column;
      }
    }
    return null;
  }

  private void writeToZipFile(ZipArchiveOutputStream out, ViewerRow row, List<ColumnStatus> binaryColumns)
    throws IOException {
    for (Map.Entry<String, ViewerCell> cellEntry : row.getCells().entrySet()) {
      final ColumnStatus binaryColumn = findBinaryColumn(binaryColumns, cellEntry.getKey());

      if (binaryColumn != null) {
        out.putArchiveEntry(
          new ZipArchiveEntry(ViewerConstants.INTERNAL_ZIP_LOB_FOLDER + cellEntry.getValue().getValue()));
        final InputStream inputStream = Files
          .newInputStream(LobPathManager.getPath(ViewerFactory.getViewerConfiguration(), databaseUUID,
            configTable.getId(), binaryColumn.getColumnIndex(), row.getUuid()));
        IOUtils.copy(inputStream, out);
        inputStream.close();
        out.closeArchiveEntry();
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
}
