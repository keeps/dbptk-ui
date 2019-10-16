package com.databasepreservation.common.api.v1.utils;

import com.databasepreservation.common.api.common.ConsumesOutputStream;
import com.databasepreservation.common.api.utils.ExtraMediaType;
import org.apache.commons.csv.CSVFormat;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public abstract class CSVOutputStream implements ConsumesOutputStream {
  /** The filename. */
  private final String filename;
  /** The CSV field delimiter. */
  private final char delimiter;

  /**
   * Constructor.
   *
   * @param filename
   *          the filename.
   * @param delimiter
   *          the CSV field delimiter.
   */
  public CSVOutputStream(final String filename, final char delimiter) {
    this.filename = filename;
    this.delimiter = delimiter;
  }

  @Override
  public String getFileName() {
    return filename;
  }

  @Override
  public String getMediaType() {
    return ExtraMediaType.TEXT_CSV;
  }

  protected CSVFormat getFormat() {
    return CSVFormat.EXCEL.withDelimiter(this.delimiter);
  }
}
