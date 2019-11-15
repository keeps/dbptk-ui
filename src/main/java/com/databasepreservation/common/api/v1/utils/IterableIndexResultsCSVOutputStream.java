package com.databasepreservation.common.api.v1.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import org.apache.commons.csv.CSVPrinter;

import com.databasepreservation.common.server.index.utils.IterableIndexResult;
import com.databasepreservation.common.shared.ViewerStructure.ViewerRow;
import com.databasepreservation.common.shared.ViewerStructure.ViewerTable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class IterableIndexResultsCSVOutputStream extends CSVOutputStream {
  /** The results to write to output stream. */
  private final IterableIndexResult results;
  private final ViewerTable table;
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
  public IterableIndexResultsCSVOutputStream(final IterableIndexResult results, final ViewerTable table, final List<String> fieldsToReturn, final String filename,
                                             final boolean exportDescription, final char delimiter) {
    super(filename, delimiter);
    this.results = results;
    this.table = table;
    this.fieldsToReturn = fieldsToReturn;
    this.exportDescription = exportDescription;
  }

  @Override
  public void consumeOutputStream(final OutputStream out) throws IOException {
    final OutputStreamWriter writer = new OutputStreamWriter(out);
    CSVPrinter printer = null;
    boolean isFirst = true;

    for (ViewerRow row : results) {
      if (isFirst) {
        printer = getFormat().withHeader(table.getCSVHeaders(fieldsToReturn, exportDescription).toArray(new String[0])).print(writer);
        isFirst = false;
      }

      printer.printRecord(row.getCellValues(fieldsToReturn));

    }

    writer.flush();
  }
}
