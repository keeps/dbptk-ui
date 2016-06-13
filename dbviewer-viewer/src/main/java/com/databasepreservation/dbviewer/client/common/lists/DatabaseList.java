package com.databasepreservation.dbviewer.client.common.lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.v2.index.IndexResult;

import com.databasepreservation.dbviewer.client.BrowserService;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.dbviewer.shared.client.ClientLogger;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DatabaseList extends BasicAsyncTableCell<ViewerDatabase> {
  private final ClientLogger logger = new ClientLogger(getClass().getName());

  private Column<ViewerDatabase, SafeHtml> levelColumn;
  private TextColumn<ViewerDatabase> nameColumn;
  private TextColumn<ViewerDatabase> archivalDateColumn;

  public DatabaseList() {
    this(null, null, null, false);
  }

  public DatabaseList(Filter filter, Facets facets, String summary, boolean selectable) {
    super(filter, facets, summary, selectable);
  }

  @Override
  protected void configureDisplay(CellTable<ViewerDatabase> display) {
    nameColumn = new TextColumn<ViewerDatabase>() {
      @Override
      public String getValue(ViewerDatabase database) {
        return database != null ? database.getMetadata().getName() : null;
      }
    };

    archivalDateColumn = new TextColumn<ViewerDatabase>() {
      @Override
      public String getValue(ViewerDatabase database) {
        return database != null ? database.getMetadata().getArchivalDate().substring(0, 10) : null;
      }
    };

    nameColumn.setSortable(true);
    archivalDateColumn.setSortable(true);

    addColumn(nameColumn, "Database name", true, false);
    addColumn(archivalDateColumn, "Archival date", true, false, 20);

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
  protected void getData(Sublist sublist, ColumnSortList columnSortList,
    AsyncCallback<IndexResult<ViewerDatabase>> callback) {
    Filter filter = getFilter();

    Map<Column<ViewerDatabase, ?>, List<String>> columnSortingKeyMap = new HashMap<Column<ViewerDatabase, ?>, List<String>>();
    // columnSortingKeyMap.put(levelColumn, Arrays.asList("level"));
    // columnSortingKeyMap.put(nameColumn, Arrays.asList("id"));
    // columnSortingKeyMap.put(archivalDateColumn,
    // Arrays.asList("archivalList"));

    Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

    GWT.log("Filter: " + filter);

    BrowserService.Util.getInstance().find(ViewerDatabase.class.getName(), filter, sorter, sublist, getFacets(),
      LocaleInfo.getCurrentLocale().getLocaleName(), callback);

  }

  @Override
  protected void onLoad() {
    super.onLoad();
    this.getSelectionModel().clear();
  }
}
