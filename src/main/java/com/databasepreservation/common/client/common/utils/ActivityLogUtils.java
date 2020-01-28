package com.databasepreservation.common.client.common.utils;

import static com.databasepreservation.common.client.ViewerConstants.CONTROLLER_ACTIVITY_LOG_RESOURCE;
import static com.databasepreservation.common.client.ViewerConstants.CONTROLLER_COLLECTION_RESOURCE;
import static com.databasepreservation.common.client.ViewerConstants.CONTROLLER_DATABASE_RESOURCE;
import static com.databasepreservation.common.client.ViewerConstants.CONTROLLER_FILE_RESOURCE;
import static com.databasepreservation.common.client.ViewerConstants.CONTROLLER_JOB_RESOURCE;
import static com.databasepreservation.common.client.ViewerConstants.CONTROLLER_SIARD_RESOURCE;
import static com.databasepreservation.common.client.ViewerConstants.CONTROLLER_USER_LOGIN_CONTROLLER;

import java.util.Map;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.fields.GenericField;
import com.databasepreservation.common.client.common.fields.RowField;
import com.databasepreservation.common.client.common.search.SearchInfo;
import com.databasepreservation.common.client.common.utils.html.FilterHtmlUtils;
import com.databasepreservation.common.client.common.utils.html.SearchInfoHtmlUtils;
import com.databasepreservation.common.client.common.utils.html.SublistHtmlUtils;
import com.databasepreservation.common.client.models.activity.logs.ActivityLogWrapper;
import com.databasepreservation.common.client.models.activity.logs.PresenceState;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.tools.ViewerJsonUtils;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ActivityLogUtils {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static FlowPanel getParametersHTML(ActivityLogWrapper entry) {
    switch (entry.getActivityLogEntry().getActionComponent()) {
      case CONTROLLER_ACTIVITY_LOG_RESOURCE:
        return getLogParameters(entry);
      case CONTROLLER_DATABASE_RESOURCE:
        return getDatabaseParameters(entry);
      case CONTROLLER_FILE_RESOURCE:
        return getFileParameters(entry);
      case CONTROLLER_USER_LOGIN_CONTROLLER:
        return getLoginParameters(entry);
      case CONTROLLER_COLLECTION_RESOURCE:
        return getCollectionParameters(entry);
      case CONTROLLER_SIARD_RESOURCE:
        return getSiardParameters(entry);
      case CONTROLLER_JOB_RESOURCE:
        return getJobParameters(entry);
      default:
        break;
    }

    return new FlowPanel();
  }

  private static FlowPanel getLogParameters(ActivityLogWrapper wrapper) {
    FlowPanel panel = new FlowPanel();
    if (wrapper.getActivityLogEntry().getActionMethod().equals("retrieve")) {
      final SafeHtml tagSafeHtml = FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.ACTIVITY_LOG,
        messages.activityLogViewedLog());
      Hyperlink hyperlink = new Hyperlink(tagSafeHtml, HistoryManager
        .linkToLog(wrapper.getActivityLogEntry().getParameters().get(ViewerConstants.CONTROLLER_LOG_ID_PARAM)));
      hyperlink.addStyleName("btn btn-link");

      panel.add(RowField.createInstance(messages.activityLogDatabaseRelated(), hyperlink));
    } else if (wrapper.getActivityLogEntry().getActionMethod().equals("find")) {
      handleFilterInfo(panel, wrapper);
      handleSublistInfo(panel, wrapper);
    }

    return panel;
  }

  private static FlowPanel getDatabaseParameters(ActivityLogWrapper wrapper) {
    FlowPanel panel = new FlowPanel();
    switch (wrapper.getActivityLogEntry().getActionMethod()) {
      case "findDatabases":
        handleFilterInfo(panel, wrapper);
        handleSublistInfo(panel, wrapper);
        return panel;
      case "retrieve":
      case "deleteDatabase":
        handleDatabaseInfo(panel, wrapper);
        break;
      case "create":
        handleFilenameInfo(panel, wrapper, messages.activityLogFilenameRelated(),
          ViewerConstants.CONTROLLER_FILENAME_PARAM);
    }
    return panel;
  }

  private static FlowPanel getCollectionParameters(ActivityLogWrapper wrapper) {
    FlowPanel panel = new FlowPanel();
    switch (wrapper.getActivityLogEntry().getActionMethod()) {
      case "exportLOB":
        handleDatabaseInfo(panel, wrapper);
        handleTableInfo(panel, wrapper);
        handleRowInfo(panel, wrapper);
        handleColumnInfo(panel, wrapper);
        handleFilenameInfo(panel, wrapper, messages.activityLogFilenameRelated(),
          ViewerConstants.CONTROLLER_FILENAME_PARAM);
        return panel;
      case "exportFindToCSV":
        handleDatabaseInfo(panel, wrapper);
        handleTableInfo(panel, wrapper);
        handleExportOptions(panel, wrapper, false);
        return panel;
      case "exportSingleRowToCSV":
        handleDatabaseInfo(panel, wrapper);
        handleTableInfo(panel, wrapper);
        handleExportOptions(panel, wrapper, true);
        return panel;
      case "findDatabases":
        handleFilterInfo(panel, wrapper);
        handleSublistInfo(panel, wrapper);
        return panel;
      case "findRows":
        handleDatabaseInfo(panel, wrapper);
        handleFilterInfo(panel, wrapper);
        handleSublistInfo(panel, wrapper);
        return panel;
      case "createCollection":
      case "getProgressData":
      case "getReport":
      case "getCollectionConfiguration":
      case "retrieve":
      case "deleteCollection":
      case "updateCollectionConfiguration":
      case "deleteSavedSearch":
        handleDatabaseInfo(panel, wrapper);
        break;
      case "getDenormalizeConfigurationFile":
      case "createDenormalizeConfigurationFile":
      case "deleteDenormalizeConfigurationFile":
      case "run":
        handleDatabaseInfo(panel, wrapper);
        handleTableInfo(panel, wrapper);
        break;
      case "retrieveRow":
        handleDatabaseInfo(panel, wrapper);
        handleRowInfo(panel, wrapper);
        break;
      case "updateSavedSearch":
        handleDatabaseInfo(panel, wrapper);
        handleSavedSearchInfo(panel, wrapper);
        handleSavedSearchEdit(panel, wrapper);
        break;
      case "findSavedSearches":
        handleDatabaseInfo(panel, wrapper);
        handleSublistInfo(panel, wrapper);
        break;
      case "retrieveSavedSearch":
        handleDatabaseInfo(panel, wrapper);
        handleSavedSearchInfo(panel, wrapper);
        break;
      case "saveSavedSearch":
        handleDatabaseInfo(panel, wrapper);
        handleTableInfo(panel, wrapper);
        handleSavedSearchEdit(panel, wrapper);
        handleSearchInfo(panel, wrapper);
        break;
    }
    return panel;
  }

  private static FlowPanel getSiardParameters(ActivityLogWrapper wrapper) {
    FlowPanel panel = new FlowPanel();

    switch (wrapper.getActivityLogEntry().getActionMethod()) {
      case "getValidationProgressData":
      case "deleteSIARDFile":
      case "deleteValidationReport":
      case "updateMetadataInformation":
      case "getMetadataInformation":
      case "validateSiard":
      case "getValidationReportFile":
        handleDatabaseInfo(panel, wrapper);
        break;
    }
    return panel;
  }

  private static FlowPanel getFileParameters(ActivityLogWrapper wrapper) {
    FlowPanel panel = new FlowPanel();

    switch (wrapper.getActivityLogEntry().getActionMethod()) {
      case "createSIARDFile":
      case "getSIARDFile":
      case "deleteSiardFile":
        panel.add(
          RowField.createInstance(messages.activityLogPathRelated(), new HTML(messages.activityLogFilenameParameter(
            wrapper.getActivityLogEntry().getParameters().get(ViewerConstants.CONTROLLER_FILENAME_PARAM)))));
        break;
    }
    return panel;
  }

  private static FlowPanel getJobParameters(ActivityLogWrapper wrapper) {
    FlowPanel panel = new FlowPanel();
    handleFilterInfo(panel, wrapper);
    return panel;
  }

  private static FlowPanel getLoginParameters(ActivityLogWrapper wrapper) {
    final Map<String, String> parameters = wrapper.getActivityLogEntry().getParameters();
    final String username = parameters.get(ViewerConstants.CONTROLLER_USERNAME_PARAM);

    FlowPanel panel = new FlowPanel();

    if (ViewerStringUtils.isNotBlank(username)) {
      SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
      safeHtmlBuilder.append(SafeHtmlUtils.fromSafeConstant(username));
      panel.add(RowField.createInstance(messages.activityLogUsernameRelated(), new HTML(safeHtmlBuilder.toSafeHtml())));
    }
    return panel;
  }

  private static void handleDatabaseInfo(FlowPanel panel, ActivityLogWrapper wrapper) {
    if (wrapper.getDatabasePresence().equals(PresenceState.YES)) {
      final SafeHtml tagSafeHtml = FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.DATABASE,
        wrapper.getDatabase().getMetadata().getName());
      Hyperlink hyperlink = new Hyperlink(tagSafeHtml, HistoryManager.linkToSIARDInfo(wrapper.getDatabase().getUuid()));
      hyperlink.addStyleName("btn btn-link");

      panel.add(RowField.createInstance(messages.activityLogDatabaseRelated(), hyperlink));
    } else if (wrapper.getDatabasePresence().equals(PresenceState.NO)) {
      panel.add(RowField.createInstance(messages.activityLogDatabaseRelated(),
        new HTML(messages.activityLogDatabaseDeleted())));
    }
  }

  private static void handleFilterInfo(FlowPanel panel, ActivityLogWrapper wrapper) {
    if (wrapper.getFilterPresence().equals(PresenceState.YES)) {
      final HTML html = new HTML(FilterHtmlUtils.getFilterHTML(wrapper.getFilter()));
      html.addStyleName("display-inline-grid metadata-information-element-value");
      final GenericField field = GenericField.createInstance("Search parameters", html);
      field.setCSSMetadata("row-field", "metadata-information-element-label");
      panel.add(field);
    }
  }

  private static void handleSublistInfo(FlowPanel panel, ActivityLogWrapper wrapper) {
    if (wrapper.getSublistPresence().equals(PresenceState.YES)) {
      long count = Long
        .parseLong(wrapper.getActivityLogEntry().getParameters().get(ViewerConstants.CONTROLLER_RETRIEVE_COUNT));
      panel.add(SublistHtmlUtils.getSublistHTML(wrapper.getSublist(), count));
    }
  }

  private static void handleRowInfo(FlowPanel panel, String databaseUUID, String tableUUID, String rowUUID) {
    panel.add(getRecordHyperLink(databaseUUID, tableUUID, rowUUID));
  }

  private static void handleRowInfo(FlowPanel panel, ActivityLogWrapper wrapper) {
    if (wrapper.getRowPresence().equals(PresenceState.YES)) {
      panel.add(
        getRecordHyperLink(wrapper.getDatabase().getUuid(), wrapper.getRow().getTableId(), wrapper.getRow().getUuid()));
    }
  }

  private static RowField getRecordHyperLink(String databaseUUID, String tableId, String rowUUID) {
    final SafeHtml tagSafeHtml = FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.RECORD, "Row");
    GWT.log(tableId);
    Hyperlink hyperlink = new Hyperlink(tagSafeHtml, HistoryManager.linkToRecord(databaseUUID, tableId, rowUUID));
    hyperlink.addStyleName("btn btn-link");

    return RowField.createInstance(messages.activityLogRecordRelated(), hyperlink);
  }

  private static void handleTableInfo(FlowPanel panel, ActivityLogWrapper wrapper) {
    if (wrapper.getTablePresence().equals(PresenceState.YES)) {
      final SafeHtml tagSafeHtml = FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.TABLE,
        wrapper.getTable().getName());
      Hyperlink hyperlink = new Hyperlink(tagSafeHtml, HistoryManager.linkToTable(wrapper.getDatabase().getUuid(),
        wrapper.getTable().getSchemaName(), wrapper.getTable().getName()));
      hyperlink.addStyleName("btn btn-link");

      panel.add(RowField.createInstance(messages.activityLogTableRelated(), hyperlink));
    }
  }

  private static void handleExportOptions(FlowPanel panel, ActivityLogWrapper wrapper, boolean singleRecord) {
    if (singleRecord) {
      final String rowUUID = wrapper.getActivityLogEntry().getParameters().get(ViewerConstants.CONTROLLER_ROW_ID_PARAM);
      handleRowInfo(panel, wrapper.getDatabase().getUuid(), wrapper.getTable().getId(), rowUUID);
    }

    final String zipFilename = wrapper.getActivityLogEntry().getParameters()
      .get(ViewerConstants.CONTROLLER_ZIP_FILENAME_PARAM);
    final String filename = wrapper.getActivityLogEntry().getParameters()
      .get(ViewerConstants.CONTROLLER_FILENAME_PARAM);
    final String exportDescriptions = wrapper.getActivityLogEntry().getParameters()
      .get(ViewerConstants.CONTROLLER_EXPORT_DESCRIPTIONS_PARAM);
    final String exportLobs = wrapper.getActivityLogEntry().getParameters()
      .get(ViewerConstants.CONTROLLER_EXPORT_LOBS_PARAM);

    if (!singleRecord) {
      panel.add(RowField.createInstance(messages.activityLogLabelForExportType(),
        new HTML(SafeHtmlUtils.fromSafeConstant(messages.activityLogTextForExportTypeTable()))));
    } else {
      panel.add(RowField.createInstance(messages.activityLogLabelForExportType(),
        new HTML(SafeHtmlUtils.fromSafeConstant(messages.activityLogTextForExportTypeRow()))));
    }

    if (ViewerStringUtils.isNotBlank(filename)) {
      panel.add(RowField.createInstance(messages.csvExportDialogLabelForFilename(),
        new HTML(SafeHtmlUtils.fromSafeConstant(filename))));
    }

    if (ViewerStringUtils.isNotBlank(zipFilename)) {
      panel.add(RowField.createInstance(messages.csvExportDialogLabelForZipFilename(),
        new HTML(SafeHtmlUtils.fromSafeConstant(zipFilename))));
    }

    if (exportDescriptions.equalsIgnoreCase(Boolean.TRUE.toString())) {
      panel.add(RowField.createInstance(messages.csvExportDialogLabelForExportHeaderWithDescriptions(),
        new HTML(SafeHtmlUtils.fromSafeConstant(messages.yes()))));
    } else {
      panel.add(RowField.createInstance(messages.csvExportDialogLabelForExportHeaderWithDescriptions(),
        new HTML(SafeHtmlUtils.fromSafeConstant(messages.no()))));
    }

    if (exportLobs.equalsIgnoreCase(Boolean.TRUE.toString())) {
      panel.add(RowField.createInstance(messages.csvExportDialogLabelForExportLOBs(),
        new HTML(SafeHtmlUtils.fromSafeConstant(messages.yes()))));
    } else {
      panel.add(RowField.createInstance(messages.csvExportDialogLabelForExportLOBs(),
        new HTML(SafeHtmlUtils.fromSafeConstant(messages.no()))));
    }
  }

  private static void handleColumnInfo(FlowPanel panel, ActivityLogWrapper wrapper) {
    if (wrapper.getColumnPresence().equals(PresenceState.YES)) {
      panel.add(RowField.createInstance(messages.columnName(),
        new HTML(SafeHtmlUtils.fromSafeConstant(wrapper.getColumnName()))));
    }
  }

  private static void handleFilenameInfo(FlowPanel panel, ActivityLogWrapper wrapper, String label, String constant) {
    final String filename = wrapper.getActivityLogEntry().getParameters().get(constant);
    if (filename != null) {
      panel.add(RowField.createInstance(label, new HTML(SafeHtmlUtils.fromSafeConstant(filename))));
    }
  }

  private static void handleSavedSearchInfo(FlowPanel panel, ActivityLogWrapper wrapper) {
    if (wrapper.getSavedSearchPresence().equals(PresenceState.YES)) {
      final SafeHtml tagSafeHtml = FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.SAVED_SEARCH,
        wrapper.getSavedSearch().getName());
      Hyperlink hyperlink = new Hyperlink(tagSafeHtml, HistoryManager
        .linkToSavedSearch(wrapper.getSavedSearch().getDatabaseUUID(), wrapper.getSavedSearch().getUuid()));
      hyperlink.addStyleName("btn btn-link");
      panel.add((RowField.createInstance(messages.activityLogSavedSearchRelated(), hyperlink)));
    } else if (wrapper.getSavedSearchPresence().equals(PresenceState.NO)) {
      panel.add(RowField.createInstance(messages.activityLogSavedSearchRelated(),
        new HTML(SafeHtmlUtils.fromSafeConstant(messages.activityLogSavedSearchDeleted()))));
    }
  }

  private static void handleSavedSearchEdit(FlowPanel panel, ActivityLogWrapper wrapper) {
    final String name = wrapper.getActivityLogEntry().getParameters()
      .get(ViewerConstants.CONTROLLER_SAVED_SEARCH_NAME_PARAM);
    final String description = wrapper.getActivityLogEntry().getParameters()
      .get(ViewerConstants.CONTROLLER_SAVED_SEARCH_DESCRIPTION_PARAM);

    if (ViewerStringUtils.isNotBlank(name)) {
      panel.add(
        RowField.createInstance(messages.activityLogSavedSearchName(), new HTML(SafeHtmlUtils.fromSafeConstant(name))));
    }

    if (ViewerStringUtils.isNotBlank(description)) {
      panel.add(RowField.createInstance(messages.activityLogSavedSearchDescription(),
        new HTML(SafeHtmlUtils.fromSafeConstant(description))));
    }
  }

  private static void handleSearchInfo(FlowPanel panel, ActivityLogWrapper wrapper) {
    if (wrapper.getSavedSearchPresence().equals(PresenceState.YES)) {
      final String searchInfoJson = wrapper.getSavedSearch().getSearchInfoJson();
      final SearchInfo searchInfo = ViewerJsonUtils.getSearchInfoMapper().read(searchInfoJson);
      panel.add(RowField.createInstance(messages.activityLogSearchInfoRelated(),
        new HTML(SearchInfoHtmlUtils.getSearchInfoHtml(searchInfo))));
    }
  }
}
