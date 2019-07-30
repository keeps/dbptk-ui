package com.databasepreservation.main.common.server.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.databasepreservation.DatabaseMigration;
import com.databasepreservation.SIARDEdition;
import com.databasepreservation.main.common.server.ProgressObserver;
import com.databasepreservation.main.common.server.SIARDProgressObserver;
import com.databasepreservation.main.common.server.ViewerConfiguration;
import com.databasepreservation.main.common.server.ViewerFactory;
import com.databasepreservation.main.common.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.main.common.server.index.utils.SolrUtils;
import com.databasepreservation.main.common.server.transformers.ToolkitStructure2ViewerStructure;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerColumn;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabaseFromToolkit;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerMetadata;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSIARDBundle;
import com.databasepreservation.main.common.shared.client.ClientLogger;
import com.databasepreservation.main.common.shared.client.tools.PathUtils;
import com.databasepreservation.main.desktop.shared.models.DBPTKModule;
import com.databasepreservation.main.desktop.shared.models.ExternalLobDBPTK;
import com.databasepreservation.main.desktop.shared.models.PreservationParameter;
import com.databasepreservation.main.desktop.shared.models.SSHConfiguration;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.ConnectionParameters;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.CustomViewsParameter;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.CustomViewsParameters;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.ExportOptionsParameters;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.ExternalLOBsParameter;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.MetadataExportOptionsParameters;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.TableAndColumnsParameters;
import com.databasepreservation.main.modules.viewer.DbvtkModuleFactory;
import com.databasepreservation.model.Reporter;
import com.databasepreservation.model.exception.ModuleException;
import com.databasepreservation.model.exception.UnsupportedModuleException;
import com.databasepreservation.model.modules.DatabaseImportModule;
import com.databasepreservation.model.modules.DatabaseModuleFactory;
import com.databasepreservation.model.modules.filters.DatabaseFilterFactory;
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

  public static boolean testConnection(String databaseUUID, ConnectionParameters parameters)
    throws GenericException {
    JDBCImportModule jdbcImportModule;

    Path reporterPath = ViewerConfiguration.getInstance().getReportPath(databaseUUID).toAbsolutePath();
    try (Reporter reporter = new Reporter(reporterPath.getParent().toString(), reporterPath.getFileName().toString())) {
      DatabaseMigration databaseMigration = DatabaseMigration.newInstance();

      DatabaseModuleFactory factory = getDatabaseImportModuleFactory(parameters.getModuleName());

      if (factory != null) {
        databaseMigration.importModule(factory);

        setupSSHConfiguration(databaseMigration, parameters);

        try {
          setupPathToDriver(parameters);
        } catch (Exception e) {
          throw new GenericException("Could not load the driver", e);
        }

        for (Map.Entry<String, String> entry : parameters.getJDBCConnectionParameters().getConnection().entrySet()) {
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

  public static boolean migrateToSIARD(String databaseUUID, String siard,
    TableAndColumnsParameters tableAndColumnsParameters, ExportOptionsParameters exportOptionsParameters,
    MetadataExportOptionsParameters metadataExportOptionsParameters) throws GenericException {
    File f = new File(siard);
    if (f.exists() && !f.isDirectory()) {
      LOGGER.info("starting to convert database");
      Path reporterPath = ViewerConfiguration.getInstance().getReportPath(databaseUUID).toAbsolutePath();
      try (
        Reporter reporter = new Reporter(reporterPath.getParent().toString(), reporterPath.getFileName().toString())) {
        DatabaseMigration databaseMigration = DatabaseMigration.newInstance();

        databaseMigration.filterFactories(new ArrayList<>());

        // BUILD Import Module
        DatabaseModuleFactory importModuleFactory = getDatabaseImportModuleFactory("siard-2");

        databaseMigration.importModule(importModuleFactory);
        databaseMigration.importModuleParameter("file", siard);

        // BUILD Export Module
        final DatabaseModuleFactory databaseExportModuleFactory = getDatabaseExportModuleFactory(
          exportOptionsParameters.getSIARDVersion());

        databaseMigration.exportModule(databaseExportModuleFactory);

        for (Map.Entry<String, String> entry : exportOptionsParameters.getParameters().entrySet()) {
          if (!entry.getValue().equals("false")) {
            LOGGER.info("Export Options - " + entry.getKey() + "->" + entry.getValue());
            databaseMigration.exportModuleParameter(entry.getKey(), entry.getValue());
          }
        }
        for (Map.Entry<String, String> entry : metadataExportOptionsParameters.getValues().entrySet()) {
          LOGGER.info("Metadata Export Options - " + entry.getKey() + "->" + entry.getValue());
          databaseMigration.exportModuleParameter(entry.getKey(), entry.getValue());
        }
        try {
          final String pathToTableFilter = constructTableFilter(tableAndColumnsParameters);
          LOGGER.info("Path to table-filter: " + pathToTableFilter);
          databaseMigration.exportModuleParameter("table-filter", pathToTableFilter);
        } catch (GenericException e) {
          throw new GenericException(e);
        }

        databaseMigration.filter(new ObservableFilter(new SIARDProgressObserver(databaseUUID)));

        databaseMigration.reporter(reporter);

        long startTime = System.currentTimeMillis();

        databaseMigration.migrate();

        long duration = System.currentTimeMillis() - startTime;
        LOGGER.info("Conversion time {}m {}s", duration / 60000, duration % 60000 / 1000);
        return true;
      } catch (IOException e) {
        throw new GenericException("Could not initialize conversion modules", e);
      } catch (ModuleException | RuntimeException e) {
        LOGGER.info("" + e.getCause());
        throw new GenericException("Could not convert the database", e);
      }
    }
    return false;
  }

  public static boolean migrateToDBMS(String databaseUUID, String siard, ConnectionParameters connectionParameters)
    throws GenericException {
    File f = new File(siard);
    if (f.exists() && !f.isDirectory()) {
      LOGGER.info("starting to convert database");
      Path reporterPath = ViewerConfiguration.getInstance().getReportPath(databaseUUID).toAbsolutePath();
      try (
        Reporter reporter = new Reporter(reporterPath.getParent().toString(), reporterPath.getFileName().toString())) {
        DatabaseMigration databaseMigration = DatabaseMigration.newInstance();

        databaseMigration.filterFactories(new ArrayList<>());

        // BUILD Import Module
        DatabaseModuleFactory importModuleFactory = getDatabaseImportModuleFactory("siard-2");

        databaseMigration.importModule(importModuleFactory);
        databaseMigration.importModuleParameter("file", siard);

        setupSSHConfiguration(databaseMigration, connectionParameters);

        // BUILD Export Module
        final DatabaseModuleFactory exportModuleFactory = getDatabaseExportModuleFactory(
          connectionParameters.getModuleName());
        for (Map.Entry<String, String> entry : connectionParameters.getJDBCConnectionParameters().getConnection()
          .entrySet()) {
          LOGGER.info("Connection Options - " + entry.getKey() + "->" + entry.getValue());
          databaseMigration.exportModuleParameter(entry.getKey(), entry.getValue());
        }
        databaseMigration.exportModule(exportModuleFactory);

        databaseMigration.filter(new ObservableFilter(new SIARDProgressObserver(databaseUUID)));

        databaseMigration.reporter(reporter);

        long startTime = System.currentTimeMillis();

        databaseMigration.migrate();

        long duration = System.currentTimeMillis() - startTime;
        LOGGER.info("Conversion time {}m {}s", duration / 60000, duration % 60000 / 1000);
        return true;
      } catch (IOException e) {
        throw new GenericException("Could not initialize conversion modules", e);
      } catch (ModuleException | RuntimeException e) {
        throw new GenericException(e.getMessage(), e);
      }
    }

    return false;
  }

  public static boolean createSIARD(String UUID, ConnectionParameters connectionParameters,
    TableAndColumnsParameters tableAndColumnsParameters, CustomViewsParameters customViewsParameters,
    ExportOptionsParameters exportOptionsParameters, MetadataExportOptionsParameters metadataExportOptionsParameters)
    throws GenericException {
    final String pathToTableFilter = constructTableFilter(tableAndColumnsParameters);
    final String siardName = PathUtils.getFileName(exportOptionsParameters.getSiardPath());

    LOGGER.info("starting to convert database " + exportOptionsParameters.getSiardPath());

    Path reporterPath = ViewerConfiguration.getInstance().getReportPath(siardName).toAbsolutePath();
    try (Reporter reporter = new Reporter(reporterPath.getParent().toString(), reporterPath.getFileName().toString())) {
      DatabaseMigration databaseMigration = DatabaseMigration.newInstance();

      if (tableAndColumnsParameters.getExternalLOBsParameters().isEmpty()) {
        databaseMigration.filterFactories(new ArrayList<>());
      } else {
        final List<DatabaseFilterFactory> databaseFilterFactories = ReflectionUtils.collectDatabaseFilterFactory();
        databaseMigration.filterFactories(databaseFilterFactories);
      }

      // BUILD Import Module
      final DatabaseModuleFactory databaseImportModuleFactory = getDatabaseImportModuleFactory(
        connectionParameters.getModuleName());

      databaseMigration.importModule(databaseImportModuleFactory);

      setupSSHConfiguration(databaseMigration, connectionParameters);

      try {
        setupPathToDriver(connectionParameters);
      } catch (Exception e) {
        throw new GenericException("Could not load the driver", e);
      }

      for (Map.Entry<String, String> entry : connectionParameters.getJDBCConnectionParameters().getConnection()
        .entrySet()) {
        LOGGER.info("Connection Options - " + entry.getKey() + "->" + entry.getValue());
        databaseMigration.importModuleParameter(entry.getKey(), entry.getValue());
      }

      if (!customViewsParameters.getCustomViewsParameter().isEmpty()) {
        final String pathToCustomViews = constructCustomViews(customViewsParameters);
        LOGGER.info("Custom view path - " + pathToCustomViews);
        databaseMigration.importModuleParameter("custom-views", pathToCustomViews);
      }

      // BUILD Export Module
      final DatabaseModuleFactory databaseExportModuleFactory = getDatabaseExportModuleFactory(
        exportOptionsParameters.getSIARDVersion());

      databaseMigration.exportModule(databaseExportModuleFactory);

      for (Map.Entry<String, String> entry : exportOptionsParameters.getParameters().entrySet()) {
        if (!entry.getValue().equals("false")) {
          LOGGER.info("Export Options - " + entry.getKey() + "->" + entry.getValue());
          databaseMigration.exportModuleParameter(entry.getKey(), entry.getValue());
        }
      }
      for (Map.Entry<String, String> entry : metadataExportOptionsParameters.getValues().entrySet()) {
        LOGGER.info("Metadata Export Options - " + entry.getKey() + "->" + entry.getValue());
        databaseMigration.exportModuleParameter(entry.getKey(), entry.getValue());
      }

      LOGGER.info("Path to table-filter: " + pathToTableFilter);

      databaseMigration.exportModuleParameter("table-filter", pathToTableFilter);

      // External Lobs
      if (!tableAndColumnsParameters.getExternalLOBsParameters().isEmpty()) {
        final ArrayList<ExternalLobDBPTK> externalLobDBPTKS = constructExternalLobFilter(tableAndColumnsParameters);
        int index = 0;
        for (ExternalLobDBPTK parameter : externalLobDBPTKS) {
          LOGGER.info("column-list: " + parameter.getPathToColumnList());
          LOGGER.info("base-path: " + parameter.getBasePath());
          LOGGER.info("reference-type: " + parameter.getReferenceType());
          databaseMigration.filterParameter("base-path", parameter.getBasePath(), index);
          databaseMigration.filterParameter("column-list", parameter.getPathToColumnList(), index);
          databaseMigration.filterParameter("reference-type", parameter.getReferenceType(), index);
          index++;
        }
      }

      databaseMigration.filter(new ObservableFilter(new SIARDProgressObserver(UUID)));

      databaseMigration.reporter(reporter);

      long startTime = System.currentTimeMillis();

      databaseMigration.migrate();

      long duration = System.currentTimeMillis() - startTime;
      LOGGER.info("Conversion time {}m {}s", duration / 60000, duration % 60000 / 1000);
      return true;
    } catch (IOException e) {
      throw new GenericException("Could not initialize conversion modules", e);
    } catch (ModuleException | RuntimeException e) {
      LOGGER.info("" + e.getCause());
      throw new GenericException("Could not convert the database", e);
    }
  }

  public static ViewerMetadata getDatabaseMetadata(String databaseUUID, ConnectionParameters parameters)
    throws GenericException {
    JDBCImportModule jdbcImportModule = null;

    Path reporterPath = ViewerConfiguration.getInstance().getReportPath(databaseUUID).toAbsolutePath();
    try (Reporter reporter = new Reporter(reporterPath.getParent().toString(), reporterPath.getFileName().toString())) {
      DatabaseMigration databaseMigration = DatabaseMigration.newInstance();

      DatabaseModuleFactory factory = getDatabaseImportModuleFactory(parameters.getModuleName());

      if (factory != null) {
        databaseMigration.importModule(factory);

        setupSSHConfiguration(databaseMigration, parameters);

        try {
          setupPathToDriver(parameters);
        } catch (Exception e) {
          throw new GenericException("Could not load the driver", e);
        }

        for (Map.Entry<String, String> entry : parameters.getJDBCConnectionParameters().getConnection().entrySet()) {
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

  public static DBPTKModule getDatabaseExportModules() throws GenericException {
    DBPTKModule dbptkModule = new DBPTKModule();
    Set<DatabaseModuleFactory> databaseModuleFactories = ReflectionUtils.collectDatabaseModuleFactories();
    for (DatabaseModuleFactory factory : databaseModuleFactories) {
      if (!factory.getModuleName().equals("list-tables")) {
        if (factory.isEnabled() && factory.producesExportModules()) {
          getDatabaseModulesParameters(factory, dbptkModule);
        }
      }
    }
    return dbptkModule;
  }

  public static DBPTKModule getDatabaseImportModules() throws GenericException {
    DBPTKModule dbptkModule = new DBPTKModule();
    Set<DatabaseModuleFactory> databaseModuleFactories = ReflectionUtils.collectDatabaseModuleFactories();
    for (DatabaseModuleFactory factory : databaseModuleFactories) {
      if (factory.isEnabled()) {
        if (factory.producesImportModules()) {
          getDatabaseModulesParameters(factory, dbptkModule);
        }
      }
    }
    return dbptkModule;
  }

  private static void getDatabaseModulesParameters(DatabaseModuleFactory factory, DBPTKModule dbptkModule)
    throws GenericException {
    PreservationParameter preservationParameter;
    final Parameters parameters;
    try {
      parameters = factory.getConnectionParameters();
      for (Parameter param : parameters.getParameters()) {
        preservationParameter = new PreservationParameter(param.longName(), param.description(), param.required(),
          param.hasArgument(), param.getInputType().name());
        if (param.valueIfNotSet() != null) {
          preservationParameter.setDefaultValue(param.valueIfNotSet());
        }
        dbptkModule.addPreservationParameter(factory.getModuleName(), preservationParameter);
      }

      for (ParameterGroup pg : parameters.getGroups()) {
        for (Parameter param : pg.getParameters()) {
          preservationParameter = new PreservationParameter(param.longName(), param.description(), param.required(),
            param.hasArgument(), param.getInputType().name());
          dbptkModule.addPreservationParameter(factory.getModuleName(), preservationParameter);
        }
      }
    } catch (UnsupportedModuleException e) {
      throw new GenericException(e);
    }
  }

  public static String loadFromLocal(String localPath) throws GenericException {
    String databaseUUID = SolrUtils.randomUUID();
    return loadFromLocal(localPath, databaseUUID);
  }

  public static String loadMetadataFromLocal(String databaseUUID, String localPath) throws GenericException {
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
      viewerDatabase.setValidationStatus(ViewerDatabase.ValidationStatus.NOT_VALIDATED);

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
      throw new GenericException("Could not convert the database.", e);
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

  private static ArrayList<ExternalLobDBPTK> constructExternalLobFilter(TableAndColumnsParameters parameters) throws GenericException {
    final ArrayList<ExternalLOBsParameter> externalLOBsParameters = parameters.getExternalLOBsParameters();
    ArrayList<ExternalLobDBPTK> externalLobDBPTKParameters = new ArrayList<>();
    ExternalLobDBPTK externalLobDBPTK = new ExternalLobDBPTK();
    for (ExternalLOBsParameter parameter : externalLOBsParameters) {
      externalLobDBPTK.setBasePath(parameter.getBasePath());
      externalLobDBPTK.setReferenceType(parameter.getReferenceType());
      StringBuilder sb = new StringBuilder();
      sb.append(parameter.getTable().getSchemaName()).append(".").append(parameter.getTable().getName()).append("{")
        .append(parameter.getColumnName()).append(";").append("}");

      FileOutputStream outputStream = null;

      try {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        final File tmpFile = File.createTempFile(SolrUtils.randomUUID(), ".txt", tmpDir);
        externalLobDBPTK.setPathToColumnList(Paths.get(tmpFile.toURI()).normalize().toAbsolutePath().toString());
        outputStream = new FileOutputStream(tmpFile);
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);
        writer.write(sb.toString());
        writer.flush();
        writer.close();
        outputStream.close();

        externalLobDBPTKParameters.add(externalLobDBPTK);
      } catch (IOException e) {
        throw new GenericException("Could not create table-filter temporary file", e);
      } finally {
        try {
          if (outputStream != null) {
            outputStream.close();
          }
        } catch (IOException e) {
          throw new GenericException("Could not close the table-filter temporary file", e);
        }
      }
    }

    return externalLobDBPTKParameters;
  }

  private static String constructTableFilter(TableAndColumnsParameters parameters) throws GenericException {
    final HashMap<String, ArrayList<ViewerColumn>> columns = parameters.getColumns();
    StringBuilder tf = new StringBuilder();

    for (Map.Entry<String, ArrayList<ViewerColumn>> entry : columns.entrySet()) {
      if (!entry.getValue().isEmpty()) {
        tf.append(entry.getKey()).append("{");
        for (ViewerColumn column : entry.getValue()) {
          tf.append(column.getDisplayName()).append(";");
        }
        tf.append("}").append("\n");
      }
    }

    FileOutputStream outputStream = null;

    try {
      File tmpDir = new File(System.getProperty("java.io.tmpdir"));
      final File tmpFile = File.createTempFile(SolrUtils.randomUUID(), ".txt", tmpDir);
      String path = Paths.get(tmpFile.toURI()).normalize().toAbsolutePath().toString();
      outputStream = new FileOutputStream(tmpFile);
      OutputStreamWriter writer = new OutputStreamWriter(outputStream);
      writer.write(tf.toString());
      writer.flush();
      writer.close();
      outputStream.close();

      return path;
    } catch (IOException e) {
      throw new GenericException("Could not create table-filter temporary file", e);
    } finally {
      try {
        if (outputStream != null) {
          outputStream.close();
        }
      } catch (IOException e) {
        throw new GenericException("Could not close the table-filter temporary file", e);
      }
    }
  }

  private static String constructCustomViews(CustomViewsParameters customViewsParameters) throws GenericException {
    final ArrayList<CustomViewsParameter> customViewParameters = customViewsParameters.getCustomViewsParameter();
    Map<String, Object> data = new HashMap<>();

    Map<String, Object> view = new HashMap<>();

    for (CustomViewsParameter parameter : customViewParameters) {
      Map<String, Object> customViewInformation = new HashMap<>();
      customViewInformation.put("query", parameter.getCustomViewQuery());
      customViewInformation.put("description", parameter.getCustomViewDescription());

      view.put(parameter.getCustomViewName(), customViewInformation);
      data.put(parameter.getSchema(), view);
    }

    Yaml yaml = new Yaml();

    FileOutputStream outputStream = null;

    try {
      File tmpDir = new File(System.getProperty("java.io.tmpdir"));
      final File tmpFile = File.createTempFile("custom_view_" + SolrUtils.randomUUID(), ".yaml", tmpDir);
      String path = Paths.get(tmpFile.toURI()).normalize().toAbsolutePath().toString();
      outputStream = new FileOutputStream(tmpFile);
      OutputStreamWriter writer = new OutputStreamWriter(outputStream);
      yaml.dump(data, writer);
      writer.close();
      outputStream.close();

      return path;
    } catch (IOException e) {
      throw new GenericException("Could not create custom views temporary file", e);
    } finally {
      try {
        if (outputStream != null) {
          outputStream.close();
        }
      } catch (IOException e) {
        throw new GenericException("Could not close the custom views temporary file", e);
      }
    }

  }

  public static ViewerMetadata updateMetadataInformation(ViewerMetadata metadata, ViewerSIARDBundle bundleSiard,
    String databaseUUID, String siardPath) throws GenericException {

    Path reporterPath = ViewerConfiguration.getInstance().getReportPath(databaseUUID).toAbsolutePath();
    try (Reporter reporter = new Reporter(reporterPath.getParent().toString(), reporterPath.getFileName().toString())) {

      if (new File(siardPath).isFile()) {
        LOGGER.info("Updating  " + siardPath);
        SIARDEdition siardEdition = SIARDEdition.newInstance();

        siardEdition.editModule(new SIARDEditFactory())
          .editModuleParameter(SIARDEditFactory.PARAMETER_FILE, Collections.singletonList(siardPath))
          .editModuleParameter(SIARDEditFactory.PARAMETER_SET, bundleSiard.getCommandList());

        siardEdition.reporter(reporter);
        siardEdition.edit();
      }
      bundleSiard.clearCommandList();

      final DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();
      solrManager.updateDatabaseMetadata(databaseUUID, metadata);

    } catch (IOException e) {
      throw new GenericException("Could not initialize conversion modules.", e);
    } catch (ModuleException | RuntimeException e) {
      throw new GenericException("Could not convert the database to the Solr instance.", e);
    }

    return metadata;
  }

  /**
   * For Java 8 or below: check
   * http://robertmaldon.blogspot.com/2007/11/dynamically-add-to-eclipse-junit.html
   * (last access: 22-07-2019)
   * 
   * @param url
   * @throws Exception
   */
  private static void addURL(URL url) throws Exception {
    URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    Class<URLClassLoader> clazz = URLClassLoader.class;

    // Use reflection
    Method method = clazz.getDeclaredMethod("addURL", URL.class);
    method.setAccessible(true);
    method.invoke(classLoader, url);
  }

  private static void setupSSHConfiguration(DatabaseMigration databaseMigration, ConnectionParameters parameters) {
    if (parameters.doSSH()) {
      final SSHConfiguration sshConfiguration = parameters.getSSHConfiguration();

      databaseMigration.importModuleParameter("ssh", "true");
      databaseMigration.importModuleParameter("ssh-host", sshConfiguration.getHostname());
      databaseMigration.importModuleParameter("ssh-user", sshConfiguration.getUsername());
      databaseMigration.importModuleParameter("ssh-password", sshConfiguration.getPassword());
      databaseMigration.importModuleParameter("ssh-port", sshConfiguration.getPort());
    }
  }

  private static void setupPathToDriver(ConnectionParameters parameters) throws Exception {
    if (parameters.getJDBCConnectionParameters().isDriver()) {
      addURL(new File(parameters.getJDBCConnectionParameters().getDriverPath()).toURI().toURL());
    }
  }
}
