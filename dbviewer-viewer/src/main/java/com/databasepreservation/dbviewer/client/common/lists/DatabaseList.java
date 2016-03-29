package com.databasepreservation.dbviewer.client.common.lists;

import java.util.Arrays;
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
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DatabaseList extends AsyncTableCell<ViewerDatabase> {
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
    levelColumn = new Column<ViewerDatabase, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(ViewerDatabase database) {
        SafeHtml ret;
        if (database == null) {
          logger.error("Trying to display a NULL item");
          ret = null;
        } else {
          // ret =
          // DescriptionLevelUtils.getElementLevelIconSafeHtml(database.getLevel());
          ret = SafeHtmlUtils.fromSafeConstant("someicon?");
        }
        return ret;
      }
    };

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

    levelColumn.setSortable(false);
    nameColumn.setSortable(true);
    archivalDateColumn.setSortable(true);

    // TODO externalize strings into constants
    display.addColumn(levelColumn, SafeHtmlUtils.fromSafeConstant("<i class='fa fa-tag'></i>"));
    display.addColumn(nameColumn, "Database name");
    display.addColumn(archivalDateColumn, "Archival date");
    display.setColumnWidth(levelColumn, "35px");
    display.setColumnWidth(nameColumn, "100%");
    display.setColumnWidth(nameColumn, "70px");
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
    columnSortingKeyMap.put(nameColumn, Arrays.asList("id"));
    columnSortingKeyMap.put(archivalDateColumn, Arrays.asList("archivalList"));

    Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

    GWT.log("Filter: " + filter);

    BrowserService.Util.getInstance().find(ViewerDatabase.class.getName(), filter, sorter, sublist, getFacets(),
      LocaleInfo.getCurrentLocale().getLocaleName(), callback);

  }
}
