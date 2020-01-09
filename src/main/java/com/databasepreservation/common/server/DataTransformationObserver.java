package com.databasepreservation.common.server;

import com.databasepreservation.common.client.models.DataTransformationProgressData;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DataTransformationObserver {
  private DataTransformationProgressData progressData;

  public DataTransformationObserver(String databaseUUID, String tableUUID) {
    progressData = DataTransformationProgressData.getInstance(databaseUUID, tableUUID);
    progressData.setProcessedRows(0);
    progressData.setFinished(false);
  }

  public void notifyStartDataTransformation(long rowsToProcess){
      progressData.setRowsToProcess(rowsToProcess);
  }

  public void notifyProcessedRow(){
    progressData.incrementProcessedRows();
  }

  public void notifyFinishDataTransformation(){
    progressData.setFinished(true);
  }

}
