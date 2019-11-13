package com.databasepreservation.server.client.common.lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;

import com.databasepreservation.common.client.BrowserService;
import com.databasepreservation.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.common.shared.client.ClientLogger;
import com.databasepreservation.common.shared.client.common.lists.BasicAsyncTableCell;
import com.databasepreservation.common.shared.client.widgets.Alert;
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

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DatabaseList extends BasicAsyncTableCell<ViewerDatabase> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private final ClientLogger logger = new ClientLogger(getClass().getName());

  private Column<ViewerDatabase, SafeHtml> levelColumn;
  private Column<ViewerDatabase, SafeHtml> nameColumn;
  private Column<ViewerDatabase, SafeHtml> statusColumn;
  private Column<ViewerDatabase, SafeHtml> archivalDateColumn;
  private Column<ViewerDatabase, SafeHtml> dataOriginTimespan;
  private Column<ViewerDatabase, SafeHtml> shortID;
  private Column<ViewerDatabase, SafeHtml> description;

  public DatabaseList() {
    this(null, null, null, false, false);
  }

  public DatabaseList(Filter filter, Facets facets, String summary, boolean selectable, boolean exportable) {
    super(filter, facets, summary, selectable, exportable);
    autoUpdate(2000);
  }

  @Override
  protected void configureDisplay(CellTable<ViewerDatabase> display) {
    nameColumn = new TooltipDatabaseColumn() {
      @Override
      public SafeHtml getValue(ViewerDatabase database) {
        return database != null && database.getMetadata() != null
          ? SafeHtmlUtils.fromString(database.getMetadata().getName())
          : SafeHtmlUtils.fromString("unknown");
      }
    };

    archivalDateColumn = new TooltipDatabaseColumn() {
      @Override
      public SafeHtml getValue(ViewerDatabase database) {
        return database != null && database.getMetadata() != null
          ? SafeHtmlUtils.fromString(database.getMetadata().getArchivalDate().substring(0, 10))
          : null;
      }
    };

    dataOriginTimespan = new TooltipDatabaseColumn() {
      @Override
      public SafeHtml getValue(ViewerDatabase database) {
        return database != null && database.getMetadata() != null
          ? SafeHtmlUtils.fromString(database.getMetadata().getDataOriginTimespan())
          : null;
      }
    };

    shortID = new TooltipDatabaseColumn() {
      @Override
      public SafeHtml getValue(ViewerDatabase database) {
        return database != null ? SafeHtmlUtils.fromString(database.getUUID()) : null;
      }
    };

    statusColumn = new TooltipDatabaseColumn() {
      @Override
      public SafeHtml getValue(ViewerDatabase database) {
        return database != null ? SafeHtmlUtils.fromString(database.getStatus().toString()) : null;
      }
    };

    description = new TooltipDatabaseColumn() {
      @Override
      public SafeHtml getValue(ViewerDatabase database) {
        return database != null && database.getMetadata() != null
          ? SafeHtmlUtils.fromString(database.getMetadata().getDescription())
          : null;
      }
    };

    // nameColumn.setSortable(true);
    // archivalDateColumn.setSortable(true);
    // dataOriginTimespan.setSortable(true);
    // description.setSortable(true);

    addColumn(nameColumn, messages.managePageTableHeaderTextForDatabaseName(), true, TextAlign.LEFT, 30);
    addColumn(archivalDateColumn, messages.siardMetadata_archivalDate(), true, TextAlign.LEFT, 20);
    addColumn(dataOriginTimespan, messages.siardMetadata_dataOriginTimeSpan(), true, TextAlign.LEFT, 20);
    addColumn(shortID, messages.uniqueID(), true, TextAlign.LEFT, 20);
    addColumn(statusColumn, messages.managePageTableHeaderTextForDatabaseStatus(), true, TextAlign.LEFT, 20);
    addColumn(description, messages.description(), true, TextAlign.LEFT, 35);

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
    AsyncCallback<IndexResult<ViewerDatabase>> callback) {
    Filter filter = getFilter();

    Map<Column<ViewerDatabase, ?>, List<String>> columnSortingKeyMap = new HashMap<>();
    // columnSortingKeyMap.put(nameColumn, Arrays.asList("id"));
    // columnSortingKeyMap.put(archivalDateColumn,
    // Arrays.asList("archivalList"));

    Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

    GWT.log("Filter: " + filter);

    BrowserService.Util.getInstance().findDatabases(filter, sorter, sublist, getFacets(),
      LocaleInfo.getCurrentLocale().getLocaleName(), callback);
  }

  @Override
  public void exportClickHandler() {
    // do nothing
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

  @Override
  protected void onAttach() {
    super.onAttach();
    refresh();
  }
}
