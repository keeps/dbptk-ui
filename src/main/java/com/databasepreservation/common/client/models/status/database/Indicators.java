/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.status.database;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonPropertyOrder({"success", "failed", "warnings", "skipped", "errors"})
public class Indicators implements Serializable {

  private String success;
  private String failed;
  private String warnings;
  private String skipped;
  private String errors;

  public Indicators() {
  }

  public Indicators(String success, String failed, String warnings, String skipped, String errors) {
    this.success = success;
    this.failed = failed;
    this.warnings = warnings;
    this.skipped = skipped;
    this.errors = errors;
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

  public String getErrors() {
    return errors;
  }

  public void setErrors(String errors) {
    this.errors = errors;
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
