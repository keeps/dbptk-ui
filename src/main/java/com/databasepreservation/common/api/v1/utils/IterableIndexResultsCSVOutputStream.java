package com.databasepreservation.common.api.v1.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVPrinter;

import com.databasepreservation.common.api.utils.HandlebarsUtils;
import com.databasepreservation.common.client.models.configuration.collection.ViewerTableConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.server.index.utils.IterableIndexResult;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class IterableIndexResultsCSVOutputStream extends CSVOutputStream {
  /** The results to write to output stream. */
  private final IterableIndexResult results;
  private final ViewerTableConfiguration configTable;
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
  public IterableIndexResultsCSVOutputStream(final IterableIndexResult results, final ViewerTableConfiguration configTable,
    final String filename, final boolean exportDescription, final char delimiter, String fieldsToHeader) {
    super(filename, delimiter);
    this.results = results;
    this.configTable = configTable;
    this.fieldsToReturn = Stream.of(fieldsToHeader.split(",")).collect(Collectors.toList());
    this.exportDescription = exportDescription;
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

      printer.printRecord(HandlebarsUtils.getCellValues(row, configTable, fieldsToReturn));

    }

    writer.flush();
  }
}
