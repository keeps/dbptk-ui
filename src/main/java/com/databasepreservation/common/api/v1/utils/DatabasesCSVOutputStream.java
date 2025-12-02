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
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.csv.CSVPrinter;

import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.server.index.utils.IterableDatabaseResult;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class DatabasesCSVOutputStream extends CSVOutputStream {
  /** The results to write to output stream. */
  private final IterableDatabaseResult<ViewerDatabase> results;

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
  public DatabasesCSVOutputStream(final IterableDatabaseResult<ViewerDatabase> results, final String filename,
    final char delimiter) {
    super(filename, delimiter);
    this.results = results;
  }

  @Override
  public void consumeOutputStream(final OutputStream out) throws IOException {
    final OutputStreamWriter writer = new OutputStreamWriter(out);
    CSVPrinter printer = null;

    multiRow(writer, printer);

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

  private void multiRow(OutputStreamWriter writer, CSVPrinter printer) throws IOException {
    boolean isFirst = true;

    Iterator<ViewerDatabase> iterator = this.results.iterator();
    while (iterator.hasNext()) {
      ViewerDatabase database = iterator.next();
      if (isFirst) {
        printer = getFormat().withHeader("Name").print(writer);
        isFirst = false;
      }

      String name = database.getMetadata() != null && database.getMetadata().getName() != null
        ? database.getMetadata().getName()
        : "";
      printer.printRecord(name);
    }
  }
}
