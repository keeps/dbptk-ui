package com.databasepreservation.common.api.v1.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.csv.CSVPrinter;
import org.roda.core.data.v2.index.sublist.Sublist;

import com.databasepreservation.common.api.utils.HandlebarsUtils;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.server.index.utils.IterableIndexResult;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ZipOutputStreamMultiRow extends ZipOutputStream {
  private final IterableIndexResult viewerRows;
  private final IterableIndexResult viewerRowsClone;
  private Sublist sublist;

  public ZipOutputStreamMultiRow(final CollectionStatus configurationCollection, final ViewerDatabase database,
    final TableStatus configTable, final IterableIndexResult viewerRows, final IterableIndexResult viewerRowsClone,
    final String zipFilename, final String csvFilename, Sublist sublist, boolean exportDescriptions,
    String fieldsToHeader) {
    super(configurationCollection, database, configTable, zipFilename, csvFilename,
      Stream.of(fieldsToHeader.split(",")).collect(Collectors.toList()), exportDescriptions);
    this.viewerRows = viewerRows;
    this.sublist = sublist;
    this.viewerRowsClone = viewerRowsClone;
  }

  @Override
  public void consumeOutputStream(OutputStream out) throws IOException {
    ZipFile siardArchive = new ZipFile(getDatabase().getPath());

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

      final List<ColumnStatus> lobColumns = getConfigTable().getLobColumns();
      while (iterator.hasNext() && (nIndex < maxIndex || all)) {
        ViewerRow row = iterator.next();
        if (nIndex < (sublist.getFirstElementIndex())) {
          nIndex++;
          continue;
        } else {
          writeToZipFile(siardArchive, zipArchiveOutputStream, row, lobColumns);
        }
        nIndex++;
      }

      nIndex = 0;
      final ByteArrayOutputStream byteArrayOutputStream = writeCSVFile(nIndex, all);
      zipArchiveOutputStream.putArchiveEntry(new ZipArchiveEntry(getCsvFilename()));
      zipArchiveOutputStream.write(byteArrayOutputStream.toByteArray());
      byteArrayOutputStream.close();
      zipArchiveOutputStream.closeArchiveEntry();

      zipArchiveOutputStream.finish();
      zipArchiveOutputStream.flush();
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
            printer = new CSVPrinter(writer, getFormat().withHeader(
              getConfigTable().getCSVHeaders(getFieldsToReturn(), isExportDescriptions()).toArray(new String[0])));
            isFirst = false;
          }

          printer.printRecord(HandlebarsUtils.getCellValues(row, getConfigTable(), getFieldsToReturn()));
        }
        nIndex++;
      }
      viewerRowsClone.close();
    }

    return listBytes;
  }
}
