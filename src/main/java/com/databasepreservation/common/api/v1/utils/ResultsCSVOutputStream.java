package com.databasepreservation.common.api.v1.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.commons.csv.CSVPrinter;
import org.roda.core.data.v2.index.IndexResult;

import com.databasepreservation.common.shared.ViewerStructure.IsIndexed;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ResultsCSVOutputStream<T extends IsIndexed> extends CSVOutputStream {
  /** The results to write to output stream. */
  private final IndexResult<T> results;

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
  public ResultsCSVOutputStream(final IndexResult<T> results, final String filename, final char delimiter) {
    super(filename, delimiter);
    this.results = results;
  }

  @Override
  public void consumeOutputStream(final OutputStream out) throws IOException {
    final OutputStreamWriter writer = new OutputStreamWriter(out);
    CSVPrinter printer = null;
    boolean isFirst = true;
    for (final T result : this.results.getResults()) {
      if (isFirst) {
        printer = getFormat().withHeader(result.toCsvHeaders().toArray(new String[0])).print(writer);
        isFirst = false;
      }
      printer.printRecord(result.toCsvValues());
    }
    writer.flush();
  }
}
