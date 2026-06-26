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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVPrinter;

import com.databasepreservation.common.api.utils.HandlebarsUtils;
import com.databasepreservation.common.client.index.IndexResult;
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
public class ResultsCSVOutputStream extends CSVOutputStream {
  /** The results to write to output stream. */
  private final IndexResult<ViewerRow> results;
  private final TableStatus configTable;
  private final ViewerRow row;
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
  public ResultsCSVOutputStream(final IndexResult<ViewerRow> results, final TableStatus configTable,
    final String filename, final boolean exportDescription, final char delimiter, String fieldsToHeader,
    String databaseUUID) {
    super(filename, delimiter);
    this.results = results;
    this.configTable = configTable;
    this.fieldsToReturn = Stream.of(fieldsToHeader.split(",")).collect(Collectors.toList());
    this.exportDescription = exportDescription;
    this.row = null;
    this.databaseUUID = databaseUUID;
  }

  public ResultsCSVOutputStream(final ViewerRow row, final TableStatus configTable, final String filename,
    final boolean exportDescription, final char delimiter, String fieldsToHeader, String databaseUUID) {
    super(filename, delimiter);
    this.row = row;
    this.results = null;
    this.configTable = configTable;
    this.fieldsToReturn = Stream.of(fieldsToHeader.split(",")).collect(Collectors.toList());
    this.exportDescription = exportDescription;
    this.databaseUUID = databaseUUID;
  }

  @Override
  public void consumeOutputStream(final OutputStream out) throws IOException {
    final OutputStreamWriter writer = new OutputStreamWriter(out);
    CSVPrinter printer = null;

    if (this.results == null) {
      singleRow(writer);
    } else {
      multiRow(writer, printer);
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

  private void singleRow(OutputStreamWriter writer) throws IOException {
    CSVPrinter printer = getFormat()
      .withHeader(configTable.getCSVHeaders(fieldsToReturn, exportDescription).toArray(new String[0])).print(writer);
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

    Map<String, List<ViewerRow>> nestedOriginalRowsForThisRow = new HashMap<>();
    for (Map.Entry<String, List<String>> entry : rowNestedUUIDs.entrySet()) {
      List<String> fieldsToReturn = new ArrayList<>();
      fieldsToReturn.add("tableId");
      fieldsToReturn.addAll(rowNestedFields.get(entry.getKey()));
      try (final IterableIndexResult nestedRows = ViewerFactory.getSolrManager().findAllRows(databaseUUID,
        new Filter(new OneOfManyFilterParameter("uuid", entry.getValue())), new Sorter(), fieldsToReturn)) {
        List<ViewerRow> nestedRowsList = new ArrayList<>();
        for (ViewerRow nestedRow : nestedRows) {
          nestedRowsList.add(nestedRow);
        }
        nestedOriginalRowsForThisRow.put(entry.getKey(), nestedRowsList);
      }
    }
    printer.printRecord(HandlebarsUtils.getCellValues(row, nestedOriginalRowsForThisRow, configTable, fieldsToReturn));
  }

  private void multiRow(OutputStreamWriter writer, CSVPrinter printer) throws IOException {
    boolean isFirst = true;
    for (final ViewerRow row : this.results.getResults()) {
      Map<String, List<String>> rowNestedUUIDs = new HashMap<>();
      Map<String, Set<String>> rowNestedFields = new HashMap<>();

      for (ViewerRow nestedRow : row.getNestedRowList()) {
        if (!rowNestedUUIDs.containsKey(nestedRow.getNestedUUID())) {
          rowNestedUUIDs.put(nestedRow.getNestedUUID(), new ArrayList<>());
          rowNestedFields.put(nestedRow.getNestedUUID(), new HashSet<>());
        }
        rowNestedUUIDs.get(nestedRow.getNestedUUID()).add(nestedRow.getNestedOriginalUUID());
        rowNestedFields.get(nestedRow.getNestedUUID())
          .addAll(nestedRow.getCells().keySet().stream().map(k -> k.substring(4)).toList());
      }

      Map<String, List<ViewerRow>> nestedOriginalRowsForThisRow = new HashMap<>();
      for (Map.Entry<String, List<String>> entry : rowNestedUUIDs.entrySet()) {
        List<String> entryFieldsToReturn = new ArrayList<>();
        entryFieldsToReturn.add("tableId");
        entryFieldsToReturn.addAll(rowNestedFields.get(entry.getKey()));
        try (final IterableIndexResult nestedRows = ViewerFactory.getSolrManager().findAllRows(databaseUUID,
          new Filter(new OneOfManyFilterParameter("uuid", entry.getValue())), new Sorter(), entryFieldsToReturn)) {
          List<ViewerRow> nestedRowsList = new ArrayList<>();
          for (ViewerRow nestedRow : nestedRows) {
            nestedRowsList.add(nestedRow);
          }
          nestedOriginalRowsForThisRow.put(entry.getKey(), nestedRowsList);
        }
      }

      if (isFirst) {
        printer = getFormat()
          .withHeader(configTable.getCSVHeaders(fieldsToReturn, exportDescription).toArray(new String[0]))
          .print(writer);
        isFirst = false;
      }

      printer
        .printRecord(HandlebarsUtils.getCellValues(row, nestedOriginalRowsForThisRow, configTable, fieldsToReturn));
    }
  }
}
