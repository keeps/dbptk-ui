package com.databasepreservation.common.client.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DataTransformationProgressData implements Serializable {
  private long rowsToProcess;
  private long processedRows;
  private boolean finished = false;

  private static Map<String, DataTransformationProgressData> instances = new HashMap<>();

  public DataTransformationProgressData() {
  }

  public static DataTransformationProgressData getInstance(String jobUUID) {
    return instances.computeIfAbsent(jobUUID, k -> new DataTransformationProgressData());
  }

  public static Map<String, DataTransformationProgressData> getInstances() {
    return instances;
  }

  public long getRowsToProcess() {
    return rowsToProcess;
  }

  public void setRowsToProcess(long rowsToProcess) {
    this.rowsToProcess = rowsToProcess;
  }

  public long getProcessedRows() {
    return processedRows;
  }

  public void setProcessedRows(long processedRows) {
    this.processedRows = processedRows;
  }

  public void incrementProcessedRows() {
    if(this.processedRows <= this.rowsToProcess){
      this.processedRows++;
    }
  }

  public boolean getFinished() {
    return finished;
  }

  public void setFinished(boolean status) {
    finished = status;
    if(finished){
      this.processedRows = this.rowsToProcess;
    }
  }

  public void reset(){
    finished = false;
    processedRows = 0;
  }
}
