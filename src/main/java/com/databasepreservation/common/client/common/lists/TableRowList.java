/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.lists;

import static com.databasepreservation.common.client.models.structure.ViewerType.dbTypes.BINARY;
import static com.databasepreservation.common.client.models.structure.ViewerType.dbTypes.CLOB;
import static com.databasepreservation.common.client.models.structure.ViewerType.dbTypes.NESTED;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.fusesource.restygwt.client.MethodCallback;
import org.roda.core.data.v2.index.sublist.Sublist;

import com.databasepreservation.common.client.ClientConfigurationManager;
import com.databasepreservation.common.client.ClientLogger;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.helpers.HelperExportTableData;
import com.databasepreservation.common.client.common.lists.utils.AsyncTableCell;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.common.utils.JavascriptUtils;
import com.databasepreservation.common.client.common.utils.TableRowListWrapper;
import com.databasepreservation.common.client.common.visualization.browse.configuration.handler.DataTransformationUtils;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.index.facets.Facets;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.sort.Sorter;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.LargeObjectConsolidateProperty;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerCell;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.services.CollectionService;
import com.databasepreservation.common.client.tools.FilterUtils;
import com.databasepreservation.common.client.tools.Humanize;
import com.databasepreservation.common.client.tools.JSOUtils;
import com.databasepreservation.common.client.tools.RestUtils;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.common.client.widgets.Alert;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.client.Window;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class TableRowList extends AsyncTableCell<ViewerRow, TableRowListWrapper> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private final ClientLogger logger = new ClientLogger(getClass().getName());

  private LinkedHashMap<ColumnStatus, Column<ViewerRow, ?>> configColumns;
  private Map<String, Boolean> columnDisplayNameToVisibleState = new HashMap<>();

  private CellTable<ViewerRow> display;

  private Sublist currentSubList;
  private Sorter currentSorter;
  private ViewerDatabase database;
  private ViewerTable viewerTable;
  private CollectionStatus collectionConfiguration;
  private final boolean showInUTC;

  public TableRowList(ViewerDatabase database, ViewerTable table, Filter filter, Facets facets, String summary,
    boolean selectable, boolean exportable, CollectionStatus status, Boolean isNested) {
    super(filter, false, facets, summary, selectable, exportable,
      new TableRowListWrapper(database, table, status, isNested));
    this.viewerTable = table;
    this.database = database;
    this.collectionConfiguration = status;
    this.showInUTC = ClientConfigurationManager.getBoolean(false, "ui.interface.show.datetime.utc");
  }

  public void setColumnVisibility(Map<String, Boolean> columnDisplayNameToVisibleState) {
    this.columnDisplayNameToVisibleState = columnDisplayNameToVisibleState;
  }

  /**
   * Checks if the column should be displayed. When uncertain, show it.
   *
   * @param column
   *          the column
   * @return the visible state
   */
  protected boolean isColumnVisible(String column) {
    // NULL -> true (show)
    // true -> true (show)
    // false -> false (hide)
    Boolean visibleState = columnDisplayNameToVisibleState.get(column);
    return visibleState == null || visibleState;
  }

  private Map<String, Integer> getColumnWithBinary(TableStatus table) {
    Map<String, Integer> binaryColumns = new HashMap<>();
    int index = 0;
    for (ColumnStatus configColumn : table.getVisibleColumnsList()) {
      if (configColumn.getType().equals(BINARY) || configColumn.getType().equals(CLOB)) {
        binaryColumns.put(configColumn.getId(), index);
      }
      index++;
    }

    return binaryColumns;
  }

  @Override
  protected void configureDisplay(CellTable<ViewerRow> display) {
    this.display = display;
    TableRowListWrapper wrapper = getObject();
    final ViewerTable table = wrapper.getTable();
    final ViewerDatabase database = wrapper.getDatabase();
    final CollectionStatus status = wrapper.getStatus();
    TableStatus tableStatus = status.getTableStatus(table.getUuid());

    configColumns = new LinkedHashMap<>(tableStatus.getVisibleColumnsList().size());

    final Map<String, Integer> columnWithBinary = getColumnWithBinary(tableStatus);
    if (!columnWithBinary.isEmpty()) {
      final CellPreviewEvent.Handler<ViewerRow> selectionEventManager = DefaultSelectionEventManager
        .createBlacklistManager(columnWithBinary.values().stream().mapToInt(Integer::intValue).toArray());
      display.setSelectionModel(getSelectionModel(), selectionEventManager);
    }

    for (ColumnStatus configColumn : tableStatus.getVisibleColumnsList()) {
      if (!NESTED.equals(configColumn.getType())) {
        // Treat as non nested
        if (BINARY.equals(configColumn.getType())) {
          Column<ViewerRow, SafeHtml> binaryColumn = buildDownloadColumn(configColumn, database, table,
            configColumn.getColumnIndex());
          binaryColumn.setSortable(true); // add to configuration file sortable options
          addColumn(configColumn, binaryColumn);
          configColumns.put(configColumn, binaryColumn);
        } else if (CLOB.equals(configColumn.getType())) {
          if (configColumn.getSearchStatus().getList().isShowContent()) {
            Column<ViewerRow, SafeHtml> column = buildSimpleColumn(configColumn);
            column.setSortable(true);
            addColumn(configColumn, column);
            configColumns.put(configColumn, column);
          } else {
            Column<ViewerRow, SafeHtml> binaryColumn = buildDownloadColumn(configColumn, database, table,
                configColumn.getColumnIndex());
            binaryColumn.setSortable(true); // add to configuration file sortable options
            addColumn(configColumn, binaryColumn);
            configColumns.put(configColumn, binaryColumn);
          }
        } else {
          Column<ViewerRow, SafeHtml> column = buildSimpleColumn(configColumn);
          column.setSortable(true);
          addColumn(configColumn, column);
          configColumns.put(configColumn, column);
        }
      } else {
        // Treat as nested
        // this is the table of nested document
        ViewerTable nestedTable = database.getMetadata()
          .getTableById(configColumn.getNestedColumns().getOriginalTable());

        Column<ViewerRow, SafeHtml> templateColumn = buildTemplateColumn(configColumn, nestedTable);
        templateColumn.setSortable(false);
        addColumn(configColumn, templateColumn);
        configColumns.put(configColumn, templateColumn);
      }
    }

    Alert alert = new Alert(Alert.MessageAlertType.LIGHT, messages.noItemsToDisplay());
    display.setEmptyTableWidget(alert);

    // define default sorting
    // display.getColumnSortList().push(new ColumnSortInfo(datesColumn, false));
    //
    // datesColumn.setCellStyleNames("nowrap");
    //
    // addStyleName("my-collections-table");
    // emptyInfo.addStyleName("my-collections-empty-info");
  }

  private Column<ViewerRow, SafeHtml> buildTemplateColumn(ColumnStatus configColumn, ViewerTable nestedTable) {
    return new Column<ViewerRow, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public void render(Cell.Context context, ViewerRow object, SafeHtmlBuilder sb) {
        SafeHtml value = getValue(object);
        if (value != null) {
          sb.appendHtmlConstant("<div title=\"" + SafeHtmlUtils.htmlEscape(value.asString()) + "\">");
          sb.append(value);
          sb.appendHtmlConstant("</div");
        }
      }

      @Override
      public SafeHtml getValue(ViewerRow row) {
        List<String> aggregationList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        SafeHtml ret = null;
        if (row.getNestedRowList() != null) {
          for (ViewerRow nestedRow : row.getNestedRowList()) {
            if (nestedRow != null && nestedRow.getCells() != null && !nestedRow.getCells().isEmpty()
              && nestedRow.getUuid().equals(configColumn.getId())) {
              Map<String, ViewerCell> cells = nestedRow.getCells();
              String template = configColumn.getSearchStatus().getList().getTemplate().getTemplate();
              if (template != null && !template.isEmpty()) {
                String json = JSOUtils.cellsToJson(cells, nestedTable);
                String s = JavascriptUtils.compileTemplate(template, json);
                aggregationList.add(s);
              }
            }
          }
          String separatorText = configColumn.getSearchStatus().getList().getTemplate().getSeparator();
          if (separatorText != null) {
            String separator = "";
            for (String s : aggregationList) {
              sb.append(separator);
              sb.append(s);
              separator = separatorText;
            }
            ret = SafeHtmlUtils.fromSafeConstant(sb.toString());
          } else {
            ret = SafeHtmlUtils.fromSafeConstant(messages.dataTransformationTableRowList(aggregationList));
          }
        }
        return ret;
      }
    };
  }

  private Column<ViewerRow, SafeHtml> buildSimpleColumn(ColumnStatus configColumn) {
    return new Column<ViewerRow, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public void render(Cell.Context context, ViewerRow object, SafeHtmlBuilder sb) {
        SafeHtml value = getValue(object);
        if (value != null) {
          sb.appendHtmlConstant("<div title=\"" + SafeHtmlUtils.htmlEscape(value.asString()) + "\">");
          sb.append(value);
          sb.appendHtmlConstant("</div");
        }
      }

      @Override
      public SafeHtml getValue(ViewerRow row) {
        SafeHtml ret = null;
        if (row == null) {
          logger.error("Trying to display a NULL ViewerRow");
        } else if (row.getCells() == null) {
          logger.error("Trying to display NULL Cells");
        } else if (row.getCells().get(configColumn.getId()) != null) {
          String value = row.getCells().get(configColumn.getId()).getValue();

          // if it exists in Solr, it is not null
          switch (configColumn.getType()) {
            case DATETIME:
              ret = SafeHtmlUtils.fromString(Humanize.formatDateTimeFromSolr(value, "yyyy-MM-dd HH:mm:ss", showInUTC));
              break;
            case DATETIME_JUST_DATE:
              ret = SafeHtmlUtils.fromString(Humanize.formatDateTimeFromSolr(value, "yyyy-MM-dd"));
              break;
            case DATETIME_JUST_TIME:
              ret = SafeHtmlUtils.fromString(Humanize.formatDateTimeFromSolr(value, "HH:mm:ss"));
              break;
            case NUMERIC_FLOATING_POINT:
              ret = SafeHtmlUtils.fromString(new BigDecimal(value).toPlainString());
              break;
            case BOOLEAN:
            case ENUMERATION:
            case TIME_INTERVAL:
            case NUMERIC_INTEGER:
            case COMPOSED_STRUCTURE:
            case COMPOSED_ARRAY:
            case STRING:
            default:
              ret = SafeHtmlUtils.fromString(value);
          }
        }
        return ret;
      }
    };
  }

  private Column<ViewerRow, SafeHtml> buildDownloadColumn(ColumnStatus configColumn, ViewerDatabase database,
    ViewerTable table, int columnIndex) {
    return new Column<ViewerRow, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public void render(Cell.Context context, ViewerRow object, SafeHtmlBuilder sb) {
        SafeHtml value = getValue(object);
        String title = messages.row_downloadLOB();
        if ((database.getPath() == null || database.getPath().isEmpty())
          && !collectionConfiguration.getConsolidateProperty().equals(LargeObjectConsolidateProperty.CONSOLIDATED)) {
          title = messages.rowPanelTextForLobUnavailable();
        }
        if (value != null) {
          sb.appendHtmlConstant("<div title=\"" + title + "\">");
          sb.append(value);
          sb.appendHtmlConstant("</div");
        }
      }

      @Override
      public SafeHtml getValue(ViewerRow row) {
        SafeHtml ret = null;
        if (row == null) {
          logger.error("Trying to display a NULL ViewerRow");
        } else if (row.getCells() == null) {
          logger.error("Trying to display NULL Cells");
        } else if (row.getCells().get(configColumn.getId()) != null) {
          if (collectionConfiguration.getConsolidateProperty().equals(LargeObjectConsolidateProperty.CONSOLIDATED)) {
            ret = getLobDownload(database, configColumn, table, row, columnIndex);
          } else {
            if (database.getPath() == null || database.getPath().isEmpty()) {
              ret = SafeHtmlUtils.fromTrustedString(messages.tablePanelTextForLobUnavailable());
            } else {
              ret = getLobDownload(database, configColumn, table, row, columnIndex);
            }
          }
        }
        return ret;
      }
    };
  }

  private SafeHtml getLobDownload(ViewerDatabase database, ColumnStatus configColumn, ViewerTable table, ViewerRow row,
    int columnIndex) {
    String template = configColumn.getSearchStatus().getList().getTemplate().getTemplate();
    if (template != null && !template.isEmpty()) {
      String json = JSOUtils.cellsToJson(ViewerConstants.TEMPLATE_LOB_DOWNLOAD_LABEL, messages.row_downloadLOB(),
        ViewerConstants.TEMPLATE_LOB_DOWNLOAD_LINK, RestUtils.createExportLobUri(database.getUuid(),
          table.getSchemaName(), table.getName(), row.getUuid(), columnIndex));
      return SafeHtmlUtils.fromSafeConstant(JavascriptUtils.compileTemplate(template, json));
    }

    return SafeHtmlUtils.fromSafeConstant("");
  }

  @Override
  protected void getData(Sublist sublist, ColumnSortList columnSortList,
    MethodCallback<IndexResult<ViewerRow>> callback) {
    TableRowListWrapper wrapper = getObject();
    ViewerTable table = wrapper.getTable();
    CollectionStatus status = wrapper.getStatus();
    Map<String, String> extraParameters = new HashMap<>();
    List<String> fieldsToReturn = new ArrayList<>();
    fieldsToReturn.add(ViewerConstants.INDEX_ID);
    Filter filter = getFilter();
    boolean hasNested = false;

    for (ColumnStatus column : status.getTableStatus(table.getUuid()).getVisibleColumnsList()) {
      if (column.getNestedColumns() != null) {
        hasNested = true;
      } else {
        fieldsToReturn.add(column.getId());
      }
    }

    if (hasNested) {
      DataTransformationUtils.buildNestedFieldsToReturn(wrapper.getTable(), wrapper.getStatus(), extraParameters,
        fieldsToReturn);
    }
    currentSubList = sublist;

    Map<Column<ViewerRow, ?>, List<String>> columnSortingKeyMap = new HashMap<>();

    for (Map.Entry<ColumnStatus, Column<ViewerRow, ?>> entry : configColumns.entrySet()) {
      ColumnStatus viewerColumn = entry.getKey();
      Column<ViewerRow, ?> column = entry.getValue();

      // if(!viewerColumn.getType().equals(NESTED)){
      columnSortingKeyMap.put(column, Collections.singletonList(viewerColumn.getId()));
      // }
    }

    currentSorter = createSorter(columnSortList, columnSortingKeyMap);

    FindRequest findRequest = new FindRequest(ViewerDatabase.class.getName(), filter, currentSorter, sublist,
      getFacets(), false, fieldsToReturn, extraParameters);

    if (!wrapper.isNested()) {
      FilterUtils.filterByTable(filter, table.getSchemaName() + "." + table.getName());
    }

    CollectionService.Util.call(callback).findRows(wrapper.getDatabase().getUuid(), wrapper.getDatabase().getUuid(),
      table.getSchemaName(), table.getName(), findRequest, LocaleInfo.getCurrentLocale().getLocaleName());
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    this.getSelectionModel().clear();
  }

  @Override
  public void exportClickHandler() {
    HelperExportTableData helperExportTableData = new HelperExportTableData(viewerTable, false);

    boolean buildZipHelper = false;

    if (database.getPath() != null && !database.getPath().isEmpty()) {
      Map<String, Integer> binaryColumns = getColumnWithBinary(
        getObject().getStatus().getTableStatusByTableId(viewerTable.getId()));
      for (String columnName : binaryColumns.keySet()) {
        if (isColumnVisible(columnName)) {
          buildZipHelper = true;
        }
      }
    }

    Dialogs.showCSVSetupDialog(messages.csvExportDialogTitle(), helperExportTableData.getWidget(buildZipHelper),
      messages.basicActionCancel(), messages.basicActionConfirm(), new DefaultAsyncCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean result) {
          if (result) {
            String filename = helperExportTableData.getFilename();
            boolean exportAll = helperExportTableData.exportAll();
            boolean exportDescription = helperExportTableData.exportDescription();

            if (helperExportTableData.isZipHelper()) {
              boolean exportLOBs = helperExportTableData.exportLobs();
              String zipFilename = helperExportTableData.getZipFileName();
              Window.Location.assign(getExportURL(zipFilename, filename, exportAll, exportDescription, exportLOBs));
            } else {
              Window.Location.assign(getExportURL(filename, exportAll, exportDescription));
            }
          }
        }
      });
  }

  public void refreshColumnVisibility() {
    int count = display.getColumnCount();
    for (int i = 0; i < count; i++) {
      display.removeColumn(0);
    }

    configColumns.keySet().forEach(configColumn -> {
      Column<ViewerRow, ?> displayColumn = configColumns.get(configColumn);
      if (isColumnVisible(configColumn.getName())) {
        addColumn(configColumn, displayColumn);
      }
    });
    handleScrollChanges();
  }

  private void addColumn(ColumnStatus viewerColumn, Column<ViewerRow, ?> displayColumn) {
    if (ViewerStringUtils.isNotBlank(viewerColumn.getCustomDescription())) {
      SafeHtmlBuilder spanTitle = CommonClientUtils.constructSpan(viewerColumn.getCustomName(),
        viewerColumn.getCustomDescription(), "column-description-block");
      SafeHtmlBuilder spanDescription = CommonClientUtils.constructSpan(viewerColumn.getCustomDescription(),
        viewerColumn.getCustomDescription(), "column-description-block column-description");
      addColumn(displayColumn, CommonClientUtils.wrapOnDiv(Arrays.asList(spanTitle, spanDescription)), true,
        TextAlign.LEFT, 10);
    } else {
      SafeHtmlBuilder spanTitle = CommonClientUtils.constructSpan(viewerColumn.getCustomName(),
        viewerColumn.getCustomDescription(), "column-description-block");
      addColumn(displayColumn, spanTitle.toSafeHtml(), true, TextAlign.LEFT, 10);
    }
  }

  private String getExportURL(String zipFilename, String filename, boolean exportAll, boolean description,
    boolean exportLobs) {
    TableRowListWrapper wrapper = getObject();
    ViewerDatabase database = wrapper.getDatabase();
    ViewerTable table = wrapper.getTable();
    CollectionStatus status = wrapper.getStatus();

    final List<ColumnStatus> visibleColumnsList = status.getTableStatusByTableId(table.getId()).getVisibleColumnsList();
    Map<String, String> extraParameters = new HashMap<>();
    boolean nested = false;

    // prepare parameter: field list
    List<String> fieldsToSolr = new ArrayList<>();
    List<String> fieldsToHeader = new ArrayList<>();
    for (ColumnStatus configColumn : visibleColumnsList) {
      if (isColumnVisible(configColumn.getName())) {
        if (!configColumn.getType().equals(NESTED)) {
          fieldsToSolr.add(configColumn.getId());
        }
        fieldsToHeader.add(configColumn.getId());
      }

      if (configColumn.getType().equals(NESTED) && !nested) {
        nested = true;
      }
    }

    if (nested) {
      DataTransformationUtils.buildNestedFieldsToReturn(wrapper.getTable(), wrapper.getStatus(), extraParameters,
        fieldsToSolr);
    }

    // if all columns are hidden, export all
    if (fieldsToSolr.isEmpty()) {
      for (ViewerColumn viewerColumn : table.getColumns()) {
        fieldsToSolr.add(viewerColumn.getSolrName());
      }
    }

    Sublist sublist;

    if (!exportAll) {
      sublist = currentSubList;
    } else {
      sublist = null;
    }

    FindRequest findRequest = new FindRequest(ViewerRow.class.getName(), getFilter(), currentSorter, sublist,
      Facets.NONE, false, fieldsToSolr, extraParameters);

    if (!nested && !wrapper.isNested()) {
      FilterUtils.filterByTable(findRequest.filter, table.getSchemaName() + "." + table.getName());
    }

    return RestUtils.createExportTableUri(database.getUuid(), table.getSchemaName(), table.getName(), findRequest,
      zipFilename, filename, description, exportLobs, fieldsToHeader);
  }

  private String getExportURL(String filename, boolean exportAll, boolean description) {
    return getExportURL(null, filename, exportAll, description, false);
  }
}
