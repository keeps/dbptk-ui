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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.csv.CSVPrinter;

import com.databasepreservation.common.api.utils.HandlebarsUtils;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.filter.OneOfManyFilterParameter;
import com.databasepreservation.common.client.index.sort.Sorter;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.index.utils.IterableIndexResult;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ZipOutputStreamSingleRow extends ZipOutputStream {
  private final ViewerRow row;
  private final String databaseUUID;

  public ZipOutputStreamSingleRow(CollectionStatus configurationCollection, ViewerDatabase database,
    TableStatus configTable, ViewerRow row, String zipFilename, String csvFilename, List<String> fieldsToReturn,
    boolean exportDescriptions, String databaseUUID) {
    super(configurationCollection, database, configTable, zipFilename, csvFilename, fieldsToReturn, exportDescriptions);
    this.row = row;
    this.databaseUUID = databaseUUID;
  }

  @Override
  public void consumeOutputStream(OutputStream out) throws IOException {
    try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(out)) {
      zipArchiveOutputStream.setUseZip64(Zip64Mode.AsNeeded);
      zipArchiveOutputStream.setMethod(ZipArchiveOutputStream.DEFLATED);

      final List<ColumnStatus> binaryColumns = getConfigTable().getLobColumns();
      final Map<ColumnStatus, ColumnStatus> nestedBinaryColumnsMap = getConfigTable()
        .getNestedLobColumns(getConfigurationCollection());

      Map<String, List<String>> rowNestedUUIDs = new HashMap<>();
      Map<String, List<String>> rowNestedFields = new HashMap<>();

      for (ViewerRow nestedRow : row.getNestedRowList()) {
        if (!rowNestedUUIDs.containsKey(nestedRow.getNestedUUID())) {
          rowNestedUUIDs.put(nestedRow.getNestedUUID(), new ArrayList<>());
          rowNestedFields.put(nestedRow.getNestedUUID(),
            nestedRow.getCells().keySet().stream().map(k -> k.substring(4)).toList());
        }
        rowNestedUUIDs.get(nestedRow.getNestedUUID()).add(nestedRow.getNestedOriginalUUID());
      }

      Map<String, IterableIndexResult> nestedOriginalRowsForThisRow = new HashMap<>();
      for (Map.Entry<String, List<String>> entry : rowNestedUUIDs.entrySet()) {
        final IterableIndexResult nestedRows = ViewerFactory.getSolrManager().findAllRows(databaseUUID,
          new Filter(new OneOfManyFilterParameter("uuid", entry.getValue())), new Sorter(),
          rowNestedFields.get(entry.getKey()));
        nestedOriginalRowsForThisRow.put(entry.getKey(), nestedRows);
      }
      if (getDatabase().getVersion().equals(ViewerConstants.SIARD_DK_1007)
        || getDatabase().getVersion().equals(ViewerConstants.SIARD_DK_128)) {
        writeToZipFile(null, zipArchiveOutputStream, row, nestedOriginalRowsForThisRow, binaryColumns,
          nestedBinaryColumnsMap, true);
      } else {
        writeToZipFile(new ZipFile(getDatabase().getPath()), zipArchiveOutputStream, row, nestedOriginalRowsForThisRow,
          binaryColumns, nestedBinaryColumnsMap);
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

      Map<String, List<String>> rowNestedUUIDs = new HashMap<>();
      Map<String, List<String>> rowNestedFields = new HashMap<>();

      for (ViewerRow nestedRow : row.getNestedRowList()) {
        if (!rowNestedUUIDs.containsKey(nestedRow.getNestedUUID())) {
          rowNestedUUIDs.put(nestedRow.getNestedUUID(), new ArrayList<>());
          rowNestedFields.put(nestedRow.getNestedUUID(),
            nestedRow.getCells().keySet().stream().map(k -> k.substring(4)).toList());
        }
        rowNestedUUIDs.get(nestedRow.getNestedUUID()).add(nestedRow.getNestedOriginalUUID());
      }

      Map<String, IterableIndexResult> nestedOriginalRowsForThisRow = new HashMap<>();
      for (Map.Entry<String, List<String>> entry : rowNestedUUIDs.entrySet()) {
        final IterableIndexResult nestedRows = ViewerFactory.getSolrManager().findAllRows(databaseUUID,
          new Filter(new OneOfManyFilterParameter("uuid", entry.getValue())), new Sorter(),
          rowNestedFields.get(entry.getKey()));
        nestedOriginalRowsForThisRow.put(entry.getKey(), nestedRows);
      }

      printer.printRecord(
        HandlebarsUtils.getCellValues(row, nestedOriginalRowsForThisRow, getConfigTable(), getFieldsToReturn()));
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
