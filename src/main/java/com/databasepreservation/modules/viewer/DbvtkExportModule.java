package com.databasepreservation.modules.viewer;

import java.nio.file.Path;
import java.util.Set;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;

import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.common.shared.ViewerStructure.ViewerDatabaseFromToolkit;
import com.databasepreservation.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.common.transformers.ToolkitStructure2ViewerStructure;
import com.databasepreservation.model.Reporter;
import com.databasepreservation.model.data.Row;
import com.databasepreservation.model.exception.ModuleException;
import com.databasepreservation.model.exception.UnknownTypeException;
import com.databasepreservation.model.modules.DatabaseExportModule;
import com.databasepreservation.model.modules.ModuleSettings;
import com.databasepreservation.model.structure.DatabaseStructure;
import com.databasepreservation.modules.DefaultExceptionNormalizer;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DbvtkExportModule implements DatabaseExportModule {
  private final DbvtkModuleConfiguration configuration;

  private final DatabaseRowsSolrManager solrManager;

  private DatabaseStructure structure;

  private ViewerDatabaseFromToolkit viewerDatabase;

  private ViewerDatabase retrieved;

  private ViewerTable currentTable;

  private String databaseUUID;

  private long rowIndex = 0;

  private Reporter reporter;

  public DbvtkExportModule(Path lobFolder) {
    this(null, lobFolder);
  }

  public DbvtkExportModule(String databaseUUID, Path lobFolder) {
    solrManager = ViewerFactory.getSolrManager();
    try {
      retrieved = solrManager.retrieve(ViewerDatabase.class, databaseUUID);
    } catch (NotFoundException | GenericException e) {
      retrieved = null;
    }
    this.databaseUUID = databaseUUID;
    configuration = DbvtkModuleConfiguration.getInstance(lobFolder);
  }

  /**
   * Gets custom settings set by the export module that modify behaviour of the
   * import module.
   *
   * @throws ModuleException
   */
  @Override
  public ModuleSettings getModuleSettings() throws ModuleException {
    return new ModuleSettings();
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
    this.structure = structure;
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
    solrManager.addTable(currentTable);
    rowIndex = 0;
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
    solrManager.addRow(retrieved.getUUID(),
      ToolkitStructure2ViewerStructure.getRow(configuration, databaseUUID, currentTable, row, rowIndex++));
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
    this.reporter = reporter;
  }

  @Override
  public ModuleException normalizeException(Exception exception, String contextMessage) {
    return DefaultExceptionNormalizer.getInstance().normalizeException(exception, contextMessage);
  }
}
