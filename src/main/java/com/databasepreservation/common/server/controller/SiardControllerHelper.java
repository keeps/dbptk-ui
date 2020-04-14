package com.databasepreservation.common.server.controller;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.modules.siard.SIARD2ModuleFactory;
import org.apache.commons.lang3.StringUtils;
import org.roda.core.data.exceptions.GenericException;

import com.databasepreservation.DatabaseMigration;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.wizard.connection.ConnectionParameters;
import com.databasepreservation.common.client.models.wizard.connection.SSHConfiguration;
import com.databasepreservation.common.client.models.wizard.customViews.CustomViewsParameters;
import com.databasepreservation.common.client.models.wizard.table.ColumnParameter;
import com.databasepreservation.common.client.models.wizard.table.ExternalLobParameter;
import com.databasepreservation.common.client.models.wizard.table.TableAndColumnsParameters;
import com.databasepreservation.common.client.models.wizard.table.ViewAndColumnsParameter;
import com.databasepreservation.common.server.index.utils.SolrUtils;
import com.databasepreservation.model.modules.configuration.ColumnConfiguration;
import com.databasepreservation.model.modules.configuration.CustomViewConfiguration;
import com.databasepreservation.model.modules.configuration.ExternalLobsConfiguration;
import com.databasepreservation.model.modules.configuration.ModuleConfiguration;
import com.databasepreservation.model.modules.configuration.SchemaConfiguration;
import com.databasepreservation.model.modules.configuration.TableConfiguration;
import com.databasepreservation.model.modules.configuration.ViewConfiguration;
import com.databasepreservation.model.modules.configuration.enums.ExternalLobsAccessMethod;
import com.databasepreservation.utils.ModuleConfigurationUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class SiardControllerHelper {
  public static String buildModuleConfigurationForSIARD(String siardVersion, String siardPath,
    TableAndColumnsParameters tableAndColumnsParameters) throws GenericException {
    try {
      File tmpDir = new File(System.getProperty("java.io.tmpdir"));
      final File tmpFile = File.createTempFile(SolrUtils.randomUUID(), ViewerConstants.YAML_SUFFIX, tmpDir);

      ModuleConfiguration moduleConfiguration = ModuleConfigurationUtils.getDefaultModuleConfiguration();
      buildImportModuleConfiguration(moduleConfiguration, siardVersion, siardPath);
      buildSchemaConfiguration(moduleConfiguration, tableAndColumnsParameters);

      ObjectMapper mapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
      mapper.writeValue(tmpFile, moduleConfiguration);

      return tmpFile.getPath();
    } catch (IOException e) {
      throw new GenericException("Could not create the import-config YAML file", e);
    }
  }

  public static String buildModuleConfiguration(ConnectionParameters connectionParameters,
    TableAndColumnsParameters tableAndColumnsParameters, CustomViewsParameters customViewsParameters)
    throws GenericException {
    try {
      File tmpDir = new File(System.getProperty("java.io.tmpdir"));
      final File tmpFile = File.createTempFile(SolrUtils.randomUUID(), ViewerConstants.YAML_SUFFIX, tmpDir);

      ModuleConfiguration moduleConfiguration = ModuleConfigurationUtils.getDefaultModuleConfiguration();
      buildImportModuleConfiguration(moduleConfiguration, connectionParameters);
      buildSchemaConfiguration(moduleConfiguration, tableAndColumnsParameters, customViewsParameters);

      ObjectMapper mapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
      mapper.writeValue(tmpFile, moduleConfiguration);

      return tmpFile.getPath();
    } catch (IOException e) {
      throw new GenericException("Could not create the import-config YAML file", e);
    }
  }

  private static void buildImportModuleConfiguration(ModuleConfiguration moduleConfiguration, String siardVersion, String siardPath) {
    String moduleName;
    if (siardVersion.equals("2.0") || siardVersion.equals("2.1")) {
      moduleName = "siard-2";
    } else if (siardVersion.equals("1.0")) {
      moduleName = "siard-1";
    } else {
      moduleName = "";
    }

    Map<String, String> properties = new HashMap<>();
    properties.put(SIARD2ModuleFactory.PARAMETER_FILE, siardPath);

    ModuleConfigurationUtils.addImportParameters(moduleConfiguration, moduleName, properties, null);
  }

  private static void buildImportModuleConfiguration(ModuleConfiguration moduleConfiguration,
    ConnectionParameters connectionParameters) throws GenericException {
    Map<String, String> remoteConnectionProperties;

    if (connectionParameters.doSSH()) {
      remoteConnectionProperties = connectionParameters.getSshConfiguration().getRemoteProperties();
    } else {
      remoteConnectionProperties = new LinkedHashMap<>();
    }

    ModuleConfigurationUtils.addImportParameters(moduleConfiguration, connectionParameters.getModuleName(),
      connectionParameters.getJdbcParameters().getConnection(), remoteConnectionProperties);

    try {
      setupPathToDriver(connectionParameters);
    } catch (Exception e) {
      throw new GenericException("Could not load the driver", e);
    }
  }

  private static void buildSchemaConfiguration(ModuleConfiguration moduleConfiguration,
    TableAndColumnsParameters tableAndColumnsParameters) {
    buildTableConfiguration(moduleConfiguration, tableAndColumnsParameters);
  }

  private static void buildSchemaConfiguration(ModuleConfiguration moduleConfiguration,
    TableAndColumnsParameters tableAndColumnsParameters, CustomViewsParameters customViewsParameters) {

    buildCustomViewConfiguration(moduleConfiguration, customViewsParameters);
    buildTableConfiguration(moduleConfiguration, tableAndColumnsParameters);
    buildViewConfiguration(moduleConfiguration, tableAndColumnsParameters);
  }

  private static void buildViewConfiguration(ModuleConfiguration moduleConfiguration,
    TableAndColumnsParameters tableAndColumnsParameters) {
    tableAndColumnsParameters.getViewAndColumnsParameterMap().forEach((key, value) -> {
      SchemaConfiguration schemaConfiguration = moduleConfiguration.getSchemaConfigurations()
        .get(value.getSchemaName());

      if (schemaConfiguration == null) {
        schemaConfiguration = new SchemaConfiguration();
      }

      schemaConfiguration.getViewConfigurations().add(getViewConfiguration(value, value.getColumns()));
      moduleConfiguration.getSchemaConfigurations().put(value.getSchemaName(), schemaConfiguration);
    });
  }

  private static ViewConfiguration getViewConfiguration(ViewAndColumnsParameter parameter,
    List<ColumnParameter> columns) {
    ViewConfiguration configuration = new ViewConfiguration();
    configuration.setName(parameter.getName());
    configuration.setMaterialized(parameter.isMaterialize());
    configuration.setColumns(getColumnConfiguration(columns));

    return configuration;
  }

  private static void buildTableConfiguration(ModuleConfiguration moduleConfiguration,
    TableAndColumnsParameters tableAndColumnsParameters) {
    tableAndColumnsParameters.getTableAndColumnsParameterMap().forEach((key, value) -> {
      SchemaConfiguration schemaConfiguration = moduleConfiguration.getSchemaConfigurations()
        .get(value.getSchemaName());

      if (schemaConfiguration == null) {
        schemaConfiguration = new SchemaConfiguration();
      }

      schemaConfiguration.getTableConfigurations().add(getTableConfiguration(value.getName(), value.getColumns()));
      moduleConfiguration.getSchemaConfigurations().put(value.getSchemaName(), schemaConfiguration);
    });
  }

  private static TableConfiguration getTableConfiguration(String tableName, List<ColumnParameter> columns) {
    TableConfiguration configuration = new TableConfiguration();
    configuration.setName(tableName);
    configuration.setColumns(getColumnConfiguration(columns));

    return configuration;
  }

  private static List<ColumnConfiguration> getColumnConfiguration(List<ColumnParameter> columns) {
    List<ColumnConfiguration> columnConfigurations = new ArrayList<>();

    columns.forEach(column -> {
      ColumnConfiguration columnConfiguration = new ColumnConfiguration();
      columnConfiguration.setName(column.getName());
      columnConfiguration.setMerkle(column.isUseOnMerkle());
      if (column.getExternalLobParameter() != null) {
        columnConfiguration.setExternalLob(getExternalLobConfiguration(column.getExternalLobParameter()));
      }
      columnConfigurations.add(columnConfiguration);
    });

    return columnConfigurations;
  }

  private static ExternalLobsConfiguration getExternalLobConfiguration(ExternalLobParameter externalLobParameter) {
    ExternalLobsConfiguration configuration = new ExternalLobsConfiguration();

    if (StringUtils.isNotBlank(externalLobParameter.getReferenceType())) {
      if (externalLobParameter.getReferenceType().equals("file-system")) {
        configuration.setAccessModule(ExternalLobsAccessMethod.FILE_SYSTEM);
      } else if (externalLobParameter.getReferenceType().equals("remote-file-system")) {
        configuration.setAccessModule(ExternalLobsAccessMethod.REMOTE);
      }
    }

    if (StringUtils.isNotBlank(externalLobParameter.getBasePath())) {
      configuration.setBasePath(externalLobParameter.getBasePath());
    } else {
      configuration.setBasePath("");
    }

    return configuration;
  }

  private static void buildCustomViewConfiguration(ModuleConfiguration moduleConfiguration,
    CustomViewsParameters customViewsParameters) {
    customViewsParameters.getCustomViewsParameter().forEach((key, value) -> {
      SchemaConfiguration schemaConfiguration = moduleConfiguration.getSchemaConfigurations().get(key);

      if (schemaConfiguration == null) {
        schemaConfiguration = new SchemaConfiguration();
      }

      schemaConfiguration.getCustomViewConfigurations().add(getCustomViewConfiguration(customViewsParameters));
      moduleConfiguration.getSchemaConfigurations().put(value.getSchemaName(), schemaConfiguration);
    });
  }

  private static CustomViewConfiguration getCustomViewConfiguration(CustomViewsParameters customViewsParameters) {
    CustomViewConfiguration customViewConfiguration = new CustomViewConfiguration();

    customViewsParameters.getCustomViewsParameter().forEach((key, customViewsParameter) -> {
      customViewConfiguration.setName(customViewsParameter.getCustomViewName());
      customViewConfiguration.setDescription(customViewsParameter.getCustomViewDescription());
      customViewConfiguration.setQuery(customViewsParameter.getCustomViewQuery());
    });

    return customViewConfiguration;
  }

  public static void setupPathToDriver(ConnectionParameters parameters) throws Exception {
    if (parameters.getJdbcParameters().isDriver()) {
      addURL(new File(parameters.getJdbcParameters().getDriverPath()).toURI().toURL());
    }
  }

  public static void setupImportModuleSSHConfiguration(DatabaseMigration databaseMigration,
    ConnectionParameters parameters) {
    if (parameters.doSSH()) {
      final SSHConfiguration sshConfiguration = parameters.getSshConfiguration();
      sshConfiguration.getRemoteProperties().forEach(databaseMigration::importModuleParameter);
    }
  }

  public static void setupExportModuleSSHConfiguration(DatabaseMigration databaseMigration,
    ConnectionParameters parameters) {
    if (parameters.doSSH()) {
      final SSHConfiguration sshConfiguration = parameters.getSshConfiguration();
      sshConfiguration.getRemoteProperties().forEach(databaseMigration::exportModuleParameter);
    }
  }

  /**
   * For Java 8 or below: check
   * http://robertmaldon.blogspot.com/2007/11/dynamically-add-to-eclipse-junit.html
   * (last access: 22-07-2019)
   */
  private static void addURL(URL url) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    Class<URLClassLoader> clazz = URLClassLoader.class;

    // Use reflection
    Method method = clazz.getDeclaredMethod("addURL", URL.class);
    method.setAccessible(true);
    method.invoke(classLoader, url);
  }
}
