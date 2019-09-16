package com.databasepreservation.main.common.server.controller;

import java.io.File;
import java.io.FileNotFoundException;
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
import org.joda.time.DateTime;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.databasepreservation.DatabaseMigration;
import com.databasepreservation.SIARDEdition;
import com.databasepreservation.SIARDValidation;
import com.databasepreservation.main.common.server.ProgressObserver;
import com.databasepreservation.main.common.server.SIARDProgressObserver;
import com.databasepreservation.main.common.server.ValidationProgressObserver;
import com.databasepreservation.main.common.server.ViewerConfiguration;
import com.databasepreservation.main.common.server.ViewerFactory;
import com.databasepreservation.main.common.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.main.common.server.index.utils.SolrUtils;
import com.databasepreservation.main.common.server.transformers.ToolkitStructure2ViewerStructure;
import com.databasepreservation.main.common.shared.ViewerConstants;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerColumn;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabaseFromToolkit;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerMetadata;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSIARDBundle;
import com.databasepreservation.main.common.shared.exceptions.ViewerException;
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
import com.databasepreservation.modules.siard.SIARDValidateFactory;
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

  public static List<List<String>> validateCustomViewQuery(String databaseUUID, ConnectionParameters parameters,
    String query) throws GenericException {
    Reporter reporter = getReporter(databaseUUID);
    List<List<String>> results = new ArrayList<>();
    final DatabaseMigration databaseMigration = initializeDatabaseMigration(reporter);

    setupJDBCConnection(databaseMigration, parameters);

    try {
      DatabaseImportModule importModule = databaseMigration.getImportModule();
      importModule.setOnceReporter(reporter);
      if (importModule instanceof JDBCImportModule) {
        JDBCImportModule jdbcImportModule = (JDBCImportModule) importModule;
        try {
          results = jdbcImportModule.testCustomViewQuery(query);
        } catch (ModuleException e) {
          throw new GenericException(e.getMessage());
        }
      }
    } catch (ModuleException e) {
      throw new GenericException(e.getMessage());
    }

    return results;
  }

  public static boolean testConnection(String databaseUUID, ConnectionParameters parameters) throws GenericException {
    Reporter reporter = getReporter(databaseUUID);
    final DatabaseMigration databaseMigration = initializeDatabaseMigration(reporter);

    setupJDBCConnection(databaseMigration, parameters);

    try {
      DatabaseImportModule importModule = databaseMigration.getImportModule();
      importModule.setOnceReporter(reporter);
      if (importModule instanceof JDBCImportModule) {
        JDBCImportModule jdbcImportModule = (JDBCImportModule) importModule;
        return jdbcImportModule.testConnection();
      }
    } catch (ModuleException e) {
      throw new GenericException(e.getMessage());
    }

    return false;
  }

  public static boolean migrateToSIARD(String databaseUUID, String siard,
    TableAndColumnsParameters tableAndColumnsParameters, ExportOptionsParameters exportOptionsParameters,
    MetadataExportOptionsParameters metadataExportOptionsParameters) throws GenericException {
    Reporter reporter = getReporter(databaseUUID);
    File f = new File(siard);
    if (f.exists() && !f.isDirectory()) {
      LOGGER.info("starting to convert database");
      final DatabaseMigration databaseMigration = initializeDatabaseMigration(reporter);

      databaseMigration.filterFactories(new ArrayList<>());

      // BUILD Import Module
      databaseMigration.importModule(new SIARD2ModuleFactory());
      databaseMigration.importModuleParameter(SIARD2ModuleFactory.PARAMETER_FILE, siard);

      // BUILD Export Module
      setupSIARDExportModule(databaseMigration, tableAndColumnsParameters, exportOptionsParameters,
        metadataExportOptionsParameters);

      databaseMigration.filter(new ObservableFilter(new SIARDProgressObserver(databaseUUID)));

      long startTime = System.currentTimeMillis();
      try {
        databaseMigration.migrate();
      } catch (ModuleException | RuntimeException e) {
        LOGGER.info("" + e.getCause());
        throw new GenericException("Could not convert the database", e);
      }
      long duration = System.currentTimeMillis() - startTime;
      LOGGER.info("Conversion time {}m {}s", duration / 60000, duration % 60000 / 1000);
      return true;
    } else {
      throw new GenericException("SIARD file not found");
    }
  }

  public static boolean migrateToDBMS(String databaseUUID, String siard, ConnectionParameters connectionParameters)
    throws GenericException {
    Reporter reporter = getReporter(databaseUUID);
    File f = new File(siard);
    if (f.exists() && !f.isDirectory()) {
      LOGGER.info("starting to convert database");
      final DatabaseMigration databaseMigration = initializeDatabaseMigration(reporter);

      databaseMigration.filterFactories(new ArrayList<>());

      // BUILD Import Module
      databaseMigration.importModule(new SIARD2ModuleFactory());
      databaseMigration.importModuleParameter(SIARD2ModuleFactory.PARAMETER_FILE, siard);

      // BUILD Export Module
      final DatabaseModuleFactory exportModuleFactory = getDatabaseExportModuleFactory(
        connectionParameters.getModuleName());
      setupExportModuleSSHConfiguration(databaseMigration, connectionParameters);
      for (Map.Entry<String, String> entry : connectionParameters.getJDBCConnectionParameters().getConnection()
        .entrySet()) {
        LOGGER.info("Connection Options - " + entry.getKey() + "->" + entry.getValue());
        databaseMigration.exportModuleParameter(entry.getKey(), entry.getValue());
      }
      databaseMigration.exportModule(exportModuleFactory);

      // Progress Observer
      databaseMigration.filter(new ObservableFilter(new SIARDProgressObserver(databaseUUID)));

      long startTime = System.currentTimeMillis();
      try {
        databaseMigration.migrate();
      } catch (ModuleException | RuntimeException e) {
        throw new GenericException(e.getMessage(), e);
      }
      long duration = System.currentTimeMillis() - startTime;
      LOGGER.info("Conversion time {}m {}s", duration / 60000, duration % 60000 / 1000);
      return true;
    } else {
      throw new GenericException("SIARD file missing");
    }
  }

  public static boolean createSIARD(String databaseUUID, ConnectionParameters connectionParameters,
    TableAndColumnsParameters tableAndColumnsParameters, CustomViewsParameters customViewsParameters,
    ExportOptionsParameters exportOptionsParameters, MetadataExportOptionsParameters metadataExportOptionsParameters)
    throws GenericException {
    Reporter reporter = getReporter(databaseUUID);
    LOGGER.info("starting to convert database " + exportOptionsParameters.getSiardPath());

    final DatabaseMigration databaseMigration = initializeDatabaseMigration(reporter);

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
    setupImportModuleSSHConfiguration(databaseMigration, connectionParameters);

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
    setupSIARDExportModule(databaseMigration, tableAndColumnsParameters, exportOptionsParameters,
      metadataExportOptionsParameters);

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

    databaseMigration.filter(new ObservableFilter(new SIARDProgressObserver(databaseUUID)));

    long startTime = System.currentTimeMillis();
    try {
      databaseMigration.migrate();
    } catch (RuntimeException e) {
      throw new GenericException("Could not convert the database", e);
    } catch (ModuleException e) {
      throw new GenericException(e.getMessage(), e);
    }
    long duration = System.currentTimeMillis() - startTime;
    LOGGER.info("Conversion time {}m {}s", duration / 60000, duration % 60000 / 1000);
    return true;
  }

  public static ViewerMetadata getDatabaseMetadata(String databaseUUID, ConnectionParameters parameters)
    throws GenericException {
    final Reporter reporter = getReporter(databaseUUID);
    final DatabaseMigration databaseMigration = initializeDatabaseMigration(reporter);
    setupJDBCConnection(databaseMigration, parameters);

    DatabaseImportModule importModule;
    DatabaseStructure schemaInformation = null;
    try {
      importModule = databaseMigration.getImportModule();
      importModule.setOnceReporter(reporter);

      if (importModule instanceof JDBCImportModule) {
        JDBCImportModule jdbcImportModule = (JDBCImportModule) importModule;
        schemaInformation = jdbcImportModule.getSchemaInformation();
        jdbcImportModule.closeConnection();
      }
    } catch (ModuleException e) {
      throw new GenericException(e.getMessage());
    }
    ViewerDatabaseFromToolkit database = null;
    try {
      database = ToolkitStructure2ViewerStructure.getDatabase(schemaInformation);
    } catch (ViewerException e) {
      LOGGER.debug(e.getMessage());
    }

    if (database != null)
      return database.getMetadata();

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

  public static String loadMetadataFromLocal(String databaseUUID, String localPath) throws GenericException {
    Path basePath = Paths.get(ViewerConfiguration.getInstance().getViewerConfigurationAsString("/",
      ViewerConfiguration.PROPERTY_BASE_UPLOAD_PATH));
    Path siardPath = basePath.resolve(localPath);
    convertSIARDMetadataToSolr(siardPath, databaseUUID);

    return databaseUUID;
  }

  private static void convertSIARDMetadataToSolr(Path siardPath, String databaseUUID) throws GenericException {
    LOGGER.info("starting to import metadata database " + siardPath.toAbsolutePath().toString());
    if (Files.notExists(siardPath)) {
      throw new GenericException("File not found at path: " + siardPath);
    }

    Path reporterPath = ViewerConfiguration.getInstance().getReportPath(databaseUUID).toAbsolutePath();
    try (Reporter reporter = new Reporter(reporterPath.getParent().toString(), reporterPath.getFileName().toString())) {
      SIARDEdition siardEdition = SIARDEdition.newInstance();

      siardEdition.editModule(new SIARDEditFactory()).editModuleParameter(SIARDEditFactory.PARAMETER_FILE,
        Collections.singletonList(siardPath.toAbsolutePath().toString()));

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

      viewerDatabase.setSIARDPath(siardPath.toAbsolutePath().toString());
      viewerDatabase.setSIARDSize(siardPath.toFile().length());
      viewerDatabase.setValidationStatus(ViewerDatabase.ValidationStatus.NOT_VALIDATED);

      final ViewerDatabaseFromToolkit database = ToolkitStructure2ViewerStructure.getDatabase(metadata);

      viewerDatabase.setMetadata(database.getMetadata());

      solrManager.addDatabase(viewerDatabase);

    } catch (IOException e) {
      throw new GenericException("Could not initialize conversion modules", e);
    } catch (ViewerException e){
      throw new GenericException(e.getMessage(), e);
    } catch (ModuleException | RuntimeException e) {
      throw new GenericException("Could not convert the database to the Solr instance.", e);
    }
  }

  public static String loadFromLocal(String localPath) throws GenericException {
    String databaseUUID = SolrUtils.randomUUID();
    return loadFromLocal(localPath, databaseUUID);
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

  private static void convertSIARDtoSolr(Path siardPath, String databaseUUID) throws GenericException {
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
    } catch (IOException e) {
      throw new GenericException("Could not initialize conversion modules", e);
    } catch (ModuleException | RuntimeException e) {
      throw new GenericException("Could not convert the database.", e);
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

  public static boolean validateSIARD(String databaseUUID, String SIARDPath, String validationReportPath, String allowedTypesPath) throws GenericException {
    Path reporterPath = ViewerConfiguration.getInstance().getReportPath(databaseUUID).toAbsolutePath();
    boolean valid;
    try (Reporter reporter = new Reporter(reporterPath.getParent().toString(), reporterPath.getFileName().toString())) {
      SIARDValidation siardValidation = SIARDValidation.newInstance();
      siardValidation.validateModule(new SIARDValidateFactory())
          .validateModuleParameter(SIARDValidateFactory.PARAMETER_FILE, SIARDPath)
          .validateModuleParameter(SIARDValidateFactory.PARAMETER_ALLOWED, allowedTypesPath)
          .validateModuleParameter(SIARDValidateFactory.PARAMETER_REPORT, validationReportPath);

      siardValidation.reporter(reporter);
      siardValidation.observer(new ValidationProgressObserver(databaseUUID));
      try {
        valid = siardValidation.validate();

        ViewerDatabase.ValidationStatus status;

        if (valid) {
          status = ViewerDatabase.ValidationStatus.VALIDATION_SUCCESS;
        } else {
          status = ViewerDatabase.ValidationStatus.VALIDATION_FAILED;
        }

        String dbptkVersion = "";

        try {
          dbptkVersion = ViewerConfiguration.getInstance().getDBPTKVersion();
          final DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();
          solrManager.updateSIARDValidationInformation(databaseUUID, status, validationReportPath, dbptkVersion,
            new DateTime().toString());
        } catch (IOException e) {
          throw new GenericException("Failed to obtain the DBPTK version from properties", e);
        }
      } catch (ModuleException e) {
        throw new GenericException(e);
      }
    } catch (IOException e) {
      throw new GenericException("Could not initialize the validate module.", e);
    }

    return valid;
  }

  /****************************************************************************
   * Private auxiliary Methods
   ****************************************************************************/
  private static Reporter getReporter(final String databaseUUID) {
    Path reporterPath = ViewerConfiguration.getInstance().getReportPath(databaseUUID).toAbsolutePath();
    return new Reporter(reporterPath.getParent().toString(), reporterPath.getFileName().toString());
  }

  private static DatabaseMigration initializeDatabaseMigration(final Reporter reporter) {
    final DatabaseMigration databaseMigration = DatabaseMigration.newInstance();
    databaseMigration.reporter(reporter);
    return databaseMigration;
  }

  private static void setupJDBCConnection(final DatabaseMigration databaseMigration,
    final ConnectionParameters parameters) throws GenericException {
    DatabaseModuleFactory factory = getDatabaseImportModuleFactory(parameters.getModuleName());

    if (factory != null) {
      databaseMigration.importModule(factory);
      setupImportModuleSSHConfiguration(databaseMigration, parameters);
      try {
        setupPathToDriver(parameters);
      } catch (Exception e) {
        throw new GenericException("Could not load the driver", e);
      }

      for (Map.Entry<String, String> entry : parameters.getJDBCConnectionParameters().getConnection().entrySet()) {
        databaseMigration.importModuleParameter(entry.getKey(), entry.getValue());
      }
    }
  }

  private static void setupSIARDExportModule(DatabaseMigration databaseMigration,
    TableAndColumnsParameters tableAndColumnsParameters, ExportOptionsParameters exportOptionsParameters,
    MetadataExportOptionsParameters metadataExportOptionsParameters) throws GenericException {
    final DatabaseModuleFactory databaseExportModuleFactory = getDatabaseExportModuleFactory(
      exportOptionsParameters.getSIARDVersion());

    databaseMigration.exportModule(databaseExportModuleFactory);

    for (Map.Entry<String, String> entry : exportOptionsParameters.getParameters().entrySet()) {
      if (!entry.getValue().equals("false")) {
        LOGGER.info("Export Options - " + entry.getKey() + "->" + entry.getValue());
        databaseMigration.exportModuleParameter(entry.getKey(), entry.getValue());
      }
    }

    if (metadataExportOptionsParameters != null) {
      for (Map.Entry<String, String> entry : metadataExportOptionsParameters.getValues().entrySet()) {
        LOGGER.info("Metadata Export Options - " + entry.getKey() + "->" + entry.getValue());
        databaseMigration.exportModuleParameter(entry.getKey(), entry.getValue());
      }
    }

    final String pathToTableFilter = constructTableFilter(tableAndColumnsParameters);
    LOGGER.info("Path to table-filter: " + pathToTableFilter);
    databaseMigration.exportModuleParameter("table-filter", pathToTableFilter);
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

  private static void getParameters(DBPTKModule dbptkModule, String moduleName, Parameters parameters) {
    if (dbptkModule == null)
      dbptkModule = new DBPTKModule();
    PreservationParameter preservationParameter;

    for (Parameter param : parameters.getParameters()) {
      if (param.getExportOptions() != null) {
        preservationParameter = new PreservationParameter(param.longName(), param.description(), param.required(),
          param.hasArgument(), param.getInputType().name(), param.getExportOptions().name());
      } else {
        preservationParameter = new PreservationParameter(param.longName(), param.description(), param.required(),
          param.hasArgument(), param.getInputType().name());
      }

      dbptkModule.addPreservationParameter(moduleName, preservationParameter);
    }
  }

  private static String createTemporaryFile(final String tmpFileName, final String tmpFileSuffix, final String content)
    throws GenericException {
    File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    final File tmpFile;
    try {
      tmpFile = File.createTempFile(tmpFileName, tmpFileSuffix, tmpDir);
    } catch (IOException e) {
      throw new GenericException("Could not create the temporary file", e);
    }
    FileOutputStream outputStream;
    try {
      outputStream = new FileOutputStream(tmpFile);
    } catch (FileNotFoundException e) {
      throw new GenericException("Could not find the temporary file", e);
    }
    OutputStreamWriter writer = new OutputStreamWriter(outputStream);
    try {
      writer.write(content);
      writer.flush();
      writer.close();
      outputStream.close();
    } catch (IOException e) {
      throw new GenericException("Could not close the temporary file", e);
    }

    return Paths.get(tmpFile.toURI()).normalize().toAbsolutePath().toString();
  }

  private static ArrayList<ExternalLobDBPTK> constructExternalLobFilter(TableAndColumnsParameters parameters)
    throws GenericException {
    final ArrayList<ExternalLOBsParameter> externalLOBsParameters = parameters.getExternalLOBsParameters();
    ArrayList<ExternalLobDBPTK> externalLobDBPTKParameters = new ArrayList<>();
    ExternalLobDBPTK externalLobDBPTK = new ExternalLobDBPTK();
    for (ExternalLOBsParameter parameter : externalLOBsParameters) {
      externalLobDBPTK.setBasePath(parameter.getBasePath());
      externalLobDBPTK.setReferenceType(parameter.getReferenceType());
      String sb = parameter.getTable().getSchemaName() + "." + parameter.getTable().getName() + "{"
        + parameter.getColumnName() + ";" + "}";
      externalLobDBPTK.setPathToColumnList(createTemporaryFile(SolrUtils.randomUUID(), ViewerConstants.TXT_SUFFIX, sb));

      externalLobDBPTKParameters.add(externalLobDBPTK);
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

    return createTemporaryFile(SolrUtils.randomUUID(), ViewerConstants.TXT_SUFFIX, tf.toString());
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
      final File tmpFile = File.createTempFile(SolrUtils.randomUUID(), ViewerConstants.YAML_SUFFIX, tmpDir);
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
        // DO NOTHING
      }
    }
  }

  /**
   * For Java 8 or below: check
   * http://robertmaldon.blogspot.com/2007/11/dynamically-add-to-eclipse-junit.html
   * (last access: 22-07-2019)
   */
  private static void addURL(URL url) throws Exception {
    URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    Class<URLClassLoader> clazz = URLClassLoader.class;

    // Use reflection
    Method method = clazz.getDeclaredMethod("addURL", URL.class);
    method.setAccessible(true);
    method.invoke(classLoader, url);
  }

  private static void setupImportModuleSSHConfiguration(DatabaseMigration databaseMigration,
    ConnectionParameters parameters) {
    if (parameters.doSSH()) {
      final SSHConfiguration sshConfiguration = parameters.getSSHConfiguration();

      databaseMigration.importModuleParameter("ssh", "true");
      databaseMigration.importModuleParameter("ssh-host", sshConfiguration.getHostname());
      databaseMigration.importModuleParameter("ssh-user", sshConfiguration.getUsername());
      databaseMigration.importModuleParameter("ssh-password", sshConfiguration.getPassword());
      databaseMigration.importModuleParameter("ssh-port", sshConfiguration.getPort());
    }
  }

  private static void setupExportModuleSSHConfiguration(DatabaseMigration databaseMigration,
    ConnectionParameters parameters) {
    if (parameters.doSSH()) {
      final SSHConfiguration sshConfiguration = parameters.getSSHConfiguration();

      databaseMigration.exportModuleParameter("ssh", "true");
      databaseMigration.exportModuleParameter("ssh-host", sshConfiguration.getHostname());
      databaseMigration.exportModuleParameter("ssh-user", sshConfiguration.getUsername());
      databaseMigration.exportModuleParameter("ssh-password", sshConfiguration.getPassword());
      databaseMigration.exportModuleParameter("ssh-port", sshConfiguration.getPort());
    }
  }

  private static void setupPathToDriver(ConnectionParameters parameters) throws Exception {
    if (parameters.getJDBCConnectionParameters().isDriver()) {
      addURL(new File(parameters.getJDBCConnectionParameters().getDriverPath()).toURI().toURL());
    }
  }
}
