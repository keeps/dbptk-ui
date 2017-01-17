package com.databasepreservation.visualization.client.common.lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;

import com.databasepreservation.visualization.client.BrowserService;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.shared.client.ClientLogger;
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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DatabaseList extends BasicAsyncTableCell<ViewerDatabase> {
  private final ClientLogger logger = new ClientLogger(getClass().getName());

  private Column<ViewerDatabase, SafeHtml> levelColumn;
  private Column<ViewerDatabase, SafeHtml> nameColumn;
  private Column<ViewerDatabase, SafeHtml> archivalDateColumn;
  private Column<ViewerDatabase, SafeHtml> dataOriginTimespan;
  private Column<ViewerDatabase, SafeHtml> shortID;
  private Column<ViewerDatabase, SafeHtml> description;

  public DatabaseList() {
    this(null, null, null, false, false);
  }

  public DatabaseList(Filter filter, Facets facets, String summary, boolean selectable, boolean exportable) {
    super(filter, facets, summary, selectable, exportable);
  }

  @Override
  protected void configureDisplay(CellTable<ViewerDatabase> display) {
    nameColumn = new TooltipDatabaseColumn() {
      @Override
      public SafeHtml getValue(ViewerDatabase database) {
        return database != null ? SafeHtmlUtils.fromString(database.getMetadata().getName()) : null;
      }
    };

    archivalDateColumn = new TooltipDatabaseColumn() {
      @Override
      public SafeHtml getValue(ViewerDatabase database) {
        return database != null ? SafeHtmlUtils.fromString(database.getMetadata().getArchivalDate().substring(0, 10))
          : null;
      }
    };

    dataOriginTimespan = new TooltipDatabaseColumn() {
      @Override
      public SafeHtml getValue(ViewerDatabase database) {
        return database != null ? SafeHtmlUtils.fromString(database.getMetadata().getDataOriginTimespan()) : null;
      }
    };

    shortID = new TooltipDatabaseColumn() {
      @Override
      public SafeHtml getValue(ViewerDatabase database) {
        return database != null ? SafeHtmlUtils.fromString(database.getUUID()) : null;
      }
    };

    description = new TooltipDatabaseColumn() {
      @Override
      public SafeHtml getValue(ViewerDatabase database) {
        return database != null ? SafeHtmlUtils.fromString(database.getMetadata().getDescription()) : null;
      }
    };

    // nameColumn.setSortable(true);
    // archivalDateColumn.setSortable(true);
    // dataOriginTimespan.setSortable(true);
    // description.setSortable(true);

    addColumn(nameColumn, "Database name", true, false, 30);
    addColumn(archivalDateColumn, "Archival date", true, false, 20);
    addColumn(dataOriginTimespan, "Data origin time span", true, false, 20);
    addColumn(shortID, "Unique ID", true, false, 20);
    addColumn(description, "Description", true, false, 35);

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

    Map<Column<ViewerDatabase, ?>, List<String>> columnSortingKeyMap = new HashMap<>();
    // columnSortingKeyMap.put(nameColumn, Arrays.asList("id"));
    // columnSortingKeyMap.put(archivalDateColumn,
    // Arrays.asList("archivalList"));

    Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

    GWT.log("Filter: " + filter);

    BrowserService.Util.getInstance().find(ViewerDatabase.class.getName(), filter, sorter, sublist, getFacets(),
      LocaleInfo.getCurrentLocale().getLocaleName(), callback);

  }

  @Override
  public void exportVisibleClickHandler() {

  }

  @Override
  public void exportAllClickHandler() {

  }

  private abstract static class TooltipDatabaseColumn extends Column<ViewerDatabase, SafeHtml> {
    public TooltipDatabaseColumn() {
      super(new SafeHtmlCell());
    }

    @Override
    public void render(Cell.Context context, ViewerDatabase object, SafeHtmlBuilder sb) {
      SafeHtml value = getValue(object);
      if (value != null) {
        sb.appendHtmlConstant("<div title=\"" + SafeHtmlUtils.htmlEscape(value.asString()) + "\">");
        sb.append(value);
        sb.appendHtmlConstant("</div");
      }
    }
  }
}
