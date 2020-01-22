package com.databasepreservation.common.api.v1.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import com.databasepreservation.common.client.index.IndexResult;
import org.apache.commons.csv.CSVPrinter;

import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerTable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ResultsCSVOutputStream extends CSVOutputStream {
  /** The results to write to output stream. */
  private final IndexResult<ViewerRow> results;
  private final ViewerTable table;
  private final ViewerRow row;
  private final List<String> fieldsToReturn;
  private final boolean exportDescription;
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
  public ResultsCSVOutputStream(final IndexResult<ViewerRow> results, final ViewerTable table, final List<String> fieldsToReturn, final String filename,
                                final boolean exportDescription, final char delimiter) {
    super(filename, delimiter);
    this.results = results;
    this.table = table;
    this.fieldsToReturn = fieldsToReturn;
    this.exportDescription = exportDescription;
    this.row = null;
  }

  public ResultsCSVOutputStream(final ViewerRow row, final ViewerTable table, final List<String> fieldsToReturn, final String filename,
                                final boolean exportDescription, final char delimiter) {
    super(filename, delimiter);
    this.row = row;
    this.results = null;
    this.table = table;
    this.fieldsToReturn = fieldsToReturn;
    this.exportDescription = exportDescription;
  }

  @Override
  public void consumeOutputStream(final OutputStream out) throws IOException {
    final OutputStreamWriter writer = new OutputStreamWriter(out);
    CSVPrinter printer = null;

    if (this.results == null) {
      singleRow(writer, printer);
    } else {
      multiRow(writer, printer);
    }

    writer.flush();
  }

  private void singleRow(OutputStreamWriter writer, CSVPrinter printer) throws IOException {
    printer = getFormat().withHeader(table.getCSVHeaders(fieldsToReturn, exportDescription).toArray(new String[0])).print(writer);
    printer.printRecord(row.getCellValues(fieldsToReturn));
  }

  private void multiRow(OutputStreamWriter writer, CSVPrinter printer) throws IOException {
    boolean isFirst = true;
    for (final ViewerRow row : this.results.getResults()) {
      if (isFirst) {
        printer = getFormat().withHeader(table.getCSVHeaders(fieldsToReturn, exportDescription).toArray(new String[0])).print(writer);
        isFirst = false;
      }

      printer.printRecord(row.getCellValues(fieldsToReturn));
    }
  }
}
