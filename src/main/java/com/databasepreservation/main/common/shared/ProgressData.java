package com.databasepreservation.main.common.shared;

import com.google.gwt.core.client.GWT;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ProgressData implements Serializable {

  private long processedRows;
  private long totalRows;
  private long processedTables;
  private long totalTables;
  private long processedSchemas;
  private long totalSchemas;
  private long currentProcessedTableRows;
  private long currentTableTotalRows;
  private String currentTableName;
  private String currentSchemaName;
  private boolean finished = false;
  private boolean databaseStructureRetrieved = false;

  private static HashMap<String, ProgressData> instances = new HashMap<>();

  public static ProgressData getInstance(String uuid) {
    if (instances.get(uuid) == null) {
      instances.put(uuid, new ProgressData());
    }
    return instances.get(uuid);
  }

  public ProgressData() {
  }

  public long getProcessedRows() {
    return processedRows;
  }

  public void setProcessedRows(long processedRows) {
    this.processedRows = processedRows;
  }

  public long getTotalRows() {
    return totalRows;
  }

  public void setTotalRows(long totalRows) {
    this.totalRows = totalRows;
  }

  public long getProcessedTables() {
    return processedTables;
  }

  public void setProcessedTables(long processedTables) {
    this.processedTables = processedTables;
  }

  public long getTotalTables() {
    return totalTables;
  }

  public void setTotalTables(long totalTables) {
    this.totalTables = totalTables;
  }

  public long getProcessedSchemas() {
    return processedSchemas;
  }

  public void setProcessedSchemas(long processedSchemas) {
    this.processedSchemas = processedSchemas;
  }

  public long getTotalSchemas() {
    return totalSchemas;
  }

  public void setTotalSchemas(long totalSchemas) {
    this.totalSchemas = totalSchemas;
  }

  public long getCurrentProcessedTableRows() {
    return currentProcessedTableRows;
  }

  public void setCurrentProcessedTableRows(long currentProcessedTableRows) {
    this.currentProcessedTableRows = currentProcessedTableRows;
  }

  public long getCurrentTableTotalRows() {
    return currentTableTotalRows;
  }

  public void setCurrentTableTotalRows(long currentTableTotalRows) {
    this.currentTableTotalRows = currentTableTotalRows;
  }

  public String getCurrentTableName() {
    return currentTableName;
  }

  public void setCurrentTableName(String currentTableName) {
    this.currentTableName = currentTableName;
  }

  public String getCurrentSchemaName() {
    return currentSchemaName;
  }

  public void setCurrentSchemaName(String currentSchemaName) {
    this.currentSchemaName = currentSchemaName;
  }


  public boolean isFinished() {
    return finished;
  }

  public void setFinished(boolean finished) {
    this.finished = finished;
  }

  public boolean isDatabaseStructureRetrieved() {
    return databaseStructureRetrieved;
  }

  public void setDatabaseStructureRetrieved(boolean databaseStructureRetrieved) {
    this.databaseStructureRetrieved = databaseStructureRetrieved;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    ProgressData that = (ProgressData) o;
    return getProcessedRows() == that.getProcessedRows() && getTotalRows() == that.getTotalRows()
      && getProcessedTables() == that.getProcessedTables() && getTotalTables() == that.getTotalTables()
      && getProcessedSchemas() == that.getProcessedSchemas() && getTotalSchemas() == that.getTotalSchemas()
      && Objects.equals(getCurrentTableName(), that.getCurrentTableName())
      && Objects.equals(getCurrentSchemaName(), that.getCurrentSchemaName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getProcessedRows(), getTotalRows(), getProcessedTables(), getTotalTables(),
      getProcessedSchemas(), getTotalSchemas(), getCurrentTableName(), getCurrentSchemaName());
  }

  public void clear() {
    processedRows = -1;
    totalRows = -1;
    processedTables = -1;
    totalTables = -1;
    processedSchemas = -1;
    totalSchemas = -1;
    currentProcessedTableRows = -1;
    currentTableTotalRows = -1;
    currentTableName = "";
    currentSchemaName = "";
    finished = false;
    databaseStructureRetrieved = false;
  }

  public void reset() {
    processedRows = 0;
    totalRows = 0;
    processedTables = 0;
    totalTables = 0;
    processedSchemas = 0;
    totalSchemas = 0;
    currentProcessedTableRows = 0;
    currentTableTotalRows = 0;
    currentTableName = "";
    currentSchemaName = "";
    finished = false;
    databaseStructureRetrieved = false;
  }
}
