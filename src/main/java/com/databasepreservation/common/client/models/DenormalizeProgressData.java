package com.databasepreservation.common.client.models;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DenormalizeProgressData implements Serializable {
  private long rowsToProcess;
  private long processedRows;
  private String tableUUID;
  private boolean finished = false;

  private static HashMap<String, DenormalizeProgressData> instances = new HashMap<>();

  public static DenormalizeProgressData getInstance(String databaseUUID, String tableUUID) {
    String uuid = databaseUUID + tableUUID;
    return instances.computeIfAbsent(uuid, k -> new DenormalizeProgressData());
  }

  public DenormalizeProgressData() {
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

  public void setTableUUID(String tableUUID) {
    this.tableUUID = tableUUID;
  }

  public String getTableUUID() {
    return tableUUID;
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
