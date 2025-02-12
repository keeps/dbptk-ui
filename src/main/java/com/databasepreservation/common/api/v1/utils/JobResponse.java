package com.databasepreservation.common.api.v1.utils;

import java.io.Serializable;

/**
 * @author António Lindo <alindo@keep.pt>
 */
public class JobResponse implements Serializable {

  private String jobId;

  private String jobStatus;

  private String jobCreationTime;

  public JobResponse() {
    // empty constructor
  }

  public JobResponse(String jobId, String jobStatus, String jobCreationTime) {
    this.jobId = jobId;
    this.jobStatus = jobStatus;
    this.jobCreationTime = jobCreationTime;
  }

  public String getJobId() {
    return jobId;
  }

  public String getJobCreationTime() {
    return jobCreationTime;
  }

  public void setJobCreationTime(String jobCreationTime) {
    this.jobCreationTime = jobCreationTime;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  public String getJobStatus() {
    return jobStatus;
  }

  public void setJobStatus(String jobStatus) {
    this.jobStatus = jobStatus;
  }
}
