package com.databasepreservation.common.client.models.status.database;

import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class Indicators implements Serializable {

  private String success;
  private String failed;
  private String warnings;
  private String skipped;

  public Indicators() {
  }

  public Indicators(String success, String failed, String warnings, String skipped) {
    this.success = success;
    this.failed = failed;
    this.warnings = warnings;
    this.skipped = skipped;
  }

  public String getSuccess() {
    return success;
  }

  public void setSuccess(String success) {
    this.success = success;
  }

  public String getFailed() {
    return failed;
  }

  public void setFailed(String failed) {
    this.failed = failed;
  }

  public String getWarnings() {
    return warnings;
  }

  public void setWarnings(String warnings) {
    this.warnings = warnings;
  }

  public String getSkipped() {
    return skipped;
  }

  public void setSkipped(String skipped) {
    this.skipped = skipped;
  }
}
