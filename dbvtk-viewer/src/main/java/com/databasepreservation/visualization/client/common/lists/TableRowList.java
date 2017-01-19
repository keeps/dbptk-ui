package com.databasepreservation.visualization.client.common.lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;

import com.databasepreservation.visualization.client.BrowserService;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerColumn;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerRow;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerTable;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerType;
import com.databasepreservation.visualization.shared.ViewerSafeConstants;
import com.databasepreservation.visualization.shared.client.ClientLogger;
import com.databasepreservation.visualization.shared.client.Tools.FontAwesomeIconManager;
import com.databasepreservation.visualization.shared.client.Tools.ViewerJsonUtils;
import com.databasepreservation.visualization.shared.client.Tools.ViewerStringUtils;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class TableRowList extends AsyncTableCell<ViewerRow, Pair<ViewerDatabase, ViewerTable>> {
  private final ClientLogger logger = new ClientLogger(getClass().getName());

  private LinkedHashMap<ViewerColumn, Column<ViewerRow, ?>> columns;
  private Map<String, Boolean> columnDisplayNameToVisibleState = new HashMap<>();

  private CellTable<ViewerRow> display;

  private Sublist currentSubList;
  private Sorter currentSorter;

  private final ViewerDatabase database;

  public TableRowList(ViewerDatabase database, ViewerTable table, Filter filter, Facets facets, String summary,
    boolean selectable, boolean exportable) {
    super(filter, false, facets, summary, selectable, exportable, new Pair<>(database, table));
    this.database = database;
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

  @Override
  protected void configureDisplay(CellTable<ViewerRow> display) {
    this.display = display;
    final ViewerTable table = getObject().getSecond();
    columns = new LinkedHashMap<>(table.getColumns().size());

    int columnIndex = 0;
    for (ViewerColumn viewerColumn : table.getColumns()) {
      final ViewerType viewerColumnType = viewerColumn.getType();
      final int thisColumnIndex = columnIndex++;
      final String solrColumnName = viewerColumn.getSolrName();

      Column<ViewerRow, SafeHtml> column = new Column<ViewerRow, SafeHtml>(new SafeHtmlCell()) {
        /**
         * Render the object into the cell, providing the full content in a
         * tooltip
         *
         * @param context
         *          the cell context
         * @param object
         *          the object to render
         * @param sb
         *          the buffer to render into
         */
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
                ret = SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.BLOB,
                  "Large binary object"));
                break;
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

      addColumn(viewerColumn, column);
      columns.put(viewerColumn, column);
    }

    Label emptyInfo = new Label("No items to display");
    display.setEmptyTableWidget(emptyInfo);

    // define default sorting
    // display.getColumnSortList().push(new ColumnSortInfo(datesColumn, false));
    //
    // datesColumn.setCellStyleNames("nowrap");
    //
    // addStyleName("my-collections-table");
    // emptyInfo.addStyleName("my-collections-empty-info");
  }

  @Override
  protected void getData(Sublist sublist, ColumnSortList columnSortList, AsyncCallback<IndexResult<ViewerRow>> callback) {
    ViewerTable table = getObject().getSecond();
    Filter filter = getFilter();
    currentSubList = sublist;

    Map<Column<ViewerRow, ?>, List<String>> columnSortingKeyMap = new HashMap<>();

    for (Map.Entry<ViewerColumn, Column<ViewerRow, ?>> entry : columns.entrySet()) {
      ViewerColumn viewerColumn = entry.getKey();
      Column<ViewerRow, ?> column = entry.getValue();

      columnSortingKeyMap.put(column, Arrays.asList(viewerColumn.getSolrName()));
    }

    currentSorter = createSorter(columnSortList, columnSortingKeyMap);

    GWT.log("Filter: " + filter);

    BrowserService.Util.getInstance().findRows(database.getUUID(), table.getUUID(), filter, currentSorter, sublist,
      getFacets(), LocaleInfo.getCurrentLocale().getLocaleName(), callback);
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    this.getSelectionModel().clear();
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

  private void addColumn(ViewerColumn viewerColumn, Column<ViewerRow, ?> displayColumn) {
    if (ViewerStringUtils.isNotBlank(viewerColumn.getDescription())) {
      StringBuilder description = new StringBuilder("<span title=\"")
        .append(SafeHtmlUtils.fromString(viewerColumn.getDisplayName()).asString()).append(": ")
        .append(SafeHtmlUtils.fromString(viewerColumn.getDescription()).asString()).append("\">")
        .append(SafeHtmlUtils.fromString(viewerColumn.getDisplayName()).asString()).append("</span>");

      addColumn(displayColumn, SafeHtmlUtils.fromSafeConstant(description.toString()), true, false, 10);
    } else {
      StringBuilder description = new StringBuilder("<span title=\"")
        .append(SafeHtmlUtils.fromString(viewerColumn.getDisplayName()).asString()).append("\">")
        .append(SafeHtmlUtils.fromString(viewerColumn.getDisplayName()).asString()).append("</span>");

      addColumn(displayColumn, SafeHtmlUtils.fromSafeConstant(description.toString()), true, false, 10);
    }
  }

  public String getExportURL(boolean all) {
    ViewerDatabase database = getObject().getFirst();
    ViewerTable table = getObject().getSecond();

    // builds something like
    // http://hostname:port/api/v1/exports/csv/databaseUUID/tableUUID?
    StringBuilder urlBuilder = new StringBuilder();
    String base = com.google.gwt.core.client.GWT.getHostPageBaseURL();
    String servlet = ViewerSafeConstants.API_SERVLET;
    String resource = ViewerSafeConstants.API_V1_EXPORT_RESOURCE;
    String method = "/csv/";
    String databaseUUID = database.getUUID();
    String tableUUID = table.getUUID();
    String queryStart = "?";
    urlBuilder.append(base).append(servlet).append(resource).append(method).append(databaseUUID).append("/")
      .append(tableUUID).append(queryStart);

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

    // add parameter: field list
    String paramFieldList = ViewerJsonUtils.getStringListMapper().write(solrColumns);
    urlBuilder.append(ViewerSafeConstants.API_QUERY_PARAM_FIELDS).append("=").append(UriUtils.encode(paramFieldList))
      .append("&");

    // add parameter: filter
    String paramFilter = ViewerJsonUtils.getFilterMapper().write(getFilter());
    urlBuilder.append(ViewerSafeConstants.API_QUERY_PARAM_FILTER).append("=").append(UriUtils.encode(paramFilter))
      .append("&");

    // add parameter: subList
    String paramSubList;
    if (!all) {
      paramSubList = ViewerJsonUtils.getSubListMapper().write(currentSubList);
    } else {
      paramSubList = ViewerJsonUtils.getSubListMapper().write(new Sublist(0, Integer.MAX_VALUE));
    }
    urlBuilder.append(ViewerSafeConstants.API_QUERY_PARAM_SUBLIST).append("=").append(UriUtils.encode(paramSubList))
      .append("&");

    // add parameter: sorter
    String paramSorter = ViewerJsonUtils.getSorterMapper().write(currentSorter);
    urlBuilder.append(ViewerSafeConstants.API_QUERY_PARAM_SORTER).append("=").append(UriUtils.encode(paramSorter));

    GWT.log(urlBuilder.toString());

    return urlBuilder.toString();
  }

  @Override
  public void exportVisibleClickHandler() {
    Window.Location.assign(getExportURL(false));
  }

  @Override
  public void exportAllClickHandler() {
    Window.Location.assign(getExportURL(true));
  }
}
