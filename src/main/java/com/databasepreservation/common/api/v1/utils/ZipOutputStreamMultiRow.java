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
import org.roda.core.data.v2.index.sublist.Sublist;

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
public class ZipOutputStreamMultiRow extends ZipOutputStream {
  private final IterableIndexResult viewerRows;
  private final IterableIndexResult viewerRowsClone;
  private Sublist sublist;
  private final String databaseUUID;

  public ZipOutputStreamMultiRow(final CollectionStatus configurationCollection, final ViewerDatabase database,
    final TableStatus configTable, final IterableIndexResult viewerRows, final IterableIndexResult viewerRowsClone,
    final String zipFilename, final String csvFilename, Sublist sublist, boolean exportDescriptions,
    String fieldsToHeader, String databaseUUID) {
    super(configurationCollection, database, configTable, zipFilename, csvFilename,
      Stream.of(fieldsToHeader.split(",")).collect(Collectors.toList()), exportDescriptions);
    this.viewerRows = viewerRows;
    this.sublist = sublist;
    this.viewerRowsClone = viewerRowsClone;
    this.databaseUUID = databaseUUID;
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

      final List<ColumnStatus> lobColumns = getConfigTable().getLobColumns();
      final Map<ColumnStatus, ColumnStatus> nestedLobColumnsMap = getConfigTable()
        .getNestedLobColumns(getConfigurationCollection());
      while (iterator.hasNext() && (nIndex < maxIndex || all)) {
        ViewerRow row = iterator.next();
        if (nIndex < (sublist.getFirstElementIndex())) {
          nIndex++;
          continue;
        } else {
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
            List<String> fieldsToReturn = new ArrayList<>();
            fieldsToReturn.add("tableId");
            fieldsToReturn.addAll(rowNestedFields.get(entry.getKey()));
            final IterableIndexResult nestedRows = ViewerFactory.getSolrManager().findAllRows(databaseUUID,
              new Filter(new OneOfManyFilterParameter("uuid", entry.getValue())), new Sorter(), fieldsToReturn);
            nestedOriginalRowsForThisRow.put(entry.getKey(), nestedRows);
          }
          if (getDatabase().getVersion().equals(ViewerConstants.SIARD_DK_1007)
            || getDatabase().getVersion().equals(ViewerConstants.SIARD_DK_128)) {
            writeToZipFile(null, zipArchiveOutputStream, row, nestedOriginalRowsForThisRow, lobColumns,
              nestedLobColumnsMap, true);
          } else {
            ZipFile siardArchive = new ZipFile(getDatabase().getPath());
            writeToZipFile(siardArchive, zipArchiveOutputStream, row, nestedOriginalRowsForThisRow, lobColumns,
              nestedLobColumnsMap);
          }
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
            List<String> fieldsToReturn = new ArrayList<>();
            fieldsToReturn.add("tableId");
            fieldsToReturn.addAll(rowNestedFields.get(entry.getKey()));
            final IterableIndexResult nestedRows = ViewerFactory.getSolrManager().findAllRows(databaseUUID,
              new Filter(new OneOfManyFilterParameter("uuid", entry.getValue())), new Sorter(), fieldsToReturn);
            nestedOriginalRowsForThisRow.put(entry.getKey(), nestedRows);
          }
          printer.printRecord(
            HandlebarsUtils.getCellValues(row, nestedOriginalRowsForThisRow, getConfigTable(), getFieldsToReturn()));
        }
        nIndex++;
      }
      viewerRowsClone.close();
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
