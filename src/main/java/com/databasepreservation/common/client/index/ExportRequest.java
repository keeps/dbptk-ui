package com.databasepreservation.common.client.index;

import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ExportRequest implements Serializable {

  public String filename;

  public String zipFilename;

  public boolean exportDescription;

  public boolean exportLOBs;

  public boolean record;

  public ExportRequest() {
  }

  /*
   * Constructor
   */
  public ExportRequest(String filename, String zipFilename, boolean exportDescription, boolean exportLOBs, boolean record) {
    this.filename = filename;
    this.zipFilename = zipFilename;
    this.exportDescription = exportDescription;
    this.exportLOBs = exportLOBs;
    this.record = record;
  }

  @Override
  public String toString() {
    return "ExportRequest{" +
        "filename='" + filename + '\'' +
        ", zipFilename='" + zipFilename + '\'' +
        ", exportDescription=" + exportDescription +
        ", exportLOBs=" + exportLOBs +
        '}';
  }
}
