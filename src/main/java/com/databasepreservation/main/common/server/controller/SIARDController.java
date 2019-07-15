package com.databasepreservation.main.common.server.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.DatabaseMigration;
import com.databasepreservation.SIARDEdition;
import com.databasepreservation.main.common.server.ProgressObserver;
import com.databasepreservation.main.common.server.ViewerConfiguration;
import com.databasepreservation.main.common.server.ViewerFactory;
import com.databasepreservation.main.common.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.main.common.server.index.utils.SolrUtils;
import com.databasepreservation.main.common.server.transformers.ToolkitStructure2ViewerStructure;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabaseFromToolkit;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerMetadata;
import com.databasepreservation.main.common.shared.client.ClientLogger;
import com.databasepreservation.main.desktop.shared.models.DBPTKModule;
import com.databasepreservation.main.desktop.shared.models.PreservationParameter;
import com.databasepreservation.main.modules.viewer.DbvtkModuleFactory;
import com.databasepreservation.model.Reporter;
import com.databasepreservation.model.exception.ModuleException;
import com.databasepreservation.model.exception.UnsupportedModuleException;
import com.databasepreservation.model.modules.DatabaseImportModule;
import com.databasepreservation.model.modules.DatabaseModuleFactory;
import com.databasepreservation.model.modules.filters.ObservableFilter;
import com.databasepreservation.model.parameters.Parameter;
import com.databasepreservation.model.parameters.ParameterGroup;
import com.databasepreservation.model.parameters.Parameters;
import com.databasepreservation.model.structure.DatabaseStructure;
import com.databasepreservation.modules.jdbc.in.JDBCImportModule;
import com.databasepreservation.modules.siard.SIARD2ModuleFactory;
import com.databasepreservation.modules.siard.SIARDEditFactory;
import com.databasepreservation.utils.ReflectionUtils;

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

  public static boolean testConnection(String databaseUUID, String moduleName, HashMap<String, String> parameters)
    throws GenericException {
    JDBCImportModule jdbcImportModule = null;

    Path reporterPath = ViewerConfiguration.getInstance().getReportPath(databaseUUID).toAbsolutePath();
    try (Reporter reporter = new Reporter(reporterPath.getParent().toString(), reporterPath.getFileName().toString())) {
      DatabaseMigration databaseMigration = DatabaseMigration.newInstance();

      DatabaseModuleFactory factory = getDatabaseImportModuleFactory(moduleName);

      if (factory != null) {
        databaseMigration.importModule(factory);

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
          databaseMigration.importModuleParameter(entry.getKey(), entry.getValue());
        }

        databaseMigration.reporter(reporter);

        DatabaseImportModule importModule = databaseMigration.getImportModule();

        if (importModule instanceof JDBCImportModule) {
          jdbcImportModule = (JDBCImportModule) importModule;
          boolean value = jdbcImportModule.testConnection();
          jdbcImportModule.closeConnection();
          return value;
        }
      }
    } catch (IOException e) {
      throw new GenericException("Could not initialize conversion modules", e);
    } catch (ModuleException e) {
      throw new GenericException(e.getMessage());
    } /* finally {
      if (jdbcImportModule != null) {
        try {
          jdbcImportModule.closeConnection();
        } catch (ModuleException e) {
          new ClientLogger(SIARDController.class.getName())
              .error("JDBC SIARD Connection error - " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);

        }
      }
    }*/

    return false;
  }

  public static ViewerMetadata getDatabaseMetadata(String databaseUUID, String moduleName, HashMap<String, String> parameters) throws GenericException {
    JDBCImportModule jdbcImportModule = null;

    Path reporterPath = ViewerConfiguration.getInstance().getReportPath(databaseUUID).toAbsolutePath();
    try (Reporter reporter = new Reporter(reporterPath.getParent().toString(), reporterPath.getFileName().toString())) {
      DatabaseMigration databaseMigration = DatabaseMigration.newInstance();

      DatabaseModuleFactory factory = getDatabaseImportModuleFactory(moduleName);

      if (factory != null) {
        databaseMigration.importModule(factory);

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
          databaseMigration.importModuleParameter(entry.getKey(), entry.getValue());
        }

        databaseMigration.reporter(reporter);

        DatabaseImportModule importModule = databaseMigration.getImportModule();
        importModule.setOnceReporter(reporter);

        if (importModule instanceof JDBCImportModule) {
          jdbcImportModule = (JDBCImportModule) importModule;
          DatabaseStructure schemaInformation = jdbcImportModule.getSchemaInformation();

          ViewerDatabaseFromToolkit database = ToolkitStructure2ViewerStructure.getDatabase(schemaInformation);
          return database.getMetadata();

        }
      }
    } catch (IOException e) {
      throw new GenericException("Could not initialize conversion modules", e);
    } catch (ModuleException e) {
      throw new GenericException(e.getMessage());
    } finally {
      if (jdbcImportModule != null) {
        try {
          jdbcImportModule.closeConnection();
        } catch (ModuleException e) {
          new ClientLogger(SIARDController.class.getName())
              .error("JDBC SIARD Connection error - " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
      }
    }
    return null;
  }

  public static DBPTKModule getSIARDExportModule(String moduleName) throws GenericException {

    DBPTKModule dbptkModule = new DBPTKModule();
    PreservationParameter preservationParameter;

    final DatabaseModuleFactory factory = getDatabaseExportModuleFactory(moduleName);
    try {
      final Parameters exportModuleParameters = factory.getExportModuleParameters();
      getParameters(dbptkModule, factory.getModuleName(), exportModuleParameters);
    } catch (UnsupportedModuleException e) {
      throw new GenericException(e);
    }
    return dbptkModule;
  }

  public static DBPTKModule getSIARDExportModules() throws GenericException {
    DBPTKModule dbptkModule = new DBPTKModule();

    Set<DatabaseModuleFactory> databaseModuleFactories = ReflectionUtils.collectDatabaseModuleFactories();

    for (DatabaseModuleFactory factory : databaseModuleFactories) {
      if (factory.isEnabled()) {
        if (factory.producesExportModules()) {
          if (factory.getModuleName().startsWith("siard")) {
            final Parameters exportModuleParameters;
            try {
              exportModuleParameters = factory.getExportModuleParameters();
              getParameters(dbptkModule, factory.getModuleName(), exportModuleParameters);
            } catch (UnsupportedModuleException e) {
              throw new GenericException(e);
            }
          }
        }
      }
    }
    return dbptkModule;
  }

  public static DBPTKModule getDatabaseImportModules() throws GenericException {
    DBPTKModule dbptkModule = new DBPTKModule();
    PreservationParameter preservationParameter;

    Set<DatabaseModuleFactory> databaseModuleFactories = ReflectionUtils.collectDatabaseModuleFactories();

    for (DatabaseModuleFactory factory : databaseModuleFactories) {
      if (factory.isEnabled()) {
        if (factory.producesImportModules()) {

          final Parameters importModuleParameters;
          try {
            importModuleParameters = factory.getConnectionParameters();
            for (Parameter param : importModuleParameters.getParameters()) {
              preservationParameter = new PreservationParameter(param.longName(), param.description(),
                param.required(), param.hasArgument(), param.getInputType().name());
              dbptkModule.addPreservationParameter(factory.getModuleName(), preservationParameter);
            }

            for (ParameterGroup pg : importModuleParameters.getGroups()) {
              for (Parameter param : pg.getParameters()) {
                preservationParameter = new PreservationParameter(param.longName(), param.description(),
                  param.required(), param.hasArgument(), param.getInputType().name());
                dbptkModule.addPreservationParameter(factory.getModuleName(), preservationParameter);
              }
            }
          } catch (UnsupportedModuleException e) {
            throw new GenericException(e);
          }
        }
      }
    }

    return dbptkModule;
  }

  public static String loadMetadataFromLocal(String localPath) throws GenericException {
    String databaseUUID = SolrUtils.randomUUID();
    return loadMetadataFromLocal(localPath, databaseUUID);
  }

  public static String loadFromLocal(String localPath) throws GenericException {
    String databaseUUID = SolrUtils.randomUUID();
    return loadFromLocal(localPath, databaseUUID);
  }

  public static String loadMetadataFromLocal(String localPath, String databaseUUID) throws GenericException {
    Path basePath = Paths.get(ViewerConfiguration.getInstance().getViewerConfigurationAsString("/",
      ViewerConfiguration.PROPERTY_BASE_UPLOAD_PATH));
    Path siardPath = basePath.resolve(localPath);
    convertSIARDMetadatatoSolr(siardPath, databaseUUID);

    return databaseUUID;
  }

  public static String loadFromLocal(String localPath, String databaseUUID) throws GenericException {
    LOGGER.info("converting database {}", databaseUUID);
    Path basePath = Paths.get(ViewerConfiguration.getInstance().getViewerConfigurationAsString("/",
      ViewerConfiguration.PROPERTY_BASE_UPLOAD_PATH));
    try {
      Path siardPath = basePath.resolve(localPath);
      convertSIARDtoSolr(siardPath, databaseUUID);
      LOGGER.info("Conversion to SIARD successful, database: {}", databaseUUID);
    } catch (GenericException e) {
      LOGGER.error("Conversion to SIARD failed for database {}", databaseUUID, e);
      throw e;
    }
    return databaseUUID;
  }

  private static boolean convertSIARDMetadatatoSolr(Path siardPath, String databaseUUID) throws GenericException {
    LOGGER.info("starting to import metadata database " + siardPath.toAbsolutePath().toString());

    Path reporterPath = ViewerConfiguration.getInstance().getReportPath(databaseUUID).toAbsolutePath();
    try (Reporter reporter = new Reporter(reporterPath.getParent().toString(), reporterPath.getFileName().toString())) {
      SIARDEdition siardEdition = SIARDEdition.newInstance();
      String path = siardPath.toAbsolutePath().toString();

      siardEdition.editModule(new SIARDEditFactory()).editModuleParameter(SIARDEditFactory.PARAMETER_FILE,
        Collections.singletonList(path));

      siardEdition.reporter(reporter);

      final DatabaseStructure metadata = siardEdition.getMetadata();

      final DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();

      ViewerDatabase viewerDatabase = new ViewerDatabase();

      viewerDatabase.setStatus(ViewerDatabase.Status.METADATA_ONLY);
      viewerDatabase.setCurrentSchemaName("");
      viewerDatabase.setCurrentTableName("");

      viewerDatabase.setIngestedSchemas(0);
      viewerDatabase.setIngestedTables(0);
      viewerDatabase.setIngestedRows(0);

      viewerDatabase.setTotalSchemas(0);
      viewerDatabase.setTotalTables(0);
      viewerDatabase.setTotalRows(0);

      viewerDatabase.setUUID(databaseUUID);

      viewerDatabase.setSIARDPath(path);
      viewerDatabase.setSIARDSize(new File(path).length());

      final ViewerDatabaseFromToolkit database = ToolkitStructure2ViewerStructure.getDatabase(metadata);

      viewerDatabase.setMetadata(database.getMetadata());

      solrManager.addDatabase(viewerDatabase);

    } catch (IOException e) {
      throw new GenericException("Could not initialize conversion modules", e);
    } catch (ModuleException | RuntimeException e) {
      throw new GenericException("Could not convert the database to the Solr instance.", e);
    }

    return true;
  }

  private static boolean convertSIARDtoSolr(Path siardPath, String databaseUUID) throws GenericException {
    LOGGER.info("starting to convert database " + siardPath.toAbsolutePath().toString());

    // build the SIARD import module, Solr export module, and start the
    // conversion
    Path reporterPath = ViewerConfiguration.getInstance().getReportPath(databaseUUID).toAbsolutePath();
    try (Reporter reporter = new Reporter(reporterPath.getParent().toString(), reporterPath.getFileName().toString())) {
      ViewerConfiguration configuration = ViewerConfiguration.getInstance();

      DatabaseMigration databaseMigration = DatabaseMigration.newInstance();

      // XXX remove this workaround after fix of NPE
      databaseMigration.filterFactories(new ArrayList<>());

      databaseMigration.importModule(new SIARD2ModuleFactory())
        .importModuleParameter(SIARD2ModuleFactory.PARAMETER_FILE, siardPath.toAbsolutePath().toString());

      databaseMigration.exportModule(new DbvtkModuleFactory())
        .exportModuleParameter(DbvtkModuleFactory.PARAMETER_LOB_FOLDER, configuration.getLobPath().toString())
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

  private static DatabaseModuleFactory getDatabaseImportModuleFactory(String moduleName) {
    Set<DatabaseModuleFactory> databaseModuleFactories = ReflectionUtils.collectDatabaseModuleFactories();

    DatabaseModuleFactory factory = null;

    for (DatabaseModuleFactory dbFactory : databaseModuleFactories) {
      if (dbFactory.isEnabled() && dbFactory.producesImportModules()) {
        if (dbFactory.getModuleName().equals(moduleName))
          factory = dbFactory;
      }
    }

    return factory;
  }

  private static DatabaseModuleFactory getDatabaseExportModuleFactory(String moduleName) {
    Set<DatabaseModuleFactory> databaseModuleFactories = ReflectionUtils.collectDatabaseModuleFactories();

    DatabaseModuleFactory factory = null;

    for (DatabaseModuleFactory dbFactory : databaseModuleFactories) {
      if (dbFactory.isEnabled() && dbFactory.producesExportModules()) {
        if (dbFactory.getModuleName().equals(moduleName))
          factory = dbFactory;
      }
    }

    return factory;
  }

  private static void getParameters(DBPTKModule dbptkModule, String moduleName, Parameters parameters) {
    if (dbptkModule == null) dbptkModule = new DBPTKModule();
    PreservationParameter preservationParameter;

    for (Parameter param : parameters.getParameters()) {
      if (param.getExportOptions() != null) {
        preservationParameter = new PreservationParameter(param.longName(), param.description(),
            param.required(), param.hasArgument(), param.getInputType().name(),
            param.getExportOptions().name());
      } else {
        preservationParameter = new PreservationParameter(param.longName(), param.description(),
            param.required(), param.hasArgument(), param.getInputType().name());
      }

      dbptkModule.addPreservationParameter(moduleName, preservationParameter);
    }
  }
}
