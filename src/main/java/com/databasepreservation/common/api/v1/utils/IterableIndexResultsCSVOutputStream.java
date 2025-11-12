/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.api.v1.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVPrinter;

import com.databasepreservation.common.api.utils.HandlebarsUtils;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.filter.OneOfManyFilterParameter;
import com.databasepreservation.common.client.index.sort.Sorter;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.index.utils.IterableIndexResult;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class IterableIndexResultsCSVOutputStream extends CSVOutputStream {
  /** The results to write to output stream. */
  private final IterableIndexResult results;
  private final TableStatus configTable;
  private final List<String> fieldsToReturn;
  private final boolean exportDescription;
  private final String databaseUUID;

  /**
   * Constructor.
   *
   * @param results
   *          the results to write to output stream.
   * @param filename
   *          the filename.
   * @param delimiter
   *          the CSV field delimiter.
   */
  public IterableIndexResultsCSVOutputStream(final IterableIndexResult results, final TableStatus configTable,
    final String filename, final boolean exportDescription, final char delimiter, String fieldsToHeader,
    String databaseUUID) {
    super(filename, delimiter);
    this.results = results;
    this.configTable = configTable;
    this.fieldsToReturn = Stream.of(fieldsToHeader.split(",")).collect(Collectors.toList());
    this.exportDescription = exportDescription;
    this.databaseUUID = databaseUUID;
  }

  @Override
  public void consumeOutputStream(final OutputStream out) throws IOException {
    final OutputStreamWriter writer = new OutputStreamWriter(out);
    CSVPrinter printer = null;
    boolean isFirst = true;

    for (ViewerRow row : results) {
      if (isFirst) {
        printer = getFormat()
          .withHeader(configTable.getCSVHeaders(fieldsToReturn, exportDescription).toArray(new String[0]))
          .print(writer);
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

      printer
        .printRecord(HandlebarsUtils.getCellValues(row, nestedOriginalRowsForThisRow, configTable, fieldsToReturn));

    }

    writer.flush();
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
