package com.databasepreservation.visualization.client.common.lists;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.v2.index.IndexResult;

import com.databasepreservation.visualization.client.BrowserService;
import com.databasepreservation.visualization.client.SavedSearch;
import com.databasepreservation.visualization.shared.ViewerSafeConstants;
import com.databasepreservation.visualization.shared.client.ClientLogger;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SavedSearchList extends BasicAsyncTableCell<SavedSearch> {
  private final ClientLogger LOGGER = new ClientLogger(getClass().getName());

  private TextColumn<SavedSearch> nameColumn;
  private TextColumn<SavedSearch> descriptionColumn;
  private TextColumn<SavedSearch> dateAddedColumn;
  private TextColumn<SavedSearch> tableNameColumn;

  public SavedSearchList(Filter filter, Facets facets, String summary, boolean selectable, boolean exportable) {
    super(filter, facets, summary, selectable, exportable);
  }

  @Override
  protected void configureDisplay(CellTable<SavedSearch> display) {
    nameColumn = new TextColumn<SavedSearch>() {
      @Override
      public String getValue(SavedSearch savedSearch) {
        return savedSearch.getName();
      }
    };

    tableNameColumn = new TextColumn<SavedSearch>() {
      @Override
      public String getValue(SavedSearch savedSearch) {
        return savedSearch.getTableName();
      }
    };

    descriptionColumn = new TextColumn<SavedSearch>() {
      @Override
      public String getValue(SavedSearch savedSearch) {
        return savedSearch.getDescription();
      }
    };

    dateAddedColumn = new TextColumn<SavedSearch>() {
      @Override
      public String getValue(SavedSearch savedSearch) {
        return savedSearch.getDateAdded();
      }
    };

    nameColumn.setSortable(true);
    tableNameColumn.setSortable(true);
    descriptionColumn.setSortable(true);
    dateAddedColumn.setSortable(true);

    addColumn(nameColumn, "Search name", true, false, 15);
    addColumn(tableNameColumn, "Table", true, false, 15);
    addColumn(dateAddedColumn, "Created", true, false, 15);
    addColumn(descriptionColumn, "Description", true, false);

    Label emptyInfo = new Label("There are no saved searches.");
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
    AsyncCallback<IndexResult<SavedSearch>> callback) {
    Filter filter = getFilter();

    Map<Column<SavedSearch, ?>, List<String>> columnSortingKeyMap = new HashMap<>();
    columnSortingKeyMap.put(nameColumn, Arrays.asList(ViewerSafeConstants.SOLR_SEARCHES_NAME));
    columnSortingKeyMap.put(tableNameColumn, Arrays.asList(ViewerSafeConstants.SOLR_SEARCHES_TABLE_NAME));
    columnSortingKeyMap.put(dateAddedColumn, Arrays.asList(ViewerSafeConstants.SOLR_SEARCHES_NAME));
    columnSortingKeyMap.put(descriptionColumn, Arrays.asList(ViewerSafeConstants.SOLR_SEARCHES_NAME));

    Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

    GWT.log("Filter: " + filter);

    BrowserService.Util.getInstance().find(SavedSearch.class.getName(), filter, sorter, sublist, getFacets(),
      LocaleInfo.getCurrentLocale().getLocaleName(), callback);

  }

  @Override
  public void exportVisibleClickHandler() {

  }

  @Override
  public void exportAllClickHandler() {

  }
}
