package com.databasepreservation.main.common.shared;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class SIARDProgressData implements Serializable {

  private String uuid;
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

  private static HashMap<String, SIARDProgressData> instances = new HashMap<>();

  public static SIARDProgressData getInstance(String UUID) {
    if (instances.get(UUID) == null) {
      instances.put(UUID, new SIARDProgressData());
    }
    return instances.get(UUID);
  }

  public SIARDProgressData() {
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
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
    SIARDProgressData that = (SIARDProgressData) o;
    return getProcessedRows() == that.getProcessedRows() && getTotalRows() == that.getTotalRows()
      && getProcessedTables() == that.getProcessedTables() && getTotalTables() == that.getTotalTables()
      && getProcessedSchemas() == that.getProcessedSchemas() && getTotalSchemas() == that.getTotalSchemas()
      && Objects.equals(getUuid(), that.getUuid()) && Objects.equals(getCurrentTableName(), that.getCurrentTableName())
      && Objects.equals(getCurrentSchemaName(), that.getCurrentSchemaName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getUuid(), getProcessedRows(), getTotalRows(), getProcessedTables(), getTotalTables(),
      getProcessedSchemas(), getTotalSchemas(), getCurrentTableName(), getCurrentSchemaName());
  }
}
