package com.databasepreservation.common.server;

import com.databasepreservation.common.ModuleObserver;
import com.databasepreservation.common.client.models.progress.ProgressData;
import com.databasepreservation.model.data.Row;
import com.databasepreservation.model.structure.DatabaseStructure;
import com.databasepreservation.model.structure.SchemaStructure;
import com.databasepreservation.model.structure.TableStructure;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ProgressObserver implements ModuleObserver {
  private ProgressData progressData;

  public ProgressObserver(String UUID) {
    progressData = ProgressData.getInstance(UUID);
  }

  @Override
  public void notifyOpenDatabase() {
    progressData.reset();
    progressData.setDatabaseStructureRetrieved(false);
  }

  @Override
  public void notifyStructureObtained(DatabaseStructure databaseStructure) {
    long totalRows = 0;
    for (SchemaStructure schema : databaseStructure.getSchemas()) {
      for (TableStructure table : schema.getTables()) {
        totalRows += table.getRows();
      }
    }
    progressData.setDatabaseStructureRetrieved(true);
    progressData.setTotalSchemas(databaseStructure.getSchemas().size());
    progressData.setTotalRows(totalRows);
  }

  @Override
  public void notifyOpenSchema(DatabaseStructure structure, SchemaStructure schemaStructure, long completedSchemas,
    long completedTablesInSchema) {
    progressData.setCurrentSchemaName(schemaStructure.getName());
    progressData.setProcessedSchemas(completedSchemas);
    progressData.setTotalTables(schemaStructure.getTables().size());
  }

  @Override
  public void notifyOpenTable(DatabaseStructure structure, TableStructure tableStructure, long completedSchemas,
    long completedTablesInSchema) {
    progressData.setCurrentTableName(tableStructure.getName());
    progressData.setCurrentProcessedTableRows(0);
    progressData.setPreviousProcessedRows(0);
    progressData.setCurrentTableTotalRows(tableStructure.getRows());
  }

  @Override
  public void notifyTableProgressSparse(DatabaseStructure structure, TableStructure table, long completedRows,
    long totalRows) {
    progressData.setCurrentProcessedTableRows(completedRows + 1);
    progressData.incrementProcessedRows(completedRows + 1);
  }

  @Override
  public void notifyTableProgressDetailed(DatabaseStructure structure, TableStructure table, Row row,
    long completedRows, long totalRows) {
    progressData.setCurrentProcessedTableRows(completedRows + 1);
    progressData.incrementProcessedRows(completedRows + 1);
  }

  @Override
  public void notifyCloseTable(DatabaseStructure structure, TableStructure table, long completedSchemas,
    long completedTablesInSchema) {
    progressData.setProcessedTables(completedTablesInSchema);
  }

  @Override
  public void notifyCloseSchema(DatabaseStructure structure, SchemaStructure schema, long completedSchemas,
    long completedTablesInSchema) {

  }

  @Override
  public void notifyCloseDatabase(DatabaseStructure structure) {
    progressData.reset();
    progressData.setFinished(true);
  }
}
