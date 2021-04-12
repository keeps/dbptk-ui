/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.modules.viewer;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrServerException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.LargeObjectConsolidateProperty;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.common.server.index.schema.SolrRowsCollectionRegistry;
import com.databasepreservation.common.transformers.ToolkitStructure2ViewerStructure;
import com.databasepreservation.model.data.Row;
import com.databasepreservation.model.exception.ModuleException;
import com.databasepreservation.model.exception.UnknownTypeException;
import com.databasepreservation.model.modules.filters.DatabaseFilterModule;
import com.databasepreservation.model.reporters.Reporter;
import com.databasepreservation.model.structure.DatabaseStructure;
import com.databasepreservation.modules.DefaultExceptionNormalizer;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DbvtkExportModule implements DatabaseFilterModule {
  private final DatabaseRowsSolrManager solrManager;
  private CollectionStatus collectionConfiguration;
  private ViewerDatabase retrieved;
  private ViewerTable currentTable;
  private String databaseUUID;
  private long rowIndex = 1;

  public DbvtkExportModule(String databaseUUID) {
    solrManager = ViewerFactory.getSolrManager();
    try {
      retrieved = solrManager.retrieve(ViewerDatabase.class, databaseUUID);
      collectionConfiguration = ViewerFactory.getConfigurationManager().getConfigurationCollection(databaseUUID,
        databaseUUID);
    } catch (NotFoundException | GenericException e) {
      retrieved = null;
    }
    this.databaseUUID = databaseUUID;
  }

  /**
   * Initialize the database, this will be the first method called
   *
   * @throws ModuleException
   */
  @Override
  public void initDatabase() throws ModuleException {
    // setup is done when DBVTK starts
  }

  /**
   * Set ignored schemas. Ignored schemas won't be exported. This method should be
   * called before handleStructure. However, if not called it will be assumed
   * there are not ignored schemas.
   *
   * @param ignoredSchemas
   *          the set of schemas to ignored
   */
  @Override
  public void setIgnoredSchemas(Set<String> ignoredSchemas) {

  }

  /**
   * Handle the database structure. This method will called after
   * setIgnoredSchemas.
   *
   * @param structure
   *          the database structure
   * @throws ModuleException
   * @throws UnknownTypeException
   */
  @Override
  public void handleStructure(DatabaseStructure structure) throws ModuleException {
    solrManager.addDatabaseRowCollection(databaseUUID);
  }

  /**
   * Prepare to handle the data of a new schema. This method will be called after
   * handleStructure or handleDataCloseSchema.
   *
   * @param schemaName
   *          the schema name
   * @throws ModuleException
   */
  @Override
  public void handleDataOpenSchema(String schemaName) throws ModuleException {
    // viewerDatabase.getSchema(schemaName);
  }

  /**
   * Prepare to handle the data of a new table. This method will be called after
   * the handleDataOpenSchema, and before some calls to handleDataRow. If there
   * are no rows in the table, then handleDataCloseTable is called after this
   * method.
   *
   * @param tableId
   *          the table id
   * @throws ModuleException
   */
  @Override
  public void handleDataOpenTable(String tableId) throws ModuleException {
    currentTable = retrieved.getMetadata().getTableById(tableId);
    solrManager.addTable(retrieved.getUuid(), currentTable);
    // rowIndex = 1;
  }

  /**
   * Handle a table row. This method will be called after the table was open and
   * before it was closed, by row index order.
   *
   * @param row
   *          the table row
   * @throws ModuleException
   */
  @Override
  public void handleDataRow(Row row) throws ModuleException {
    solrManager.addRow(retrieved.getUuid(),
      ToolkitStructure2ViewerStructure.getRow(collectionConfiguration, currentTable, row, rowIndex++));
  }

  /**
   * Finish handling the data of a table. This method will be called after all
   * table rows for the table where requested to be handled.
   *
   * @param tableId
   *          the table id
   * @throws ModuleException
   */
  @Override
  public void handleDataCloseTable(String tableId) throws ModuleException {
    // committing + optimizing after whole database
  }

  /**
   * Finish handling the data of a schema. This method will be called after all
   * schemas of the schema were requested to be handled.
   *
   * @param schemaName
   *          the schema name
   * @throws ModuleException
   */
  @Override
  public void handleDataCloseSchema(String schemaName) throws ModuleException {
    // committing + optimizing after whole database

    try {
      ViewerFactory.getSolrClient().commit(SolrRowsCollectionRegistry.get(databaseUUID).getIndexName());
    } catch (SolrServerException | IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Finish the database. This method will be called when all data was requested
   * to be handled. This is the last method.
   *
   * @throws ModuleException
   */
  @Override
  public void finishDatabase() throws ModuleException {
    solrManager.markDatabaseAsReady(databaseUUID);
    collectionConfiguration.setConsolidateProperty(LargeObjectConsolidateProperty.NOT_CONSOLIDATED);
    ViewerFactory.getConfigurationManager().updateCollectionStatus(databaseUUID, collectionConfiguration);
  }

  @Override
  public void updateModuleConfiguration(String s, Map<String, String> map, Map<String, String> map1) {
    // do nothing
  }

  /**
   * Provide a reporter through which potential conversion problems should be
   * reported. This reporter should be provided only once for the export module
   * instance.
   *
   * @param reporter
   *          The initialized reporter instance.
   */
  @Override
  public void setOnceReporter(Reporter reporter) {
  }

  @Override
  public DatabaseFilterModule migrateDatabaseTo(DatabaseFilterModule databaseFilterModule) throws ModuleException {
    return this;
  }

  @Override
  public ModuleException normalizeException(Exception exception, String contextMessage) {
    return DefaultExceptionNormalizer.getInstance().normalizeException(exception, contextMessage);
  }
}
