package com.databasepreservation.visualization.client.common.lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;

import com.databasepreservation.visualization.client.BrowserService;
import com.databasepreservation.visualization.client.SavedSearch;
import com.databasepreservation.visualization.client.common.DefaultAsyncCallback;
import com.databasepreservation.visualization.shared.ViewerSafeConstants;
import com.databasepreservation.visualization.shared.client.ClientLogger;
import com.databasepreservation.visualization.shared.client.Tools.FontAwesomeIconManager;
import com.databasepreservation.visualization.shared.client.Tools.HistoryManager;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
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
    cells.add(new ActionsCell(messages.edit(), FontAwesomeIconManager.ACTION_EDIT,
      new FontAwesomeActionCell.Delegate<SavedSearch>() {
        @Override
        public void execute(SavedSearch object) {
          HistoryManager.gotoEditSavedSearch(object.getDatabaseUUID(), object.getUUID());
        }
      }));
    cells.add(new ActionsCell(messages.delete(), FontAwesomeIconManager.ACTION_DELETE, "btn-danger",
      new FontAwesomeActionCell.Delegate<SavedSearch>() {
        @Override
        public void execute(final SavedSearch object) {
          BrowserService.Util.getInstance().deleteSearch(getDatabaseUUID(), object.getUUID(),
            new DefaultAsyncCallback<Void>() {
              @Override
              public void onSuccess(Void result) {
                GWT.log("deleted " + object.getUUID());
                SavedSearchList.this.refresh();
              }
            });
        }
      }));
    CompositeCell<SavedSearch> compositeCell = new CompositeCell<>(cells);

    actionsColumn = new Column<SavedSearch, SavedSearch>(compositeCell) {
      @Override
      public SavedSearch getValue(SavedSearch object) {
        // return
        // SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.TABLE));
        return object;
      }
    };

    nameColumn.setSortable(true);
    tableNameColumn.setSortable(true);
    descriptionColumn.setSortable(true);
    dateAddedColumn.setSortable(true);
    actionsColumn.setSortable(false);

    addColumn(nameColumn, messages.name(), true, false, 15);
    addColumn(tableNameColumn, messages.table(), true, false, 15);
    addColumn(dateAddedColumn, messages.created(), true, false, 15);
    addColumn(descriptionColumn, messages.description(), true, false);
    addColumn(actionsColumn, messages.actions(), false, false, 6);

    Label emptyInfo = new Label(messages.thereAreNoSavedSearches());
    display.setEmptyTableWidget(emptyInfo);

    // define default sorting
    // display.getColumnSortList().push(new ColumnSortInfo(datesColumn, false));
    //
    // datesColumn.setCellStyleNames("nowrap");
    //
    // addStyleName("my-collections-table");
    // emptyInfo.addStyleName("my-collections-empty-info");

  }

  protected CellPreviewEvent.Handler<SavedSearch> getSelectionEventManager() {
    return DefaultSelectionEventManager.createBlacklistManager(4);
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

    BrowserService.Util.getInstance().findSavedSearches(getDatabaseUUID(), filter, sorter, sublist, getFacets(),
      LocaleInfo.getCurrentLocale().getLocaleName(), callback);

  }

  @Override
  public void exportVisibleClickHandler() {

  }

  @Override
  public void exportAllClickHandler() {

  }

  /**
   * Edit and delete cells, in a way that they can be added to a CompositeCell.
   * based on this code http://stackoverflow.com/a/9119496/1483200
   */
  private static class ActionsCell implements HasCell<SavedSearch, SavedSearch> {
    private FontAwesomeActionCell<SavedSearch> cell;

    public ActionsCell(String tooltip, String icon, String extraButtonClasses,
      FontAwesomeActionCell.Delegate<SavedSearch> delegate) {
      cell = new FontAwesomeActionCell<>(tooltip, icon, extraButtonClasses, delegate);
    }

    public ActionsCell(String tooltip, String icon, FontAwesomeActionCell.Delegate<SavedSearch> delegate) {
      cell = new FontAwesomeActionCell<>(tooltip, icon, delegate);
    }

    @Override
    public Cell<SavedSearch> getCell() {
      return cell;
    }

    @Override
    public FieldUpdater<SavedSearch, SavedSearch> getFieldUpdater() {
      return null;
    }

    @Override
    public SavedSearch getValue(SavedSearch object) {
      return object;
    }
  }
}
