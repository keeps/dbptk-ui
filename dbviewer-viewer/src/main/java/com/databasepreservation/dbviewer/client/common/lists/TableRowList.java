package com.databasepreservation.dbviewer.client.common.lists;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IndexResult;

import com.databasepreservation.dbviewer.client.BrowserService;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerColumn;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerRow;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerTable;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerType;
import com.databasepreservation.dbviewer.shared.client.ClientLogger;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class TableRowList extends AsyncTableCell<ViewerRow, Pair<ViewerDatabase, ViewerTable>> {
  private final ClientLogger logger = new ClientLogger(getClass().getName());

  private Map<ViewerColumn, Column<ViewerRow, ?>> columns;

  public TableRowList(ViewerDatabase database, ViewerTable table) {
    this(database, table, null, null, null, false);
  }

  public TableRowList(ViewerDatabase database, ViewerTable table, Filter filter, Facets facets, String summary,
    boolean selectable) {
    super(filter, false, facets, summary, selectable, new Pair<ViewerDatabase, ViewerTable>(database, table));
  }

  @Override
  protected void configureDisplay(CellTable<ViewerRow> display) {
    ViewerTable table = getObject().getSecond();
    columns = new HashMap<>(table.getColumns().size());

    int columnIndex = 0;
    for (ViewerColumn viewerColumn : table.getColumns()) {
      ViewerType viewerColumnType = viewerColumn.getType();
      final int thisColumnIndex = columnIndex++;
      final String solrColumnName = viewerColumn.getSolrName();

      Column<ViewerRow, SafeHtml> column = new Column<ViewerRow, SafeHtml>(new SafeHtmlCell()) {
        @Override
        public SafeHtml getValue(ViewerRow row) {
          SafeHtml ret;
          if (row == null) {
            logger.error("Trying to display a NULL ViewerRow");
            ret = null;
          } else if (row.getCells() == null) {
            logger.error("Trying to display NULL Cells");
            ret = null;
          } else if (row.getCells().get(solrColumnName) == null) {
            logger.error("Trying to display NULL value");
            ret = null;
          } else {
            ret = SafeHtmlUtils.fromString(row.getCells().get(solrColumnName).getValue());
          }
          return ret;
        }
      };
      column.setSortable(viewerColumn.sortable());
      //column.setCellStyleNames("nowrap");

      //display.addColumn(column, viewerColumn.getDisplayName());

      addColumn(column, viewerColumn.getDisplayName(), true, false, 50);

      columns.put(viewerColumn, column);
      //display.setColumnWidth(column, "100%");
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

    Map<Column<ViewerRow, ?>, List<String>> columnSortingKeyMap = new HashMap<>();

    for (Map.Entry<ViewerColumn, Column<ViewerRow, ?>> entry : columns.entrySet()) {
      ViewerColumn viewerColumn = entry.getKey();
      Column<ViewerRow, ?> column = entry.getValue();

      columnSortingKeyMap.put(column, Arrays.asList(viewerColumn.getSolrName()));
    }

    Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

    GWT.log("Filter: " + filter);

    BrowserService.Util.getInstance().findRows(ViewerRow.class.getName(), table.getUUID(), filter, sorter, sublist,
      getFacets(), LocaleInfo.getCurrentLocale().getLocaleName(), callback);
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    this.getSelectionModel().clear();
  }
}
