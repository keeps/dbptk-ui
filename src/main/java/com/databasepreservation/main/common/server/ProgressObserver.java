package com.databasepreservation.main.common.server;

import com.databasepreservation.common.ModuleObserver;
import com.databasepreservation.model.data.Row;
import com.databasepreservation.model.structure.DatabaseStructure;
import com.databasepreservation.model.structure.SchemaStructure;
import com.databasepreservation.model.structure.TableStructure;
import com.databasepreservation.main.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ProgressObserver implements ModuleObserver {
  private final String databaseUUID;
  private final DatabaseRowsSolrManager solrManager;

  public ProgressObserver(String databaseUUID) {
    this.databaseUUID = databaseUUID;
    this.solrManager = ViewerFactory.getSolrManager();
  }

  @Override
  public void notifyOpenDatabase() {
    // do nothing
  }

  @Override
  public void notifyStructureObtained(DatabaseStructure structure) {
    // can not do this yet because the database has not yet been created by dbvtk
    // export module

    // solrManager.updateDatabaseTotalSchemas(databaseUUID,
    // structure.getSchemas().size());
  }

  @Override
  public void notifyOpenSchema(DatabaseStructure structure, SchemaStructure schema, long completedSchemas,
    long completedTablesInSchema) {
    solrManager.updateDatabaseCurrentSchema(databaseUUID, schema.getName(), completedSchemas,
      schema.getTables().size());
  }

  @Override
  public void notifyOpenTable(DatabaseStructure structure, TableStructure table, long completedSchemas,
    long completedTablesInSchema) {
    solrManager.updateDatabaseCurrentTable(databaseUUID, table.getName(), completedTablesInSchema, table.getRows());
  }

  @Override
  public void notifyTableProgressSparse(DatabaseStructure structure, TableStructure table, long completedRows,
    long totalRows) {
    solrManager.updateDatabaseCurrentRow(databaseUUID, completedRows);
  }

  @Override
  public void notifyTableProgressDetailed(DatabaseStructure structure, TableStructure table, Row row,
    long completedRows, long totalRows) {
    // do nothing for each row
  }

  @Override
  public void notifyCloseTable(DatabaseStructure structure, TableStructure table, long completedSchemas,
    long completedTablesInSchema) {
    // do nothing
  }

  @Override
  public void notifyCloseSchema(DatabaseStructure structure, SchemaStructure schema, long completedSchemas,
    long completedTablesInSchema) {
    // do nothing
  }

  @Override
  public void notifyCloseDatabase(DatabaseStructure structure) {
    solrManager.updateDatabaseIngestionFinished(databaseUUID);
  }
}
