package com.databasepreservation.common.api.v1.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.databasepreservation.common.api.utils.HandlebarsUtils;
import org.apache.commons.csv.CSVPrinter;

import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.models.configuration.collection.ViewerTableConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerRow;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ResultsCSVOutputStream extends CSVOutputStream {
  /** The results to write to output stream. */
  private final IndexResult<ViewerRow> results;
  private final ViewerTableConfiguration configTable;
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
  public ResultsCSVOutputStream(final IndexResult<ViewerRow> results, final ViewerTableConfiguration configTable,
    final String filename, final boolean exportDescription, final char delimiter, String fieldsToHeader) {
    super(filename, delimiter);
    this.results = results;
    this.configTable = configTable;
    this.fieldsToReturn = Stream.of(fieldsToHeader.split(",")).collect(Collectors.toList());
    ;
    this.exportDescription = exportDescription;
    this.row = null;
  }

  public ResultsCSVOutputStream(final ViewerRow row, final ViewerTableConfiguration configTable, final String filename,
                                final boolean exportDescription, final char delimiter, String fieldsToHeader) {
    super(filename, delimiter);
    this.row = row;
    this.results = null;
    this.configTable = configTable;
    this.fieldsToReturn = Stream.of(fieldsToHeader.split(",")).collect(Collectors.toList());
    this.exportDescription = exportDescription;
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

  private void singleRow(OutputStreamWriter writer) throws IOException {
    CSVPrinter printer = getFormat()
      .withHeader(configTable.getCSVHeaders(fieldsToReturn, exportDescription).toArray(new String[0])).print(writer);
    printer.printRecord(HandlebarsUtils.getCellValues(row, configTable, fieldsToReturn));
  }

  private void multiRow(OutputStreamWriter writer, CSVPrinter printer) throws IOException {
    boolean isFirst = true;
    for (final ViewerRow row : this.results.getResults()) {
      if (isFirst) {
        printer = getFormat()
          .withHeader(configTable.getCSVHeaders(fieldsToReturn, exportDescription).toArray(new String[0]))
          .print(writer);
        isFirst = false;
      }

      printer.printRecord(HandlebarsUtils.getCellValues(row, configTable, fieldsToReturn));
    }
  }
}
