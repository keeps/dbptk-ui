package com.databasepreservation.common.client.common.lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fusesource.restygwt.client.MethodCallback;
import org.roda.core.data.v2.index.sublist.Sublist;

import com.databasepreservation.common.client.ClientLogger;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.lists.cells.ActionsCell;
import com.databasepreservation.common.client.common.lists.utils.AsyncTableCell;
import com.databasepreservation.common.client.common.search.SavedSearch;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.index.facets.Facets;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.sort.Sorter;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.services.DatabaseService;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.widgets.Alert;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SavedSearchList extends AsyncTableCell<SavedSearch, String> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private final ClientLogger LOGGER = new ClientLogger(getClass().getName());

  private TextColumn<SavedSearch> nameColumn;
  private TextColumn<SavedSearch> descriptionColumn;
  private TextColumn<SavedSearch> dateAddedColumn;
  private TextColumn<SavedSearch> tableNameColumn;
  private Column<SavedSearch, SavedSearch> actionsColumn;

  public SavedSearchList(String databaseUUID, Filter filter, Facets facets, String summary, boolean selectable,
                         boolean exportable) {
    super(filter, false, facets, summary, selectable, exportable, databaseUUID);
  }

  private String getDatabaseUUID() {
    return getObject();
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

    // column with 2 buttons (edit and delete)
    ArrayList<HasCell<SavedSearch, ?>> cells = new ArrayList<>();
    cells.add(new ActionsCell<>(messages.edit(), FontAwesomeIconManager.ACTION_EDIT,
        object -> HistoryManager.gotoEditSavedSearch(object.getDatabaseUUID(), object.getUuid())));
    cells.add(new ActionsCell<>(messages.delete(), FontAwesomeIconManager.ACTION_DELETE, "btn-danger",
      object -> DatabaseService.Util.call((Void result) -> {
        GWT.log("deleted " + object.getUuid());
        SavedSearchList.this.refresh();
      }).deleteSavedSearch(getDatabaseUUID(), getDatabaseUUID(), object.getUuid())));
    CompositeCell<SavedSearch> compositeCell = new CompositeCell<>(cells);

    actionsColumn = new Column<SavedSearch, SavedSearch>(compositeCell) {
      @Override
      public SavedSearch getValue(SavedSearch object) {
        return object;
      }
    };

    nameColumn.setSortable(true);
    tableNameColumn.setSortable(true);
    descriptionColumn.setSortable(true);
    dateAddedColumn.setSortable(true);
    actionsColumn.setSortable(false);

    addColumn(nameColumn, messages.name(), true, TextAlign.LEFT, 15);
    addColumn(descriptionColumn, messages.description(), true, TextAlign.LEFT);
    addColumn(tableNameColumn, messages.table(), true, TextAlign.LEFT, 15);
    addColumn(dateAddedColumn, messages.created(), true, TextAlign.LEFT, 15);

    addColumn(actionsColumn, messages.managePageTableHeaderTextForActions(), false, TextAlign.LEFT, 6);

    Alert emptyInfo = new Alert(Alert.MessageAlertType.LIGHT, messages.noItemsToDisplay());
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
  protected CellPreviewEvent.Handler<SavedSearch> getSelectionEventManager() {
    return DefaultSelectionEventManager.createBlacklistManager(4);
  }

  @Override
  public void exportClickHandler() {
    // do nothing
  }

  @Override
  protected void getData(Sublist sublist, ColumnSortList columnSortList,
    MethodCallback<IndexResult<SavedSearch>> callback) {
    Filter filter = getFilter();

    Map<Column<SavedSearch, ?>, List<String>> columnSortingKeyMap = new HashMap<>();
    columnSortingKeyMap.put(nameColumn, Collections.singletonList(ViewerConstants.SOLR_SEARCHES_NAME));
    columnSortingKeyMap.put(tableNameColumn, Collections.singletonList(ViewerConstants.SOLR_SEARCHES_TABLE_NAME));
    columnSortingKeyMap.put(dateAddedColumn, Collections.singletonList(ViewerConstants.SOLR_SEARCHES_NAME));
    columnSortingKeyMap.put(descriptionColumn, Collections.singletonList(ViewerConstants.SOLR_SEARCHES_NAME));

    Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

    GWT.log("Filter: " + filter);

    FindRequest findRequest = new FindRequest(ViewerDatabase.class.getName(), filter, sorter, sublist, getFacets());
    DatabaseService.Util.call(callback).findSavedSearches(getDatabaseUUID(), getDatabaseUUID(), findRequest,
      LocaleInfo.getCurrentLocale().getLocaleName());
  }
}
