package com.databasepreservation.common.client.common.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.client.common.lists.TableRowList;
import com.databasepreservation.common.client.common.utils.AdvancedSearchUtils;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.services.CollectionService;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.tools.ViewerJsonUtils;
import com.github.nmorel.gwtjackson.client.exception.JsonDeserializationException;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class TableSearchPanel extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final Binder uiBinder = GWT.create(Binder.class);
  private static final String EMPTY = "empty";
  private CollectionStatus status;

  interface Binder extends UiBinder<Widget, TableSearchPanel> {
  }

  @UiField
  SimplePanel searchContainer;
  SearchPanel searchPanel;

  @UiField
  SimplePanel tableContainer;

  private TableRowList tableRowList;

  FlowPanel itemsSearchAdvancedFieldsPanel;

  private Filter initialFilter;

  private final Map<String, SearchField> searchFields = new HashMap<>();

  private final Map<String, Boolean> columnDisplayNameToVisibleState = new HashMap<>();

  private ViewerDatabase database;
  private ViewerTable table;

  private SearchInfo currentSearchInfo = null;

  /**
   * Table search panel with a SearchInfo predefined search
   */
  public TableSearchPanel(SearchInfo searchInfo, CollectionStatus status) {
    this.status = status;
    if (SearchInfo.isPresentAndValid(searchInfo)) {
      currentSearchInfo = searchInfo;
    }
  }

  /**
   * Table search panel with a SearchInfo (as JSON string) predefined search
   *
   * @param searchInfoJson
   */
  public TableSearchPanel(String searchInfoJson, CollectionStatus status) {
    this(status);
    setCurrentSearchInfoFromJson(searchInfoJson);
  }

  public SearchInfo getCurrentSearchInfo() {
    return currentSearchInfo;
  }

  /**
   * Table search panel without a predefined search
   */
  public TableSearchPanel(CollectionStatus status) {
    itemsSearchAdvancedFieldsPanel = new FlowPanel();
    this.status = status;

    initWidget(uiBinder.createAndBindUi(this));

    itemsSearchAdvancedFieldsPanel.addStyleName("searchAdvancedFieldsPanel empty");
  }

  public void provideSource(final ViewerDatabase database, final ViewerTable table) {
    provideSource(database, table, null, false);
  }

  public void provideSource(final ViewerDatabase database, final ViewerTable table, Filter initialFilter,
    Boolean isNested) {
    if (initialFilter == null) {
      initialFilter = new Filter();
    }
    this.initialFilter = initialFilter;
    this.database = database;
    this.table = table;

    tableRowList = new TableRowList(database, table, initialFilter, null, null, false, table.getCountRows() != 0,
      status, isNested);
    tableRowList.setColumnVisibility(columnDisplayNameToVisibleState);

    GWT.log("initial filter: " + initialFilter);

    final boolean showSearchAdvancedDisclosureButton = status.getTableStatusByTableId(table.getId())
      .showAdvancedSearchOption();

    if (isNested) {
      searchPanel = new SearchPanel(initialFilter, ViewerConstants.INDEX_SEARCH, messages.searchPlaceholder(),
        table.getName(), showSearchAdvancedDisclosureButton, new DefaultAsyncCallback<Void>() {
          @Override
          public void onSuccess(Void result) {
            TableSearchPanel.this.saveQuery();
          }
        });
    } else {
      searchPanel = new SearchPanel(initialFilter, ViewerConstants.INDEX_SEARCH, messages.searchPlaceholder(), false,
        showSearchAdvancedDisclosureButton, new DefaultAsyncCallback<Void>() {
          @Override
          public void onSuccess(Void result) {
            TableSearchPanel.this.saveQuery();
          }
        });
    }
    searchPanel.setList(tableRowList);
    searchPanel.setDefaultFilterIncremental(true);

    showSearchAdvancedFieldsPanel();

    tableRowList.getSelectionModel().addSelectionChangeHandler(event -> {
      ViewerRow record = tableRowList.getSelectionModel().getSelectedObject();
      if (record != null) {
        HistoryManager.gotoRecord(database.getUuid(), table.getId(), record.getUuid());
      }
    });

    searchContainer.setWidget(searchPanel);
    tableContainer.setWidget(tableRowList);

    if (currentSearchInfo == null) {
      initAdvancedSearch();
    } else {
      applyCurrentSearchInfo();
    }
  }

  public void setColumnVisibility(Map<String, Boolean> columnVisibility) {
    tableRowList.setColumnVisibility(columnVisibility);
    tableRowList.refreshColumnVisibility();
  }

  private void addSearchFieldPanel(final SearchFieldPanel searchFieldPanel) {
    itemsSearchAdvancedFieldsPanel.add(searchFieldPanel);
    itemsSearchAdvancedFieldsPanel.removeStyleName(EMPTY);

    searchPanel.setSearchAdvancedGoEnabled(true);
    searchPanel.setClearSearchButtonEnabled(true);
  }

  private void initAdvancedSearch() {
    final Map<String, List<SearchField>> searchFieldsFromTable = AdvancedSearchUtils.getSearchFieldsFromTableMap(table,
      status, database.getMetadata());
    TableSearchPanel.this.searchFields.clear();
    searchFieldsFromTable.forEach((key, value) -> {
      itemsSearchAdvancedFieldsPanel.add(CommonClientUtils.getAdvancedSearchDivider(key));
      buildSearchFieldPanel(value);
    });
  }

  private void buildSearchFieldPanel(List<SearchField> list) {
    list.forEach(searchField -> {
      if (searchField.isFixed()) {
        final SearchFieldPanel searchFieldPanel = new SearchFieldPanel();
        searchFieldPanel.setSearchField(searchField);
        addSearchFieldPanel(searchFieldPanel);
        searchFieldPanel.selectSearchField();
      }
    });
  }

  public void showSearchAdvancedFieldsPanel() {
    searchPanel.setVariables(initialFilter, ViewerConstants.INDEX_SEARCH, tableRowList, itemsSearchAdvancedFieldsPanel);
  }

  public void applySearchInfoJson(String searchInfoJson) {
    GWT.log("SearchInfo: " + searchInfoJson);
    setCurrentSearchInfoFromJson(searchInfoJson);
    applyCurrentSearchInfo();
  }

  public void applySearchInfoJson() {
    GWT.log("currentSearchInfo: " + currentSearchInfo);
    applyCurrentSearchInfo();
  }

  private void applyCurrentSearchInfo() {
    if (currentSearchInfo != null) {
      // clear existing advanced search fields
      this.searchPanel.openSearchAdvancedPanel();
      itemsSearchAdvancedFieldsPanel.clear();
      itemsSearchAdvancedFieldsPanel.addStyleName(EMPTY);

      // handle creating / editing search fields
      this.searchFields.clear();

      currentSearchInfo.getFields().forEach(searchField -> {
        if (searchField.isFixed()) {
          final SearchFieldPanel searchFieldPanel = new SearchFieldPanel();
          searchFieldPanel.setSearchField(searchField);
          addSearchFieldPanel(searchFieldPanel);
          searchFieldPanel.selectSearchField();
        }
      });

      // update search panel and trigger a search
      searchPanel.updateSearchPanel(currentSearchInfo);

      // show only visible columns
      columnDisplayNameToVisibleState.clear();
      tableRowList.refreshColumnVisibility();
    }
  }

  /**
   * Converts searchInfoJson to a SearchInfo object and if it is valid puts it
   * into currentSearchInfo
   *
   * @param searchInfoJson
   *          the SearchInfo object provided as Json
   */
  private void setCurrentSearchInfoFromJson(String searchInfoJson) {
    try {
      SearchInfo searchInfo = null;
      searchInfo = ViewerJsonUtils.getSearchInfoMapper().read(searchInfoJson);
      if (SearchInfo.isPresentAndValid(searchInfo)) {
        currentSearchInfo = searchInfo;
      } else {
        GWT.log("InvalidJson: Could not obtain SearchInfo from JSON string: " + searchInfoJson);
      }
    } catch (JsonDeserializationException e) {
      // if it fails, leave everything as is and log the error
      GWT.log("JsonDeserializationException: Could not obtain SearchInfo from JSON string: " + searchInfoJson);
    }
  }

  private SearchInfo createSearchInfo() {
    SearchInfo searchInfo = new SearchInfo();
    searchInfo.setDefaultFilter(searchPanel.getDefaultFilter());
    searchInfo.setCurrentFilter(searchPanel.getCurrentFilter());
    searchInfo.setFields(searchPanel.getAdvancedSearchSearchFields());
    searchInfo.setFieldParameters(searchPanel.getAdvancedSearchFilterParameters());
    return searchInfo;
  }

  public boolean isSearchInfoDefined() {
    return currentSearchInfo != null;
  }

  private void saveQuery() {
    SearchInfo searchInfo = createSearchInfo();
    CollectionService.Util
      .call((String savedSearchUUID) -> searchPanel.querySavedHandler(true, database, savedSearchUUID),
        (String errorMessage) -> searchPanel.querySavedHandler(false, database, null))
      .saveSavedSearch(database.getUuid(), database.getUuid(), table.getId(), messages.searchOnTable(table.getName()),
        "", searchInfo);
  }
}
