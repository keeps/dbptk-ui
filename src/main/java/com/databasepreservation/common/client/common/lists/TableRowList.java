package com.databasepreservation.common.client.common.lists;

import static com.databasepreservation.common.client.models.structure.ViewerType.dbTypes.BINARY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.fusesource.restygwt.client.MethodCallback;
import org.roda.core.data.v2.index.sublist.Sublist;

import com.databasepreservation.common.client.ClientLogger;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.helpers.HelperExportTableData;
import com.databasepreservation.common.client.common.lists.utils.AsyncTableCell;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.common.utils.ExportResourcesUtils;
import com.databasepreservation.common.client.common.utils.TableRowListWrapper;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.index.facets.Facets;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.sort.Sorter;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerCell;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.models.structure.ViewerType;
import com.databasepreservation.common.client.services.DatabaseService;
import com.databasepreservation.common.client.tools.FilterUtils;
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

  private LinkedHashMap<ViewerColumn, Column<ViewerRow, ?>> columns;
  private Map<String, Boolean> columnDisplayNameToVisibleState = new HashMap<>();

  private CellTable<ViewerRow> display;

  private Sublist currentSubList;
  private Sorter currentSorter;
  private ViewerTable viewerTable;

  public TableRowList(ViewerDatabase database, ViewerTable table, Filter filter, Facets facets, String summary,
    boolean selectable, boolean exportable, CollectionStatus status, Boolean isNested) {
    super(filter, false, facets, summary, selectable, exportable,
      new TableRowListWrapper(database, table, status, isNested));
    this.viewerTable = table;
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
  protected boolean isColumnVisible(ViewerColumn column) {
    // NULL -> true (show)
    // true -> true (show)
    // false -> false (hide)
    Boolean visibleState = columnDisplayNameToVisibleState.get(column.getDisplayName());
    return visibleState == null || visibleState;
  }

  private int getColumnWithBinary(ViewerTable table) {
    int columnIndex = 0;
    for (ViewerColumn viewerColumn : table.getColumns()) {
      columnIndex++;
      final ViewerType viewerColumnType = viewerColumn.getType();
      ViewerType.dbTypes type = viewerColumnType.getDbType();
      if (type.equals(BINARY)) {
        return columnIndex - 1;
      }
    }
    return -1;
  }

  @Override
  protected void configureDisplay(CellTable<ViewerRow> display) {
    this.display = display;
    TableRowListWrapper wrapper = getObject();
    final ViewerTable table = wrapper.getTable();
    final ViewerDatabase database = wrapper.getDatabase();
    final CollectionStatus status = wrapper.getStatus();

    columns = new LinkedHashMap<>(table.getColumns().size());

    TableStatus tableStatus = status.getTableStatus(table.getUuid());

    final int columnWithBinary = getColumnWithBinary(table);
    if (columnWithBinary != -1) {
      final CellPreviewEvent.Handler<ViewerRow> selectionEventManager = DefaultSelectionEventManager
        .createBlacklistManager(columnWithBinary);
      display.setSelectionModel(getSelectionModel(), selectionEventManager);
    }

    int columnIndex = 0;

    for (ColumnStatus columnStatus : tableStatus.getColumns()) {
      if (!status.showColumn(tableStatus.getUuid(), columnStatus.getId()))
        continue;
      if (columnStatus.getNestedColumns().isEmpty()) {
        ViewerColumn viewerColumn = null;
        for (ViewerColumn column : table.getColumns()) {
          if (columnStatus.getId().equals(column.getSolrName())) {
            viewerColumn = column;
            break;
          }
        }
        if (viewerColumn == null)
          continue;

        final ViewerType viewerColumnType = viewerColumn.getType();
        final int thisColumnIndex = columnIndex++;
        final String solrColumnName = viewerColumn.getSolrName();
        final ViewerType.dbTypes type = viewerColumnType.getDbType();
        if (type.equals(BINARY)) {
          ViewerColumn finalViewerColumn = viewerColumn;
          Column<ViewerRow, SafeHtml> column = new Column<ViewerRow, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public void render(Cell.Context context, ViewerRow object, SafeHtmlBuilder sb) {
              SafeHtml value = getValue(object);
              if (value != null) {
                sb.appendHtmlConstant("<div title=\"" + messages.row_downloadLOB() + "\">");
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
              } else if (row.getCells().get(solrColumnName) != null) {
                final String value = row.getCells().get(solrColumnName).getValue();
                ret = SafeHtmlUtils
                  .fromTrustedString(CommonClientUtils.getAnchorForLOBDownload(database.getUuid(), table.getUuid(),
                    row.getUuid(), finalViewerColumn.getColumnIndexInEnclosingTable(), value).toString());
              }

              return ret;
            }
          };
          column.setSortable(viewerColumn.sortable());
          addColumn(viewerColumn, column);
          columns.put(viewerColumn, column);
        } else {
          Column<ViewerRow, SafeHtml> column = new Column<ViewerRow, SafeHtml>(new SafeHtmlCell()) {
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
              } else if (row.getCells().get(solrColumnName) != null) {
                ViewerType.dbTypes type = viewerColumnType.getDbType();
                String value = row.getCells().get(solrColumnName).getValue();

                // if it exists in Solr, it is not null
                switch (type) {
                  case BINARY:
                    // case DATETIME:
                    // ret =
                    // SafeHtmlUtils.fromString(JodaUtils.solrDateTimeDisplay(JodaUtils.solrDateParse(value)));
                    // break;
                    // case DATETIME_JUST_DATE:
                    // ret =
                    // SafeHtmlUtils.fromString(JodaUtils.solrDateDisplay(JodaUtils.solrDateParse(value)));
                    // break;
                    // case DATETIME_JUST_TIME:
                    // ret =
                    // SafeHtmlUtils.fromString(JodaUtils.solrTimeDisplay(JodaUtils.solrDateParse(value)));
                    // ret = SafeHtmlUtils.fromString(new Date().)
                    // break;
                  case BOOLEAN:
                  case ENUMERATION:
                  case TIME_INTERVAL:
                  case NUMERIC_FLOATING_POINT:
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
          column.setSortable(viewerColumn.sortable());
          addColumn(columnStatus, column);
          columns.put(viewerColumn, column);
        }
      } else {
        // this is the table of nested document
        ViewerTable nestedTable = database.getMetadata().getTableById(columnStatus.getId());
        List<ViewerColumn> columnsToAggregate = new ArrayList<>();
        for (String nestedColumn : columnStatus.getNestedColumns()) {
          for (ViewerColumn column : nestedTable.getColumns()) {
            if (column.getSolrName().equals(nestedColumn)) {
              columnsToAggregate.add(column);
            }
          }
        }

        Column<ViewerRow, SafeHtml> column = new Column<ViewerRow, SafeHtml>(new SafeHtmlCell()) {
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
            String aggregationColumn = null;
            SafeHtml ret = null;
            if (row.getNestedRowList() != null) {
              for (ViewerRow nestedRow : row.getNestedRowList()) {
                if (nestedRow == null || nestedRow.getCells() == null || nestedRow.getCells().isEmpty()) {
                  continue;
                } else if (nestedRow.getTableId().equals(nestedTable.getId())) {
                  Map<String, ViewerCell> cells = nestedRow.getCells();
                  String aggregationCell = null;
                  for (Map.Entry<String, ViewerCell> entry : cells.entrySet()) {
                    if (aggregationCell == null) {
                      aggregationCell = entry.getValue().getValue();
                    } else {
                      aggregationCell = aggregationCell + "," + entry.getValue().getValue();
                    }
                  }
                  if (aggregationColumn == null) {
                    aggregationColumn = aggregationCell;
                  } else {
                    aggregationColumn = aggregationColumn + ";" + aggregationCell;
                  }
                  ret = SafeHtmlUtils.fromString(aggregationColumn);
                }
              }
            }

            return ret;
          }
        };
        addColumn(columnStatus, column);
        for (ViewerColumn viewerColumn : columnsToAggregate) {
          columns.put(viewerColumn, column);
        }
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

  @Override
  protected void getData(Sublist sublist, ColumnSortList columnSortList,
    MethodCallback<IndexResult<ViewerRow>> callback) {
    TableRowListWrapper wrapper = getObject();
    ViewerTable table = wrapper.getTable();
    CollectionStatus status = wrapper.getStatus();
    Map<String, String> extraParameters = new HashMap<>();
    List<String> fieldsToReturn = new ArrayList<>();
    Filter filter;
    Boolean hasNested = false;
    if (wrapper.isNested()) {
      filter = getFilter();
    } else {
      filter = FilterUtils.filterByTable(getFilter(), table.getId());
    }
    for (ColumnStatus column : status.getTableStatus(table.getUuid()).getColumns()) {
      if (status.showColumn(table.getUuid(), column.getId())) {
        if (column.getNestedColumns().isEmpty()) {
          fieldsToReturn.add(column.getId());
        } else {
          hasNested = true;
        }
      }
    }
    // fieldsToReturn.add("*, [child limit=100]");
    if (hasNested) {
      buildNestedFieldsToReturn(wrapper, extraParameters, fieldsToReturn);
    }
    currentSubList = sublist;

    Map<Column<ViewerRow, ?>, List<String>> columnSortingKeyMap = new HashMap<>();

    for (Map.Entry<ViewerColumn, Column<ViewerRow, ?>> entry : columns.entrySet()) {
      ViewerColumn viewerColumn = entry.getKey();
      Column<ViewerRow, ?> column = entry.getValue();

      columnSortingKeyMap.put(column, Collections.singletonList(viewerColumn.getSolrName()));
    }

    currentSorter = createSorter(columnSortList, columnSortingKeyMap);

    GWT.log("Filter: " + filter);
    GWT.log("isNested: " + wrapper.isNested());

    FindRequest findRequest = new FindRequest(ViewerDatabase.class.getName(), filter, currentSorter, sublist,
      getFacets(), false, fieldsToReturn, extraParameters);

    DatabaseService.Util.call(callback).findRows(wrapper.getDatabase().getUuid(), wrapper.getDatabase().getUuid(), findRequest,
      LocaleInfo.getCurrentLocale().getLocaleName());
  }

  // TODO
  private void buildNestedFieldsToReturn(TableRowListWrapper wrapper, Map<String, String> extraParameters,
    List<String> fieldsToReturn) {
    StringBuilder sb = new StringBuilder();
    ViewerTable table = wrapper.getTable();
    CollectionStatus status = wrapper.getStatus();

    TableStatus tableStatus = status.getTableStatus(table.getUuid());
    fieldsToReturn.add(ViewerConstants.INDEX_ID);
    int nestedCount = 0;
    String keys = "";
    String separator = "";
    for (ColumnStatus column : tableStatus.getColumns()) {
      if (!column.getNestedColumns().isEmpty()) {
        String nestedTableId = column.getId();
        String key = ViewerConstants.SOLR_ROWS_NESTED + "." + nestedCount;
        keys = keys + separator  + key;
        separator = ",";
        fieldsToReturn.add(key + ":[subquery]");
        extraParameters.put(key + ".q", "+nestedTableId:" + nestedTableId + " AND {!terms f=_root_ v=$row.uuid}");
        extraParameters.put(key + ".rows", "10");
        nestedCount++;
      }
    }
    fieldsToReturn.add( ViewerConstants.SOLR_ROWS_NESTED + ":" + "\"" + keys + "\"");
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
    final List<ViewerColumn> binaryColumns = viewerTable.getBinaryColumns();
    for (ViewerColumn column : binaryColumns) {
      if (isColumnVisible(column)) {
        buildZipHelper = true;
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

    for (ViewerColumn viewerColumn : columns.keySet()) {
      Column<ViewerRow, ?> displayColumn = columns.get(viewerColumn);

      if (isColumnVisible(viewerColumn)) {
        addColumn(viewerColumn, displayColumn);
      }
    }
    handleScrollChanges();
  }

  private void addColumn(ColumnStatus viewerColumn, Column<ViewerRow, ?> displayColumn) {
    if (ViewerStringUtils.isNotBlank(viewerColumn.getDescription())) {
      SafeHtmlBuilder spanTitle = CommonClientUtils.constructSpan(viewerColumn.getCustomName(),
        viewerColumn.getDescription(), "column-description-block");
      SafeHtmlBuilder spanDescription = CommonClientUtils.constructSpan(viewerColumn.getDescription(),
        viewerColumn.getDescription(), "column-description-block column-description");
      addColumn(displayColumn, CommonClientUtils.wrapOnDiv(Arrays.asList(spanTitle, spanDescription)), true,
        TextAlign.LEFT, 10);
    } else {
      SafeHtmlBuilder spanTitle = CommonClientUtils.constructSpan(viewerColumn.getCustomName(),
        viewerColumn.getDescription(), "column-description-block");
      addColumn(displayColumn, spanTitle.toSafeHtml(), true, TextAlign.LEFT, 10);
    }
  }

  private void addColumn(ViewerColumn viewerColumn, Column<ViewerRow, ?> displayColumn) {
    if (ViewerStringUtils.isNotBlank(viewerColumn.getDescription())) {
      SafeHtmlBuilder spanTitle = CommonClientUtils.constructSpan(viewerColumn.getDisplayName(),
        viewerColumn.getDescription(), "column-description-block");
      SafeHtmlBuilder spanDescription = CommonClientUtils.constructSpan(viewerColumn.getDescription(),
        viewerColumn.getDescription(), "column-description-block column-description");
      addColumn(displayColumn, CommonClientUtils.wrapOnDiv(Arrays.asList(spanTitle, spanDescription)), true,
        TextAlign.LEFT, 10);
    } else {
      SafeHtmlBuilder spanTitle = CommonClientUtils.constructSpan(viewerColumn.getDisplayName(),
        viewerColumn.getDescription(), "column-description-block");
      addColumn(displayColumn, spanTitle.toSafeHtml(), true, TextAlign.LEFT, 10);
    }
  }

  private String getExportURL(String zipFilename, String filename, boolean exportAll, boolean description,
    boolean exportLobs) {
    TableRowListWrapper wrapper = getObject();
    ViewerDatabase database = wrapper.getDatabase();
    ViewerTable table = wrapper.getTable();

    // prepare parameter: field list
    List<String> solrColumns = new ArrayList<>();
    for (ViewerColumn viewerColumn : columns.keySet()) {
      if (isColumnVisible(viewerColumn)) {
        solrColumns.add(viewerColumn.getSolrName());
      }
    }
    // if all columns are hidden, export all
    if (solrColumns.isEmpty()) {
      for (ViewerColumn viewerColumn : table.getColumns()) {
        solrColumns.add(viewerColumn.getSolrName());
      }
    }

    Sublist sublist;

    if (!exportAll) {
      sublist = currentSubList;
    } else {
      sublist = null;
    }

    FindRequest findRequest = new FindRequest(ViewerRow.class.getName(), getFilter(), currentSorter, sublist,
      Facets.NONE, false, solrColumns);

    return ExportResourcesUtils.getExportURL(database.getUuid(), table.getUuid(), findRequest, zipFilename, filename, description, exportLobs, false);
  }

  private String getExportURL(String filename, boolean exportAll, boolean description) {
    return getExportURL(null, filename, exportAll, description, false);
  }
}
