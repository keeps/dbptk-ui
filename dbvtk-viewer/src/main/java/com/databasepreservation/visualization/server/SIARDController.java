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
import com.databasepreservation.model.Reporter;
import com.databasepreservation.model.exception.ModuleException;
import com.databasepreservation.model.modules.filters.ObservableFilter;
import com.databasepreservation.modules.dbvtk.DbvtkModuleFactory;
import com.databasepreservation.modules.siard.SIARD2ModuleFactory;
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
    LOGGER.info("converting database {}", databaseUUID);
    try {
      convertSIARDtoSolr(Paths.get(localPath), databaseUUID);
      LOGGER.info("Conversion to SIARD successful, database: {}", databaseUUID);
    } catch (GenericException e) {
      LOGGER.error("Conversion to SIARD failed for database {}", databaseUUID);
      throw e;
    }
    return databaseUUID;
  }

  private static boolean convertSIARDtoSolr(Path siardPath, String databaseUUID) throws GenericException {
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
}
