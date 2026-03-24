package com.databasepreservation.common.client.models.structure;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ViewerJobStepExecution implements Serializable {
  @Serial
  private static final long serialVersionUID = -8867114953667932644L;

  private String name;
  private String status;
  private long processed;
  private long skips;
  private long duration;

  public ViewerJobStepExecution() {
  }

  public ViewerJobStepExecution(String name, String status, long processed, long skips, long duration) {
    this.name = name;
    this.status = status;
    this.processed = processed;
    this.skips = skips;
    this.duration = duration;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public long getProcessed() {
    return processed;
  }

  public void setProcessed(long processed) {
    this.processed = processed;
  }

  public long getSkips() {
    return skips;
  }

  public void setSkips(long skips) {
    this.skips = skips;
  }

  public long getDuration() {
    return duration;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }
}
