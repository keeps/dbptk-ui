package com.databasepreservation.common.utils;

import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.configuration.collection.ViewerAdvancedConfiguration;
import com.databasepreservation.common.client.models.configuration.collection.ViewerCollectionConfiguration;
import com.databasepreservation.common.client.models.configuration.collection.ViewerColumnConfiguration;
import com.databasepreservation.common.client.models.configuration.collection.ViewerDetailsConfiguration;
import com.databasepreservation.common.client.models.configuration.collection.ViewerExportConfiguration;
import com.databasepreservation.common.client.models.configuration.collection.ViewerFacetsConfiguration;
import com.databasepreservation.common.client.models.configuration.collection.ViewerListConfiguration;
import com.databasepreservation.common.client.models.configuration.collection.ViewerSearchConfiguration;
import com.databasepreservation.common.client.models.configuration.collection.ViewerTableConfiguration;
import com.databasepreservation.common.client.models.configuration.collection.ViewerTemplateConfiguration;
import com.databasepreservation.common.client.models.configuration.database.ViewerDatabaseConfiguration;
import com.databasepreservation.common.client.models.configuration.database.ViewerSiardConfiguration;
import com.databasepreservation.common.client.models.configuration.database.ViewerValidationConfiguration;
import com.databasepreservation.common.client.models.configuration.database.ViewerValidationIndicators;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseValidationStatus;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.models.structure.ViewerType;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ViewerConfigurationUtils {

  public static List<ViewerTableConfiguration> getTableConfigurationFromList(ViewerDatabase database) {
    List<ViewerTableConfiguration> viewerTableConfigurations = new ArrayList<>();
    for (ViewerTable table : database.getMetadata().getTables().values()) {
      viewerTableConfigurations.add(getTableConfiguration(database, table));
    }
    return viewerTableConfigurations;
  }

  public static ViewerTableConfiguration getTableConfiguration(ViewerDatabase database, ViewerTable table) {
    return getTableConfiguration(database, table, true);
  }

  public static ViewerTableConfiguration getTableConfiguration(ViewerDatabase database, ViewerTable table,
    boolean show) {
    final int schemaIndex = database.getMetadata().getSchemaIndex(table.getSchemaUUID());
    final int tableIndex = database.getMetadata().getTableIndex(table.getUuid());

    ViewerTableConfiguration tableConfiguration = new ViewerTableConfiguration();
    tableConfiguration.setUuid(table.getUuid());
    tableConfiguration.setId(table.getId());
    tableConfiguration.setSchemaFolder(ViewerConstants.SIARD_SCHEMA_PREFIX + schemaIndex);
    tableConfiguration.setTableFolder(ViewerConstants.SIARD_TABLE_PREFIX + tableIndex);
    tableConfiguration.setName(table.getName());
    tableConfiguration.setCustomName(table.getName());
    tableConfiguration.setDescription(table.getDescription());
    tableConfiguration.setCustomDescription(table.getDescription());
    tableConfiguration.setColumns(getColumnsConfiguration(table.getColumns()));
    tableConfiguration.setShow(show);

    return tableConfiguration;
  }

  public static List<ViewerColumnConfiguration> getColumnsConfiguration(List<ViewerColumn> viewerColumns) {
    return getColumnsConfiguration(viewerColumns, true);
  }

  public static List<ViewerColumnConfiguration> getColumnsConfiguration(List<ViewerColumn> viewerColumns,
    boolean show) {
    List<ViewerColumnConfiguration> viewerColumnConfigurationList = new ArrayList<>();
    int order = 1;
    for (ViewerColumn viewerColumn : viewerColumns) {
      viewerColumnConfigurationList.add(getColumnConfiguration(viewerColumn, show, order++));
    }
    return viewerColumnConfigurationList;
  }

  public static ViewerColumnConfiguration getColumnConfiguration(ViewerColumn column, boolean show, int order) {
    ViewerColumnConfiguration configuration = new ViewerColumnConfiguration();
    configuration.setId(column.getSolrName());
    configuration.setName(column.getDisplayName());
    if (column.getType() != null) {
      configuration.setOriginalType(column.getType().getOriginalTypeName());
      configuration.setTypeName(column.getType().getTypeName());
      configuration.setNullable(column.getNillable().toString());
      configuration.setType(column.getType().getDbType());
    }
    configuration.setColumnIndex(column.getColumnIndexInEnclosingTable());
    configuration.setCustomName(column.getDisplayName());
    configuration.setDescription(column.getDescription());
    configuration.setCustomDescription(column.getDescription());
    configuration.setNestedColumns(null);
    configuration.setOrder(order);
    configuration.setViewerExportConfiguration(getExportConfiguration());
    configuration.setViewerSearchConfiguration(getSearchConfiguration(show));
    configuration.setViewerDetailsConfiguration(getDetailsConfiguration(show));

    if (column.getType().getDbType().equals(ViewerType.dbTypes.BINARY)) {
      final ViewerTemplateConfiguration template = getTemplateConfiguration();
      template.setTemplate(ViewerConstants.DEFAULT_DOWNLOAD_LABEL_TEMPLATE);
      configuration.updateSearchListTemplate(template);
      configuration.updateDetailsTemplate(template);
    }

    return configuration;
  }

  private static ViewerExportConfiguration getExportConfiguration() {
    ViewerExportConfiguration configuration = new ViewerExportConfiguration();
    configuration.setViewerTemplateConfiguration(getTemplateConfiguration());

    return configuration;
  }

  public static ViewerDetailsConfiguration getDetailsConfiguration(boolean show) {
    ViewerDetailsConfiguration configuration = new ViewerDetailsConfiguration();
    configuration.setShow(show);
    configuration.setViewerTemplateConfiguration(getTemplateConfiguration());

    return configuration;
  }

  public static ViewerSearchConfiguration getSearchConfiguration(boolean show) {
    ViewerSearchConfiguration configuration = new ViewerSearchConfiguration();
    configuration.setAdvanced(getAdvancedConfiguration());
    configuration.setList(getListConfiguration(show));
    configuration.setFacets(getFacetsConfiguration());

    return configuration;
  }

  public static ViewerAdvancedConfiguration getAdvancedConfiguration() {
    return new ViewerAdvancedConfiguration();
  }

  public static ViewerListConfiguration getListConfiguration(boolean show) {
    ViewerListConfiguration configuration = new ViewerListConfiguration();
    configuration.setShow(show);
    configuration.setTemplate(getTemplateConfiguration());

    return configuration;
  }

  public static ViewerTemplateConfiguration getTemplateConfiguration() {
    return new ViewerTemplateConfiguration();
  }

  public static ViewerFacetsConfiguration getFacetsConfiguration() {
    return new ViewerFacetsConfiguration();
  }

  public static ViewerDatabaseConfiguration getDatabaseConfiguration(ViewerDatabase database) {
    ViewerDatabaseConfiguration configuration = new ViewerDatabaseConfiguration();
    configuration.setId(database.getUuid());
    configuration.setViewerValidationConfiguration(getValidationConfiguration(database));
    configuration.setViewerSiardConfiguration(getSiardConfiguration(database));

    return configuration;
  }

  public static ViewerValidationConfiguration getValidationConfiguration(ViewerDatabase database) {
    ViewerValidationConfiguration configuration = new ViewerValidationConfiguration();
    configuration.setValidatorVersion(database.getValidatedVersion());
    configuration.setReportLocation(database.getValidatorReportPath());
    configuration.setCreatedOn(database.getValidatedAt());
    configuration.setValidationStatus(database.getValidationStatus());
    configuration.setViewerValidationIndicators(getIndicators(database));

    return configuration;
  }

  public static ViewerValidationConfiguration getValidationConfiguration(
    ViewerDatabaseValidationStatus validationStatus, String date, String validationReportPath, String dbptkVersion,
    ViewerValidationIndicators viewerValidationIndicators) {
    ViewerValidationConfiguration configuration = new ViewerValidationConfiguration();
    configuration.setValidationStatus(validationStatus);
    configuration.setCreatedOn(date);
    configuration.setReportLocation(validationReportPath);
    configuration.setValidatorVersion(dbptkVersion);
    configuration.setViewerValidationIndicators(viewerValidationIndicators);

    return configuration;
  }

  public static ViewerValidationIndicators getIndicators(ViewerDatabase database) {
    ViewerValidationIndicators viewerValidationIndicators = new ViewerValidationIndicators();
    viewerValidationIndicators.setSuccess(database.getValidationPassed());
    viewerValidationIndicators.setFailed(database.getValidationErrors());
    viewerValidationIndicators.setWarnings(database.getValidationWarnings());
    viewerValidationIndicators.setSkipped(database.getValidationSkipped());

    return viewerValidationIndicators;
  }

  public static ViewerValidationIndicators getIndicators(String passed, String failed, String warnings,
    String skipped) {
    ViewerValidationIndicators viewerValidationIndicators = new ViewerValidationIndicators();
    viewerValidationIndicators.setSuccess(passed);
    viewerValidationIndicators.setFailed(failed);
    viewerValidationIndicators.setWarnings(warnings);
    viewerValidationIndicators.setSkipped(skipped);

    return viewerValidationIndicators;
  }

  public static ViewerSiardConfiguration getSiardConfiguration(ViewerDatabase database) {
    ViewerSiardConfiguration configuration = new ViewerSiardConfiguration();
    configuration.setLocation(database.getPath());

    return configuration;
  }

  public static ViewerCollectionConfiguration getCollectionConfiguration(final String databaseUUID,
    final String solrCollectionName) {
    ViewerCollectionConfiguration configuration = new ViewerCollectionConfiguration();
    configuration.setVersion(ViewerConstants.COLLECTION_STATUS_VERSION);
    configuration.setDatabaseUUID(databaseUUID);
    configuration.setSolrCollectionPrefix(ViewerConstants.SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX);
    configuration.setId(solrCollectionName);

    return configuration;
  }
}
