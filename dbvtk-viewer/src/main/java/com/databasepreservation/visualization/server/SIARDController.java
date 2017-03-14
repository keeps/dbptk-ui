package com.databasepreservation.visualization.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.common.ObservableModule;
import com.databasepreservation.visualization.utils.SolrManager;
import org.apache.solr.client.solrj.SolrClient;
import org.roda.core.data.exceptions.GenericException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.ModuleObserver;
import com.databasepreservation.model.Reporter;
import com.databasepreservation.model.exception.ModuleException;
import com.databasepreservation.model.exception.UnknownTypeException;
import com.databasepreservation.model.modules.DatabaseExportModule;
import com.databasepreservation.model.modules.DatabaseImportModule;
import com.databasepreservation.model.modules.DatabaseModuleFactory;
import com.databasepreservation.model.parameters.Parameter;
import com.databasepreservation.model.structure.DatabaseStructure;
import com.databasepreservation.model.structure.SchemaStructure;
import com.databasepreservation.model.structure.TableStructure;
import com.databasepreservation.modules.siard.SIARD2ModuleFactory;
import com.databasepreservation.modules.solr.SolrModuleFactory;
import com.databasepreservation.visualization.utils.SolrUtils;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SIARDController {
  private static final Logger LOGGER = LoggerFactory.getLogger(SIARDController.class);

  public static String REPORT_FILENAME = "report.md";

  public static String loadFromLocal(String localPath) throws GenericException {
    String databaseUUID = SolrUtils.randomUUID();
    LOGGER.info("converting database " + databaseUUID);
    try {
      Path workingDirectory = createUploadWorkingDirectory(databaseUUID);
      if (convertSIARDtoSolr(Paths.get(localPath), workingDirectory, databaseUUID)) {
        LOGGER.info("Conversion to SIARD successful, database: " + databaseUUID);
      } else {
        LOGGER.error("Conversion to SIARD failed for database " + databaseUUID);
      }
    } catch (IOException e) {
      throw new GenericException("could not create temporary working directory", e);
    }
    return databaseUUID;
  }

  private static Path createUploadWorkingDirectory(String databaseUUID) throws IOException {
    return Files.createDirectories(ViewerConfiguration.getInstance().getUploadsPath().resolve(databaseUUID));
  }

  private static boolean convertSIARDtoSolr(Path siardPath, Path workingDirectory, String databaseUUID)
    throws GenericException {
    boolean conversionCompleted = false;
    LOGGER.info("starting to convert database " + siardPath.toAbsolutePath().toString());

    // build the SIARD import module, Solr export module, and start the
    // conversion
    try {
      // TODO: support defining specific reporter filename
      Reporter reporter = new Reporter(workingDirectory.toAbsolutePath().toString(), REPORT_FILENAME);

      DatabaseModuleFactory siardImportFactory = new SIARD2ModuleFactory(reporter);
      Map<Parameter, String> siardParameters = new HashMap<>();
      siardParameters.put(siardImportFactory.getAllParameters().get("file"), siardPath.toAbsolutePath().toString());
      DatabaseImportModule siardImportModule = siardImportFactory.buildImportModule(siardParameters);
      siardImportModule.setOnceReporter(reporter);

      ProgressObserver progressObserver = new ProgressObserver(databaseUUID);
      ((ObservableModule)siardImportModule).addModuleObserver(progressObserver);

      DatabaseModuleFactory solrExportFactory = new SolrModuleFactory(reporter);
      Map<Parameter, String> solrParameters = new HashMap<>();
      ViewerConfiguration configuration = ViewerConfiguration.getInstance();
      solrParameters.put(solrExportFactory.getAllParameters().get("hostname"),
        configuration.getViewerConfigurationAsString(ViewerConfiguration.PROPERTY_SOLR_HOSTNAME));
      solrParameters.put(solrExportFactory.getAllParameters().get("port"),
        configuration.getViewerConfigurationAsString(ViewerConfiguration.PROPERTY_SOLR_PORT));
      solrParameters.put(solrExportFactory.getAllParameters().get("zookeeper-hostname"),
        configuration.getViewerConfigurationAsString(ViewerConfiguration.PROPERTY_ZOOKEEPER_HOSTNAME));
      solrParameters.put(solrExportFactory.getAllParameters().get("zookeeper-port"),
        configuration.getViewerConfigurationAsString(ViewerConfiguration.PROPERTY_ZOOKEEPER_PORT));
      solrParameters.put(solrExportFactory.getAllParameters().get("database-id"), databaseUUID);

      DatabaseExportModule solrExportModule = solrExportFactory.buildExportModule(solrParameters);
      solrExportModule.setOnceReporter(reporter);

      long startTime = System.currentTimeMillis();
      try {
        siardImportModule.getDatabase(solrExportModule);
        conversionCompleted = true;
      } catch (ModuleException | UnknownTypeException | RuntimeException e) {
        throw new GenericException("Could not convert the database to the Solr instance.", e);
      }
      long duration = System.currentTimeMillis() - startTime;
      LOGGER.info("Conversion time " + (duration / 60000) + "m " + (duration % 60000 / 1000) + "s");
    } catch (ModuleException e) {
      throw new GenericException("Could not initialize conversion modules", e);
    }

    return conversionCompleted;
  }

  private static class ProgressObserver implements ModuleObserver {
    private final String databaseUUID;
    private final SolrManager solrManager;

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
      solrManager.updateDatabaseTotalSchemas(databaseUUID, structure.getSchemas().size());
    }

    @Override
    public void notifyOpenSchema(DatabaseStructure structure, SchemaStructure schema, long completedSchemas,
      long completedTablesInSchema) {
      solrManager.updateDatabaseCurrentSchema(databaseUUID, schema.getName(), completedSchemas, schema.getTables().size());
    }

    @Override
    public void notifyOpenTable(DatabaseStructure structure, TableStructure table, long completedSchemas,
      long completedTablesInSchema) {
      solrManager.updateDatabaseCurrentTable(databaseUUID, table.getName(), completedTablesInSchema, table.getRows());
    }

    @Override
    public void notifyTableProgress(DatabaseStructure structure, TableStructure table, long completedRows,
      long totalRows) {
      LOGGER.info("---------------- progress: {}/{}", completedRows, totalRows);
      solrManager.updateDatabaseCurrentRow(databaseUUID, completedRows);
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
}
