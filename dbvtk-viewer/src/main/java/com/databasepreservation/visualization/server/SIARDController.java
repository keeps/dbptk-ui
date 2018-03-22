package com.databasepreservation.visualization.server;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.DatabaseMigration;
import com.databasepreservation.common.ModuleObserver;
import com.databasepreservation.model.Reporter;
import com.databasepreservation.model.data.Row;
import com.databasepreservation.model.exception.ModuleException;
import com.databasepreservation.model.modules.filters.ObservableFilter;
import com.databasepreservation.model.structure.DatabaseStructure;
import com.databasepreservation.model.structure.SchemaStructure;
import com.databasepreservation.model.structure.TableStructure;
import com.databasepreservation.modules.dbvtk.DbvtkModuleFactory;
import com.databasepreservation.modules.siard.SIARD2ModuleFactory;
import com.databasepreservation.visualization.utils.SolrManager;
import com.databasepreservation.visualization.utils.SolrUtils;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SIARDController {
  private static final Logger LOGGER = LoggerFactory.getLogger(SIARDController.class);

  public static String getReportFileContents(String databaseUUID) throws NotFoundException {
    Path reportPath = ViewerConfiguration.getInstance().getReportPath(databaseUUID);
    String result;
    if (Files.exists(reportPath)) {
      try (InputStream in = Files.newInputStream(reportPath)) {
        result = IOUtils.toString(in);
      } catch (IOException e) {
        throw new NotFoundException("The database does not have a conversion report.", e);
      }
    } else {
      throw new NotFoundException("The database does not have a conversion report.");
    }
    return result;
  }

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
    LOGGER.info("starting to convert database " + siardPath.toAbsolutePath().toString());

    // build the SIARD import module, Solr export module, and start the
    // conversion
    Path reporterPath = ViewerConfiguration.getInstance().getReportPath(databaseUUID).toAbsolutePath();
    try (Reporter reporter = new Reporter(reporterPath.getParent().toString(), reporterPath.getFileName().toString())) {
      ViewerConfiguration configuration = ViewerConfiguration.getInstance();

      DatabaseMigration databaseMigration = DatabaseMigration.newInstance();

      databaseMigration.importModule(new SIARD2ModuleFactory())
        .importModuleParameter(SIARD2ModuleFactory.PARAMETER_FILE, siardPath.toAbsolutePath().toString());

      databaseMigration.exportModule(new DbvtkModuleFactory())
        .exportModuleParameter(DbvtkModuleFactory.PARAMETER_HOSTNAME,
          configuration.getViewerConfigurationAsString(ViewerConfiguration.PROPERTY_SOLR_HOSTNAME))
        .exportModuleParameter(DbvtkModuleFactory.PARAMETER_PORT,
          configuration.getViewerConfigurationAsString(ViewerConfiguration.PROPERTY_SOLR_PORT))
        .exportModuleParameter(DbvtkModuleFactory.PARAMETER_ZOOKEEPER_HOST,
          configuration.getViewerConfigurationAsString(ViewerConfiguration.PROPERTY_ZOOKEEPER_HOSTNAME))
        .exportModuleParameter(DbvtkModuleFactory.PARAMETER_ZOOKEEPER_PORT,
          configuration.getViewerConfigurationAsString(ViewerConfiguration.PROPERTY_ZOOKEEPER_PORT))
        .exportModuleParameter(DbvtkModuleFactory.PARAMETER_LOB_FOLDER,
          ViewerConfiguration.getInstance().getLobPath().toString())
        .exportModuleParameter(DbvtkModuleFactory.PARAMETER_DATABASE_UUID, databaseUUID);

      databaseMigration.filter(new ObservableFilter(new ProgressObserver(databaseUUID)));

      databaseMigration.reporter(reporter);

      long startTime = System.currentTimeMillis();

      databaseMigration.migrate();

      long duration = System.currentTimeMillis() - startTime;
      LOGGER.info("Conversion time {}m {}s", duration / 60000, duration % 60000 / 1000);
      return true;
    } catch (IOException e) {
      throw new GenericException("Could not initialize conversion modules", e);
    } catch (ModuleException | RuntimeException e) {
      throw new GenericException("Could not convert the database to the Solr instance.", e);
    }
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
      // do nothing
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
