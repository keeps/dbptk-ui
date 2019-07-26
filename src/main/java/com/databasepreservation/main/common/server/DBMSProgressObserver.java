package com.databasepreservation.main.common.server;

import com.databasepreservation.common.ModuleObserver;
import com.databasepreservation.main.common.shared.ProgressData;
import com.databasepreservation.main.common.shared.client.common.utils.JavascriptUtils;
import com.databasepreservation.model.data.Row;
import com.databasepreservation.model.structure.DatabaseStructure;
import com.databasepreservation.model.structure.SchemaStructure;
import com.databasepreservation.model.structure.TableStructure;
import com.google.gwt.core.client.GWT;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DBMSProgressObserver implements ModuleObserver {
  private ProgressData progressData;

  public DBMSProgressObserver(String UUID) {
    progressData = ProgressData.getInstance(UUID);
  }

  @Override
  public void notifyOpenDatabase() {
    progressData.reset();
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
    progressData.setProcessedTables(completedTablesInSchema);
    progressData.setCurrentTableTotalRows(tableStructure.getRows());
  }

  @Override
  public void notifyTableProgressSparse(DatabaseStructure databaseStructure, TableStructure tableStructure,
    long completedRows, long totalRows) {
    progressData.setCurrentProcessedTableRows(completedRows);
  }

  @Override
  public void notifyTableProgressDetailed(DatabaseStructure databaseStructure, TableStructure tableStructure, Row row,
    long completedRows, long totalRows) {
    progressData.setCurrentProcessedTableRows(completedRows);
  }

  @Override
  public void notifyCloseTable(DatabaseStructure databaseStructure, TableStructure tableStructure,
    long completedSchemas, long completedTablesInSchema) {
    progressData.setProcessedTables(completedTablesInSchema);
    progressData.setCurrentProcessedTableRows(tableStructure.getRows());
    progressData.setProcessedRows(progressData.getProcessedRows() + tableStructure.getRows());
  }

  @Override
  public void notifyCloseSchema(DatabaseStructure databaseStructure, SchemaStructure schemaStructure,
    long completedSchemas, long completedTablesInSchema) {
    progressData.setProcessedSchemas(completedSchemas);
  }

  @Override
  public void notifyCloseDatabase(DatabaseStructure databaseStructure) {
    progressData.setFinished(true);
  }
}
