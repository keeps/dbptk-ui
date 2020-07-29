package com.databasepreservation.common.server.controller;

import static com.databasepreservation.common.client.ViewerConstants.SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_SEARCHES_DATABASE_UUID;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.DatabaseMigration;
import com.databasepreservation.SIARDEdition;
import com.databasepreservation.SIARDValidation;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.search.SavedSearch;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.filter.SimpleFilterParameter;
import com.databasepreservation.common.client.models.dbptk.Module;
import com.databasepreservation.common.client.models.parameters.PreservationParameter;
import com.databasepreservation.common.client.models.parameters.SIARDUpdateParameters;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseFromToolkit;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseValidationStatus;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerSIARDBundle;
import com.databasepreservation.common.client.models.wizard.connection.ConnectionParameters;
import com.databasepreservation.common.client.models.wizard.connection.ConnectionResponse;
import com.databasepreservation.common.client.models.wizard.customViews.CustomViewsParameters;
import com.databasepreservation.common.client.models.wizard.export.ExportOptionsParameters;
import com.databasepreservation.common.client.models.wizard.export.MetadataExportOptionsParameters;
import com.databasepreservation.common.client.models.wizard.filter.MerkleTreeFilterParameters;
import com.databasepreservation.common.client.models.wizard.table.TableAndColumnsParameters;
import com.databasepreservation.common.exceptions.ViewerException;
import com.databasepreservation.common.server.SIARDProgressObserver;
import com.databasepreservation.common.server.ValidationProgressObserver;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.common.server.index.factory.SolrClientFactory;
import com.databasepreservation.common.server.index.schema.SolrDefaultCollectionRegistry;
import com.databasepreservation.common.server.index.utils.SolrUtils;
import com.databasepreservation.common.transformers.ToolkitStructure2ViewerStructure;
import com.databasepreservation.model.exception.ModuleException;
import com.databasepreservation.model.exception.SIARDVersionNotSupportedException;
import com.databasepreservation.model.exception.UnsupportedModuleException;
import com.databasepreservation.model.modules.DatabaseImportModule;
import com.databasepreservation.model.modules.DatabaseModuleFactory;
import com.databasepreservation.model.modules.filters.DatabaseFilterFactory;
import com.databasepreservation.model.modules.filters.ObservableFilter;
import com.databasepreservation.model.parameters.Parameter;
import com.databasepreservation.model.parameters.ParameterGroup;
import com.databasepreservation.model.parameters.Parameters;
import com.databasepreservation.model.reporters.NoOpReporter;
import com.databasepreservation.model.reporters.Reporter;
import com.databasepreservation.model.structure.DatabaseStructure;
import com.databasepreservation.modules.config.ImportConfigurationModuleFactory;
import com.databasepreservation.modules.jdbc.in.JDBCImportModule;
import com.databasepreservation.modules.siard.SIARD2ModuleFactory;
import com.databasepreservation.modules.siard.SIARDEditFactory;
import com.databasepreservation.modules.siard.SIARDValidateFactory;
import com.databasepreservation.modules.viewer.DbvtkModuleFactory;
import com.databasepreservation.utils.ReflectionUtils;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SIARDController {
  private static final Logger LOGGER = LoggerFactory.getLogger(SIARDController.class);

  public static String getReportFileContents(String databaseUUID) throws NotFoundException {
    Path reportPath = ViewerConfiguration.getInstance().getReportPath(databaseUUID);
    String result;
    if (reportPath.toFile().exists()) {
      try (InputStream in = Files.newInputStream(reportPath)) {
        result = IOUtils.toString(in, StandardCharsets.UTF_8);
      } catch (IOException e) {
        throw new NotFoundException("The database does not have a conversion report.", e);
      }
    } else {
      throw new NotFoundException("The database does not have a conversion report.");
    }
    return result;
  }

  public static List<List<String>> validateCustomViewQuery(ConnectionParameters parameters, String query)
    throws GenericException {
    Reporter reporter = new NoOpReporter();
    List<List<String>> results = new ArrayList<>();
    final DatabaseMigration databaseMigration = initializeDatabaseMigration(reporter);

    setupJDBCConnection(databaseMigration, parameters);

    try {
      DatabaseImportModule importModule = databaseMigration.getImportModule();
      importModule.setOnceReporter(reporter);
      if (importModule instanceof JDBCImportModule) {
        JDBCImportModule jdbcImportModule = (JDBCImportModule) importModule;
        results = jdbcImportModule.testCustomViewQuery(query);
      }
    } catch (ModuleException e) {
      throw new GenericException(e.getMessage());
    }

    return results;
  }

  public static ConnectionResponse testConnection(ConnectionParameters parameters) {
    NoOpReporter reporter = new NoOpReporter();
    final DatabaseMigration databaseMigration = initializeDatabaseMigration(reporter);
    ConnectionResponse response = new ConnectionResponse();
    try {
      setupJDBCConnection(databaseMigration, parameters);

      DatabaseImportModule importModule = databaseMigration.getImportModule();
      importModule.setOnceReporter(reporter);
      if (importModule instanceof JDBCImportModule) {
        JDBCImportModule jdbcImportModule = (JDBCImportModule) importModule;
        response.setConnected(jdbcImportModule.testConnection());
        response.setMessage("OK");
        return response;
      } else {
        response.setConnected(false);
        response.setMessage("");
        return response;
      }
    } catch (ModuleException | GenericException e) {
      response.setConnected(false);
      response.setMessage(e.getMessage());
      return response;
    }
  }

  public static boolean migrateToSIARD(String databaseUUID, String siardVersion, String siardPath,
    TableAndColumnsParameters tableAndColumnsParameters, ExportOptionsParameters exportOptionsParameters,
    MetadataExportOptionsParameters metadataExportOptionsParameters) throws GenericException {
    Reporter reporter = getReporter(databaseUUID);
    File f = new File(siardPath);
    if (f.exists() && !f.isDirectory()) {
      LOGGER.info("starting to convert database");
      final DatabaseMigration databaseMigration = initializeDatabaseMigration(reporter);

      if (tableAndColumnsParameters.isExternalLobConfigurationSet()) {
        final List<DatabaseFilterFactory> databaseFilterFactories = ReflectionUtils.collectDatabaseFilterFactory();
        databaseMigration.filterFactories(databaseFilterFactories);
      } else {
        databaseMigration.filterFactories(new ArrayList<>());
      }

      // Build Import Config Module
      DatabaseModuleFactory importConfigurationModuleFactory = new ImportConfigurationModuleFactory();
      databaseMigration.importModule(importConfigurationModuleFactory);

      String importConfigTmpPath = SiardControllerHelper.buildModuleConfigurationForSIARD(siardVersion, siardPath,
        tableAndColumnsParameters);
      databaseMigration.importModuleParameter(ImportConfigurationModuleFactory.PARAMETER_FILE, importConfigTmpPath);

      // BUILD Export Module
      setupSIARDExportModule(databaseMigration, exportOptionsParameters, metadataExportOptionsParameters);

      databaseMigration.filter(new ObservableFilter(new SIARDProgressObserver(databaseUUID)));

      long startTime = System.currentTimeMillis();
      try {
        databaseMigration.migrate();
      } catch (ModuleException | RuntimeException e) {
        throw new GenericException("Could not convert the database", e);
      }
      long duration = System.currentTimeMillis() - startTime;
      LOGGER.info("Conversion time {}m {}s", duration / 60000, duration % 60000 / 1000);
      return true;
    } else {
      throw new GenericException("SIARD file not found");
    }
  }

  public static boolean migrateToDBMS(String databaseUUID, String siardVersion, String siardPath,
    ConnectionParameters connectionParameters) throws GenericException {
    Reporter reporter = getReporter(databaseUUID);
    File f = new File(siardPath);
    if (f.exists() && !f.isDirectory()) {
      LOGGER.info("starting to convert database");
      final DatabaseMigration databaseMigration = initializeDatabaseMigration(reporter);

      databaseMigration.filterFactories(new ArrayList<>());

      // BUILD Import Module
      databaseMigration.importModule(getSIARDImportModuleFactory(siardVersion));
      databaseMigration.importModuleParameter(SIARD2ModuleFactory.PARAMETER_FILE, siardPath);

      // BUILD Export Module
      final DatabaseModuleFactory exportModuleFactory = getDatabaseExportModuleFactory(
        connectionParameters.getModuleName());
      SiardControllerHelper.setupExportModuleSSHConfiguration(databaseMigration, connectionParameters);

      for (Map.Entry<String, String> entry : connectionParameters.getJdbcParameters().getConnection().entrySet()) {
        LOGGER.info("Connection Options - {} -> {}", entry.getKey(), entry.getValue());
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

  public static String createSIARD(String uniqueId, ConnectionParameters connectionParameters,
    TableAndColumnsParameters tableAndColumnsParameters, CustomViewsParameters customViewsParameters,
    MerkleTreeFilterParameters merkleTreeFilterParameters, ExportOptionsParameters exportOptionsParameters,
    MetadataExportOptionsParameters metadataExportOptionsParameters) throws GenericException {
    final String databaseUUID = SolrUtils.randomUUID();
    Reporter reporter = getReporterForMigration(databaseUUID);
    LOGGER.info("starting to convert database {}", exportOptionsParameters.getSiardPath());

    final DatabaseMigration databaseMigration = initializeDatabaseMigration(reporter);

    List<DatabaseFilterFactory> filterFactories = new ArrayList<>();
    int index = 0;
    for (DatabaseFilterFactory factory : ReflectionUtils.collectDatabaseFilterFactory()) {
      if (!merkleTreeFilterParameters.getValues().isEmpty() && factory.getFilterName().equals("merkle-tree")) {
        filterFactories.add(factory);
      }

      if (tableAndColumnsParameters.isExternalLobConfigurationSet()
        && factory.getFilterName().equals("external-lobs")) {
        filterFactories.add(factory);
      }
    }

    for (DatabaseFilterFactory factory : filterFactories) {
      if (factory.getFilterName().equals("merkle-tree")) {
        merkleTreeFilterParameters.setDbptkFilterIndex(index);
      }
      if (factory.getFilterName().equals("external-lobs")) {
        tableAndColumnsParameters.setDbptkFilterIndex(index);
      }

      index++;
    }

    databaseMigration.filterFactories(filterFactories);

    // Build Import Config Module
    DatabaseModuleFactory importConfigurationModuleFactory = new ImportConfigurationModuleFactory();
    databaseMigration.importModule(importConfigurationModuleFactory);

    try {
      SiardControllerHelper.setupPathToDriver(connectionParameters);
    } catch (Exception e) {
      throw new GenericException("Could not load the driver", e);
    }

    String importConfigTmpPath = SiardControllerHelper.buildModuleConfiguration(connectionParameters,
      tableAndColumnsParameters, customViewsParameters);
    databaseMigration.importModuleParameter(ImportConfigurationModuleFactory.PARAMETER_FILE, importConfigTmpPath);

    // Build Export Module
    setupSIARDExportModule(databaseMigration, exportOptionsParameters, metadataExportOptionsParameters);

    // Merkle Tree Filter
    if (!merkleTreeFilterParameters.getValues().isEmpty()) {
      merkleTreeFilterParameters.getValues().forEach((k, v) -> {
        databaseMigration.filterParameter(k, v, merkleTreeFilterParameters.getDbptkFilterIndex());
      });
    }

    if (tableAndColumnsParameters.isExternalLobConfigurationSet()) {
      databaseMigration.filterParameter("", "", tableAndColumnsParameters.getDbptkFilterIndex());
    }

    databaseMigration.filter(new ObservableFilter(new SIARDProgressObserver(uniqueId)));

    long startTime = System.currentTimeMillis();
    try {
      databaseMigration.migrate();
    } catch (RuntimeException e) {
      throw new GenericException("Could not create the SIARD file", e);
    } catch (ModuleException e) {
      throw new GenericException(e.getMessage(), e);
    }
    long duration = System.currentTimeMillis() - startTime;
    LOGGER.info("Conversion time {}m {}s", duration / 60000, duration % 60000 / 1000);
    return databaseUUID;
  }

  public static ViewerMetadata getDatabaseMetadata(ConnectionParameters parameters) throws GenericException {
    final Reporter reporter = new NoOpReporter();
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

    try {
      ViewerDatabaseFromToolkit database = ToolkitStructure2ViewerStructure.getDatabase(schemaInformation, true);
      return database.getMetadata();
    } catch (ViewerException e) {
      throw new GenericException(e.getMessage());
    }
  }

  public static List<Module> getSIARDExportModule(String moduleName) throws GenericException {
    final DatabaseModuleFactory factory = getDatabaseExportModuleFactory(moduleName);
    if (factory == null) {
      throw new GenericException("Unable to retrieve the database export module factory");
    }

    try {
      final Parameters exportModuleParameters = factory.getExportModuleParameters();
      return Collections.singletonList(getParameters(factory.getModuleName(), exportModuleParameters));
    } catch (UnsupportedModuleException e) {
      throw new GenericException(e);
    }
  }

  public static List<Module> getSIARDModules(String moduleName) throws GenericException {
    List<Module> modules = new ArrayList<>(getSIARDImportModule(moduleName));
    modules.addAll(getSIARDExportModule(moduleName));

    return modules;
  }

  public static List<Module> getSIARDImportModule(String moduleName) throws GenericException {
    final DatabaseModuleFactory factory = getDatabaseImportModuleFactory(moduleName);
    if (factory == null) {
      throw new GenericException("Unable to retrieve the database import module factory");
    }

    try {
      final Parameters importModuleParameters = factory.getImportModuleParameters();
      return Collections.singletonList(getParameters(factory.getModuleName(), importModuleParameters));
    } catch (UnsupportedModuleException e) {
      throw new GenericException(e);
    }
  }

  public static List<Module> getDBMSModules() throws GenericException {
    List<Module> modules = new ArrayList<>(getDatabaseExportModules());
    modules.addAll(getDatabaseImportModules());

    return modules;
  }

  public static List<Module> getDBMSModules(String moduleName) throws GenericException {
    List<Module> modules = new ArrayList<>();
    final DatabaseModuleFactory databaseExportModuleFactory = getDatabaseExportModuleFactory(moduleName);
    final DatabaseModuleFactory databaseImportModuleFactory = getDatabaseImportModuleFactory(moduleName);

    if (databaseExportModuleFactory != null) {
      modules.add(getDatabaseModuleParameters(databaseExportModuleFactory));
    }

    if (databaseImportModuleFactory != null) {
      modules.add(getDatabaseModuleParameters(databaseImportModuleFactory));
    }

    return modules;
  }

  public static List<Module> getSiardModules() throws GenericException {
    List<Module> modules = new ArrayList<>(getSIARDExportModules());
    modules.addAll(getSIARDImportModules());

    return modules;
  }

  public static List<Module> getSIARDImportModules() throws GenericException {
    List<Module> modules = new ArrayList<>();

    Set<DatabaseModuleFactory> databaseModuleFactories = ReflectionUtils.collectDatabaseModuleFactories();

    for (DatabaseModuleFactory factory : databaseModuleFactories) {
      if (factory.isEnabled() && factory.producesImportModules()
        && factory.getModuleName().startsWith(ViewerConstants.SIARD)) {
        final Parameters exportModuleParameters;
        try {
          exportModuleParameters = factory.getExportModuleParameters();
          modules.add(getParameters(factory.getModuleName(), exportModuleParameters));
        } catch (UnsupportedModuleException e) {
          throw new GenericException(e);
        }
      }
    }

    return modules;
  }

  public static List<Module> getSIARDExportModules() throws GenericException {
    List<Module> modules = new ArrayList<>();

    Set<DatabaseModuleFactory> databaseModuleFactories = ReflectionUtils.collectDatabaseModuleFactories();

    for (DatabaseModuleFactory factory : databaseModuleFactories) {
      if (factory.isEnabled() && factory.producesExportModules()
        && factory.getModuleName().startsWith(ViewerConstants.SIARD)) {
        final Parameters exportModuleParameters;
        try {
          exportModuleParameters = factory.getExportModuleParameters();
          modules.add(getParameters(factory.getModuleName(), exportModuleParameters));
        } catch (UnsupportedModuleException e) {
          throw new GenericException(e);
        }
      }
    }

    return modules;
  }

  public static List<Module> getDatabaseExportModules() throws GenericException {
    List<Module> modules = new ArrayList<>();
    Set<DatabaseModuleFactory> databaseModuleFactories = ReflectionUtils.collectDatabaseModuleFactories();
    for (DatabaseModuleFactory factory : databaseModuleFactories) {
      if (!factory.getModuleName().equals("import-config")
        && !factory.getModuleName().toLowerCase().contains(ViewerConstants.SIARD)
        && !factory.getModuleName().equalsIgnoreCase("internal-dbptke-export") && factory.isEnabled()
        && factory.producesExportModules()) {
        modules.add(getDatabaseModuleParameters(factory));
      }
    }

    return modules;
  }

  public static List<Module> getDatabaseExportModule(String moduleName) throws GenericException {
    List<Module> modules = new ArrayList<>();
    Set<DatabaseModuleFactory> databaseModuleFactories = ReflectionUtils.collectDatabaseModuleFactories();
    for (DatabaseModuleFactory factory : databaseModuleFactories) {
      if (!factory.getModuleName().equals("import-config")
        && !factory.getModuleName().toLowerCase().contains(ViewerConstants.SIARD)
        && !factory.getModuleName().equalsIgnoreCase("internal-dbptke-export") && factory.isEnabled()
        && factory.producesExportModules()) {
        if (factory.getModuleName().equals(moduleName)) {
          modules.add(getDatabaseModuleParameters(factory));
        }
      }
    }

    return modules;
  }

  public static List<Module> getDatabaseImportModules() throws GenericException {
    List<Module> modules = new ArrayList<>();
    Set<DatabaseModuleFactory> databaseModuleFactories = ReflectionUtils.collectDatabaseModuleFactories();
    for (DatabaseModuleFactory factory : databaseModuleFactories) {
      if (factory.isEnabled() && factory.producesImportModules()
        && !factory.getModuleName().toLowerCase().contains(ViewerConstants.SIARD)
        && !factory.getModuleName().equals("import-config")) {
        modules.add(getDatabaseModuleParameters(factory));
      }
    }

    return modules;
  }

  public static List<Module> getDatabaseImportModule(String moduleName) throws GenericException {
    List<Module> modules = new ArrayList<>();
    Set<DatabaseModuleFactory> databaseModuleFactories = ReflectionUtils.collectDatabaseModuleFactories();
    for (DatabaseModuleFactory factory : databaseModuleFactories) {
      if (factory.isEnabled() && factory.producesImportModules()
        && !factory.getModuleName().toLowerCase().contains(ViewerConstants.SIARD)) {
        if (factory.getModuleName().equals(moduleName)) {
          modules.add(getDatabaseModuleParameters(factory));
        }
      }
    }

    return modules;
  }

  public static List<Module> getDatabaseFilterModules() {
    List<Module> modules = new ArrayList<>();
    final List<DatabaseFilterFactory> databaseFilterFactories = ReflectionUtils.collectDatabaseFilterFactory();
    for (DatabaseFilterFactory factory : databaseFilterFactories) {
      if (factory.isEnabled()) {
        modules.add(getFilterModuleParameters(factory));
      }
    }

    return modules;
  }

  public static List<Module> getDatabaseFilterModule(String moduleName) {
    List<Module> modules = new ArrayList<>();
    final List<DatabaseFilterFactory> databaseFilterFactories = ReflectionUtils.collectDatabaseFilterFactory();
    for (DatabaseFilterFactory factory : databaseFilterFactories) {
      if (factory.isEnabled() && factory.getFilterName().equals(moduleName)) {
        modules.add(getFilterModuleParameters(factory));
      }
    }

    return modules;
  }

  private static Module getFilterModuleParameters(DatabaseFilterFactory factory) {
    Module module = new Module(factory.getFilterName());

    PreservationParameter preservationParameter;
    for (Parameter parameter : factory.getParameters().getParameters()) {
      preservationParameter = new PreservationParameter(parameter.longName(), parameter.description(),
        parameter.required(), parameter.hasArgument(), parameter.getInputType().name(),
        parameter.getDefaultSelectedIndex());

      if (parameter.getPossibleValues() != null) {
        preservationParameter.setPossibleValues(parameter.getPossibleValues());
      }

      if (parameter.getFileFilter() != null) {
        preservationParameter.setFileFilter(parameter.getFileFilter().name());
      }

      if (parameter.valueIfNotSet() != null) {
        preservationParameter.setDefaultValue(parameter.valueIfNotSet());
      }

      module.addPreservationParameter(preservationParameter);
    }

    return module;
  }

  public static String loadMetadataFromLocal(String localPath) throws GenericException {
    String databaseUUID = SolrUtils.randomUUID();
    return loadMetadataFromLocal(databaseUUID, localPath);
  }

  private static String loadMetadataFromLocal(String databaseUUID, String localPath) throws GenericException {
    Path basePath = Paths.get(ViewerConfiguration.getInstance().getViewerConfigurationAsString("/",
      ViewerConfiguration.PROPERTY_BASE_UPLOAD_PATH));
    Path siardPath = basePath.resolve(localPath);
    convertSIARDMetadataToSolr(siardPath, databaseUUID);
    return databaseUUID;
  }

  private static void convertSIARDMetadataToSolr(Path siardPath, String databaseUUID) throws GenericException {
    validateSIARDLocation(siardPath);

    LOGGER.info("starting to import metadata database {}", siardPath.toAbsolutePath());

    Path reporterPath = ViewerConfiguration.getInstance().getReportPath(databaseUUID).toAbsolutePath();
    try (Reporter reporter = new Reporter(reporterPath.getParent().toString(), reporterPath.getFileName().toString())) {
      SIARDEdition siardEdition = SIARDEdition.newInstance();

      siardEdition.editModule(new SIARDEditFactory()).editModuleParameter(SIARDEditFactory.PARAMETER_FILE,
        Collections.singletonList(siardPath.toAbsolutePath().toString()));

      siardEdition.reporter(reporter);

      final DatabaseStructure metadata = siardEdition.getMetadata();

      final DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();

      ViewerDatabase viewerDatabase = new ViewerDatabase();

      viewerDatabase.setStatus(ViewerDatabaseStatus.METADATA_ONLY);
      viewerDatabase.setUuid(databaseUUID);

      viewerDatabase.setPath(siardPath.toAbsolutePath().toString());
      viewerDatabase.setSize(siardPath.toFile().length());
      viewerDatabase.setVersion(siardEdition.getSIARDVersion());
      viewerDatabase.setValidationStatus(ViewerDatabaseValidationStatus.NOT_VALIDATED);

      final ViewerDatabaseFromToolkit database = ToolkitStructure2ViewerStructure.getDatabase(metadata);

      viewerDatabase.setMetadata(database.getMetadata());

      solrManager.addDatabaseMetadata(viewerDatabase);

    } catch (IOException e) {
      throw new GenericException("Could not initialize conversion modules", e);
    } catch (ViewerException e) {
      throw new GenericException(e.getMessage(), e);
    } catch (ModuleException | RuntimeException e) {
      throw new GenericException("Could not convert the database to the Solr instance.", e);
    }
  }

  private static void validateSIARDLocation(Path siardPath) throws GenericException {
    if (ViewerFactory.getViewerConfiguration().getApplicationEnvironment()
      .equals(ViewerConstants.APPLICATION_ENV_SERVER)) {
      LOGGER.info("starting to check if path: {} is valid", siardPath.toAbsolutePath());
      // Checks if path is within the internal SIARD file path
      final boolean internal = ViewerConfiguration.checkPathIsWithin(siardPath,
        ViewerConfiguration.getInstance().getSIARDFilesPath());

      if (internal) {
        if (!siardPath.toFile().exists()) {
          throw new GenericException("File not found at path");
        }
      } else {
        // checks if is on the property base path
        final boolean onBasePath = ViewerConfiguration.checkPathIsWithin(siardPath, Paths.get(ViewerConfiguration
          .getInstance().getViewerConfigurationAsString("/", ViewerConfiguration.PROPERTY_BASE_UPLOAD_PATH)));
        if (onBasePath) {
          if (!siardPath.toFile().exists()) {
            throw new GenericException("File not found");
          }
        } else {
          throw new GenericException("File not found");
        }
      }
    }
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
    validateSIARDLocation(siardPath);

    LOGGER.info("starting to convert database {}", siardPath.toAbsolutePath());

    // build the SIARD import module, Solr export module, and start the
    // conversion
    Path reporterPath = ViewerConfiguration.getInstance().getReportPath(databaseUUID).toAbsolutePath();
    try (Reporter reporter = new Reporter(reporterPath.getParent().toString(), reporterPath.getFileName().toString())) {
      ViewerConfiguration configuration = ViewerConfiguration.getInstance();

      DatabaseMigration databaseMigration = DatabaseMigration.newInstance();

      // XXX remove this workaround after fix of NPE
      databaseMigration.filterFactories(new ArrayList<>());

      databaseMigration.importModule(new SIARD2ModuleFactory())
        .importModuleParameter(SIARD2ModuleFactory.PARAMETER_FILE, siardPath.toAbsolutePath().toString())
        .importModuleParameter(SIARD2ModuleFactory.PARAMETER_IGNORE_LOBS, "true");

      databaseMigration.exportModule(new DbvtkModuleFactory())
        .exportModuleParameter(DbvtkModuleFactory.PARAMETER_DATABASE_UUID, databaseUUID);

      databaseMigration.filter(new ObservableFilter(new SIARDProgressObserver(databaseUUID)));

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

  public static ViewerMetadata updateMetadataInformation(String databaseUUID, String siardPath,
    SIARDUpdateParameters parameters) throws GenericException {

    Path reporterPath = ViewerConfiguration.getInstance().getReportPathForEdition(databaseUUID).toAbsolutePath();
    ViewerMetadata metadata = parameters.getMetadata();
    try (Reporter reporter = new Reporter(reporterPath.getParent().toString(), reporterPath.getFileName().toString())) {

      ViewerSIARDBundle bundleSiard = parameters.getSiardBundle();
      if (new File(siardPath).isFile()) {
        LOGGER.info("Updating {}", siardPath);
        SIARDEdition siardEdition = SIARDEdition.newInstance();

        siardEdition.editModule(new SIARDEditFactory())
          .editModuleParameter(SIARDEditFactory.PARAMETER_FILE, Collections.singletonList(siardPath))
          .editModuleParameter(SIARDEditFactory.PARAMETER_SET, bundleSiard.getCommandMapAsList());

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

  public static boolean validateSIARD(String databaseUUID, String siardPath, String validationReportPath,
    String allowedTypesPath, boolean skipAdditionalChecks) throws GenericException {
    Path reporterPath = ViewerConfiguration.getInstance().getReportPathForValidation(databaseUUID).toAbsolutePath();
    boolean valid;

    try (Reporter reporter = new Reporter(reporterPath.getParent().toString(), reporterPath.getFileName().toString())) {
      SIARDValidation siardValidation = SIARDValidation.newInstance();
      siardValidation.validateModule(new SIARDValidateFactory())
        .validateModuleParameter(SIARDValidateFactory.PARAMETER_FILE, siardPath)
        .validateModuleParameter(SIARDValidateFactory.PARAMETER_ALLOWED, allowedTypesPath)
        .validateModuleParameter(SIARDValidateFactory.PARAMETER_REPORT, validationReportPath);

      if (skipAdditionalChecks) {
        siardValidation.validateModuleParameter(SIARDValidateFactory.PARAMETER_SKIP_ADDITIONAL_CHECKS, "true");
      }

      siardValidation.reporter(reporter);
      siardValidation.observer(new ValidationProgressObserver(databaseUUID));
      try {
        String dbptkVersion = ViewerConfiguration.getInstance().getDBPTKVersion();
        final DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();
        solrManager.updateSIARDValidationInformation(databaseUUID, ViewerDatabaseValidationStatus.VALIDATION_RUNNING,
          validationReportPath, dbptkVersion, new DateTime().toString());

        System.setProperty("dbptk.memory.dir",
          ViewerConfiguration.getInstance().getMapDBPath().toAbsolutePath().toString());

        valid = siardValidation.validate();

        ViewerDatabaseValidationStatus status;

        if (valid) {
          status = ViewerDatabaseValidationStatus.VALIDATION_SUCCESS;
        } else {
          status = ViewerDatabaseValidationStatus.VALIDATION_FAILED;
        }

        solrManager.updateSIARDValidationInformation(databaseUUID, status, validationReportPath, dbptkVersion,
          new DateTime().toString());
      } catch (IOException e) {
        updateStatusValidate(databaseUUID, ViewerDatabaseValidationStatus.ERROR);
        throw new GenericException("Failed to obtain the DBPTK version from properties", e);
      } catch (ModuleException e) {
        updateStatusValidate(databaseUUID, ViewerDatabaseValidationStatus.ERROR);
        if (e instanceof SIARDVersionNotSupportedException) {
          LOGGER.error("{}: {}", e.getMessage(), ((SIARDVersionNotSupportedException) e).getVersionInfo());
          throw new GenericException(e.getMessage() + ": " + ((SIARDVersionNotSupportedException) e).getVersionInfo());
        }
        throw new GenericException(e);
      } catch (RuntimeException e) {
        updateStatusValidate(databaseUUID, ViewerDatabaseValidationStatus.ERROR);
        throw new GenericException(e.getMessage(), e);
      }
    } catch (IOException e) {
      updateStatusValidate(databaseUUID, ViewerDatabaseValidationStatus.ERROR);
      throw new GenericException("Could not initialize the validate module.", e);
    }

    return valid;
  }

  public static void updateSIARDValidatorIndicators(String databaseUUID, String passed, String failed, String errors,
    String warnings, String skipped) {
    final DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();
    solrManager.updateSIARDValidationIndicators(databaseUUID, passed, errors, failed, warnings, skipped);
  }

  public static void updateStatusValidate(String databaseUUID, ViewerDatabaseValidationStatus status) {
    final DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();
    solrManager.updateSIARDValidationInformation(databaseUUID, status, null, null, new DateTime().toString());
  }

  public static boolean deleteAll(String databaseUUID)
    throws NotFoundException, GenericException, RequestNotValidException {
    final DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();

    ViewerDatabase database = solrManager.retrieve(ViewerDatabase.class, databaseUUID);

    if (ViewerFactory.getViewerConfiguration().getApplicationEnvironment()
      .equals(ViewerConstants.APPLICATION_ENV_SERVER)) {
      String siardPath = database.getPath();
      final boolean deleteSiard = !ViewerConfiguration.getInstance().getViewerConfigurationAsBoolean(false,
        ViewerConfiguration.PROPERTY_DISABLE_SIARD_DELETION);
      if (StringUtils.isNotBlank(siardPath) && Paths.get(siardPath).toFile().exists() && deleteSiard) {
        deleteSIARDFileFromPath(siardPath, databaseUUID);
      }
    }

    ViewerFactory.getConfigurationManager().deleteDatabaseFolder(databaseUUID);

    String reportPath = database.getValidatorReportPath();
    if (StringUtils.isNotBlank(reportPath) && Paths.get(reportPath).toFile().exists()) {
      deleteValidatorReportFileFromPath(reportPath, databaseUUID);
    }

    if (database.getStatus().equals(ViewerDatabaseStatus.AVAILABLE)
      || database.getStatus().equals(ViewerDatabaseStatus.ERROR)) {
      final String collectionName = SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX + databaseUUID;
      if (SolrClientFactory.get().deleteCollection(collectionName)) {
        Filter savedSearchFilter = new Filter(new SimpleFilterParameter(SOLR_SEARCHES_DATABASE_UUID, databaseUUID));
        SolrUtils.delete(ViewerFactory.getSolrClient(), SolrDefaultCollectionRegistry.get(SavedSearch.class),
          savedSearchFilter);

        ViewerFactory.getSolrManager().markDatabaseCollection(databaseUUID, ViewerDatabaseStatus.METADATA_ONLY);
      }
    }
    ViewerFactory.getSolrManager().deleteDatabasesCollection(databaseUUID);
    return true;
  }

  public static void deleteSIARDFileFromPath(String siardPath, String databaseUUID) throws GenericException {
    Path path = Paths.get(siardPath);
    if (!path.toFile().exists()) {
      throw new GenericException("File not found at path: " + siardPath);
    }

    try {
      Files.delete(path);
      LOGGER.info("SIARD file removed from system ({})", path.toAbsolutePath());
      final DatabaseRowsSolrManager solrManager = ViewerFactory.getSolrManager();
      solrManager.updateSIARDPath(databaseUUID, null);

    } catch (IOException e) {
      throw new GenericException("Could not delete SIARD file from system", e);
    }
  }

  public static void deleteValidatorReportFileFromPath(String validatorReportPath, String databaseUUID)
    throws GenericException {
    Path path = Paths.get(validatorReportPath);
    if (!path.toFile().exists()) {
      throw new GenericException("File not found at path: " + validatorReportPath);
    }

    try {
      Files.delete(path);
      LOGGER.info("SIARD validator report file removed from system ({})", path.toAbsolutePath());
      updateStatusValidate(databaseUUID, ViewerDatabaseValidationStatus.NOT_VALIDATED);
      updateSIARDValidatorIndicators(databaseUUID, null, null, null, null, null);
    } catch (IOException e) {
      throw new GenericException("Could not delete SIARD validator report file from system", e);
    }
  }

  /****************************************************************************
   * Private auxiliary Methods
   ****************************************************************************/
  private static Reporter getReporter(final String databaseUUID) {
    Path reporterPath = ViewerConfiguration.getInstance().getReportPath(databaseUUID).toAbsolutePath();
    return new Reporter(reporterPath.getParent().toString(), reporterPath.getFileName().toString());
  }

  private static Reporter getReporterForMigration(final String databaseUUID) {
    Path reporterPath = ViewerConfiguration.getInstance().getReportPathForMigration(databaseUUID).toAbsolutePath();
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
      SiardControllerHelper.setupImportModuleSSHConfiguration(databaseMigration, parameters);
      try {
        SiardControllerHelper.setupPathToDriver(parameters);
      } catch (Exception e) {
        throw new GenericException("Could not load the driver", e);
      }

      for (Map.Entry<String, String> entry : parameters.getJdbcParameters().getConnection().entrySet()) {
        databaseMigration.importModuleParameter(entry.getKey(), entry.getValue());
      }
    }
  }

  private static void setupSIARDExportModule(DatabaseMigration databaseMigration,
    ExportOptionsParameters exportOptionsParameters, MetadataExportOptionsParameters metadataExportOptionsParameters) {
    final DatabaseModuleFactory databaseExportModuleFactory = getDatabaseExportModuleFactory(
      exportOptionsParameters.getSiardVersion());

    databaseMigration.exportModule(databaseExportModuleFactory);

    for (Map.Entry<String, String> entry : exportOptionsParameters.getParameters().entrySet()) {
      if (!entry.getValue().equals("false")) {
        LOGGER.info("Export Options - {} -> {}", entry.getKey(), entry.getValue());
        databaseMigration.exportModuleParameter(entry.getKey(), entry.getValue());
      }
    }

    if (metadataExportOptionsParameters != null) {
      for (Map.Entry<String, String> entry : metadataExportOptionsParameters.getValues().entrySet()) {
        LOGGER.info("Metadata Export Options - {} -> {}", entry.getKey(), entry.getValue());
        databaseMigration.exportModuleParameter(entry.getKey(), entry.getValue());
      }
    }
  }

  private static DatabaseModuleFactory getDatabaseImportModuleFactory(String moduleName) {
    Set<DatabaseModuleFactory> databaseModuleFactories = ReflectionUtils.collectDatabaseModuleFactories();

    DatabaseModuleFactory factory = null;

    for (DatabaseModuleFactory dbFactory : databaseModuleFactories) {
      if (dbFactory.isEnabled() && dbFactory.producesImportModules() && dbFactory.getModuleName().equals(moduleName)) {
        factory = dbFactory;
      }
    }

    return factory;
  }

  private static DatabaseModuleFactory getSIARDImportModuleFactory(String version) {
    Set<DatabaseModuleFactory> databaseModuleFactories = ReflectionUtils.collectDatabaseModuleFactories();
    final String moduleName;
    if (version.equals("2.0") || version.equals("2.1")) {
      moduleName = "siard-2";
    } else if (version.equals("1.0")) {
      moduleName = "siard-1";
    } else {
      moduleName = "";
    }

    DatabaseModuleFactory factory = null;

    for (DatabaseModuleFactory dbFactory : databaseModuleFactories) {
      if (dbFactory.isEnabled() && dbFactory.producesImportModules() && dbFactory.getModuleName().equals(moduleName)) {
        factory = dbFactory;
      }
    }

    return factory;
  }

  private static DatabaseModuleFactory getDatabaseExportModuleFactory(String moduleName) {
    Set<DatabaseModuleFactory> databaseModuleFactories = ReflectionUtils.collectDatabaseModuleFactories();

    DatabaseModuleFactory factory = null;

    for (DatabaseModuleFactory dbFactory : databaseModuleFactories) {
      if (dbFactory.isEnabled() && dbFactory.producesExportModules() && dbFactory.getModuleName().equals(moduleName)) {
        factory = dbFactory;
      }
    }

    return factory;
  }

  private static Module getDatabaseModuleParameters(DatabaseModuleFactory factory) throws GenericException {
    Module module = new Module(factory.getModuleName());

    PreservationParameter preservationParameter;
    final Parameters parameters;
    try {
      parameters = factory.getConnectionParameters();
      for (Parameter param : parameters.getParameters()) {
        preservationParameter = new PreservationParameter(param.longName(), param.description(), param.required(),
          param.hasArgument(), param.getInputType().name());
        if (param.getFileFilter() != null) {
          preservationParameter.setFileFilter(param.getFileFilter().name());
        }
        if (param.valueIfNotSet() != null) {
          preservationParameter.setDefaultValue(param.valueIfNotSet());
        }
        module.addPreservationParameter(preservationParameter);
      }

      for (ParameterGroup pg : parameters.getGroups()) {
        for (Parameter param : pg.getParameters()) {
          preservationParameter = new PreservationParameter(param.longName(), param.description(), param.required(),
            param.hasArgument(), param.getInputType().name());
          if (param.getFileFilter() != null) {
            preservationParameter.setFileFilter(param.getFileFilter().name());
          }
          module.addPreservationParameter(preservationParameter);
        }
      }
    } catch (UnsupportedModuleException e) {
      throw new GenericException(e);
    }

    return module;
  }

  private static Module getParameters(String moduleName, Parameters parameters) {
    Module module = new Module(moduleName);
    PreservationParameter preservationParameter;

    for (Parameter param : parameters.getParameters()) {
      if (param.getExportOptions() != null) {
        preservationParameter = new PreservationParameter(param.longName(), param.description(), param.required(),
          param.hasArgument(), param.getInputType().name(), param.getExportOptions().name(),
          param.getDefaultSelectedIndex());
        if (param.getFileFilter() != null) {
          preservationParameter.setFileFilter(param.getFileFilter().name());
        }
        if (param.getPossibleValues() != null) {
          preservationParameter.setPossibleValues(param.getPossibleValues());
        }
      } else {
        preservationParameter = new PreservationParameter(param.longName(), param.description(), param.required(),
          param.hasArgument(), param.getInputType().name(), param.getDefaultSelectedIndex());
        if (param.getFileFilter() != null) {
          preservationParameter.setFileFilter(param.getFileFilter().name());
        }
        if (param.getPossibleValues() != null) {
          preservationParameter.setPossibleValues(param.getPossibleValues());
        }
      }

      if (param.valueIfNotSet() != null) {
        preservationParameter.setDefaultValue(param.valueIfNotSet());
      }

      module.addPreservationParameter(preservationParameter);
    }

    return module;
  }
}
