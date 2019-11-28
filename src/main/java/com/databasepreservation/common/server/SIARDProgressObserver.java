package com.databasepreservation.common.server;

import com.databasepreservation.common.ModuleObserver;
import com.databasepreservation.common.client.models.ProgressData;
import com.databasepreservation.model.data.Row;
import com.databasepreservation.model.structure.DatabaseStructure;
import com.databasepreservation.model.structure.SchemaStructure;
import com.databasepreservation.model.structure.TableStructure;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class SIARDProgressObserver implements ModuleObserver {
  private ProgressData progressData;

  public SIARDProgressObserver(String UUID) {
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
  public void notifyOpenSchema(DatabaseStructure databaseStructure, SchemaStructure schemaStructure,
    long completedSchemas, long completedTablesInSchema) {
    progressData.setCurrentSchemaName(schemaStructure.getName());
    progressData.setProcessedSchemas(completedSchemas);
    progressData.setTotalTables(schemaStructure.getTables().size());
  }

  @Override
  public void notifyOpenTable(DatabaseStructure databaseStructure, TableStructure tableStructure, long completedSchemas,
    long completedTablesInSchema) {
    progressData.setCurrentTableName(tableStructure.getName());
    progressData.setCurrentProcessedTableRows(0);
    progressData.setPreviousProcessedRows(0);
    progressData.setCurrentTableTotalRows(tableStructure.getRows());
  }

  @Override
  public void notifyTableProgressSparse(DatabaseStructure databaseStructure, TableStructure tableStructure,
    long completedRows, long totalRows) {
    progressData.setCurrentProcessedTableRows(completedRows + 1);
    progressData.incrementProcessedRows(completedRows + 1);
  }

  @Override
  public void notifyTableProgressDetailed(DatabaseStructure databaseStructure, TableStructure tableStructure, Row row,
    long completedRows, long totalRows) {
    progressData.setCurrentProcessedTableRows(completedRows + 1);
    progressData.incrementProcessedRows(completedRows + 1);
  }

  @Override
  public void notifyCloseTable(DatabaseStructure databaseStructure, TableStructure tableStructure,
    long completedSchemas, long completedTablesInSchema) {
    progressData.setProcessedTables(completedTablesInSchema);
  }

  @Override
  public void notifyCloseSchema(DatabaseStructure databaseStructure, SchemaStructure schemaStructure,
    long completedSchemas, long completedTablesInSchema) {
    // DO NOTHING
  }

  @Override
  public void notifyCloseDatabase(DatabaseStructure databaseStructure) {
    progressData.reset();
    progressData.setFinished(true);
  }
}
