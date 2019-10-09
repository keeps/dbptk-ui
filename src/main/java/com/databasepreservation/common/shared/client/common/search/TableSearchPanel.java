package com.databasepreservation.common.shared.client.common.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.v2.index.filter.Filter;

import com.databasepreservation.common.client.BrowserService;
import com.databasepreservation.common.shared.ViewerConstants;
import com.databasepreservation.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.common.shared.ViewerStructure.ViewerRow;
import com.databasepreservation.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.shared.client.common.lists.TableRowList;
import com.databasepreservation.common.shared.client.common.utils.ListboxUtils;
import com.databasepreservation.common.shared.client.tools.HistoryManager;
import com.databasepreservation.common.shared.client.tools.ViewerJsonUtils;
import com.github.nmorel.gwtjackson.client.exception.JsonDeserializationException;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class TableSearchPanel extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final Binder uiBinder = GWT.create(Binder.class);

  interface Binder extends UiBinder<Widget, TableSearchPanel> {
  }

  @UiField
  SimplePanel searchContainer;
  SearchPanel searchPanel;

  @UiField
  SimplePanel tableContainer;

  // @UiField
  // Button logJsonSearch;

  private TableRowList tableRowList;

  FlowPanel itemsSearchAdvancedFieldsPanel;
  ListBox searchAdvancedFieldOptions;

  private Filter initialFilter;

  private final Map<String, SearchField> searchFields = new HashMap<String, SearchField>();

  private final Map<String, Boolean> columnDisplayNameToVisibleState = new HashMap<>();

  private ViewerDatabase database;
  private ViewerTable table;

  private SearchInfo currentSearchInfo = null;

  /**
   * Table search panel with a SearchInfo predefined search
   */
  public TableSearchPanel(SearchInfo searchInfo) {
    this();
    if (SearchInfo.isPresentAndValid(searchInfo)) {
      currentSearchInfo = searchInfo;
    }
  }

  /**
   * Table search panel with a SearchInfo (as JSON string) predefined search
   * 
   * @param searchInfoJson
   */
  public TableSearchPanel(String searchInfoJson) {
    this();
    setCurrentSearchInfoFromJson(searchInfoJson);
  }

  /**
   * Table search panel without a predefined search
   */
  public TableSearchPanel() {
    itemsSearchAdvancedFieldsPanel = new FlowPanel();
    searchAdvancedFieldOptions = new ListBox();

    initWidget(uiBinder.createAndBindUi(this));

    // logJsonSearch.addClickHandler(new ClickHandler() {
    // @Override
    // public void onClick(ClickEvent event) {
    // GWT.log("----- BEGIN");
    // GWT.log(createSearchInfo().asJson());
    // GWT.log("----- encoded:");
    // GWT.log(createSearchInfo().asUrlEncodedJson());
    // GWT.log("----- END");
    // }
    // });

    itemsSearchAdvancedFieldsPanel.addStyleName("searchAdvancedFieldsPanel empty");
  }

  public void provideSource(final ViewerDatabase database, final ViewerTable table) {
    provideSource(database, table, null);
  }

  public void provideSource(final ViewerDatabase database, final ViewerTable table, Filter initialFilter) {
    if (initialFilter == null) {
      initialFilter = ViewerConstants.DEFAULT_FILTER;
    }
    this.initialFilter = initialFilter;
    this.database = database;
    this.table = table;

    tableRowList = new TableRowList(database, table, initialFilter, null, null, false, true);
    tableRowList.setColumnVisibility(columnDisplayNameToVisibleState);

    GWT.log("initial filter: " + initialFilter.toString());

    searchPanel = new SearchPanel(initialFilter, ViewerConstants.INDEX_SEARCH, messages.searchPlaceholder(),
      false, true, new DefaultAsyncCallback<Void>() {
        @Override
        public void onSuccess(Void result) {
          TableSearchPanel.this.saveQuery();
        }
      });
    searchPanel.setList(tableRowList);
    searchPanel.setDefaultFilterIncremental(true);
    showSearchAdvancedFieldsPanel();

    tableRowList.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        ViewerRow record = tableRowList.getSelectionModel().getSelectedObject();
        if (record != null) {
          HistoryManager.gotoRecord(database.getUUID(), table.getUUID(), record.getUUID());
        }
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

  private void addSearchFieldPanel(final SearchFieldPanel searchFieldPanel) {
    itemsSearchAdvancedFieldsPanel.add(searchFieldPanel);
    itemsSearchAdvancedFieldsPanel.removeStyleName("empty");

    searchPanel.setSearchAdvancedGoEnabled(true);

    ClickHandler clickHandler = new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        itemsSearchAdvancedFieldsPanel.remove(searchFieldPanel);
        if (itemsSearchAdvancedFieldsPanel.getWidgetCount() == 0) {
          itemsSearchAdvancedFieldsPanel.addStyleName("empty");
          searchPanel.setSearchAdvancedGoEnabled(false);
        }
      }
    };

    searchFieldPanel.addRemoveClickHandler(clickHandler);
  }

  private void initAdvancedSearch() {
    BrowserService.Util.getInstance().getSearchFields(table, new DefaultAsyncCallback<List<SearchField>>() {
      @Override
      public void onSuccess(List<SearchField> searchFields) {
        TableSearchPanel.this.searchFields.clear();
        for (SearchField searchField : searchFields) {
          ListboxUtils.insertItemByAlphabeticOrder(searchAdvancedFieldOptions, searchField.getLabel(),
            searchField.getId());
          TableSearchPanel.this.searchFields.put(searchField.getId(), searchField);
        }

        updateSearchFields(searchFields);
      }
    });
  }

  public void showSearchAdvancedFieldsPanel() {
    searchPanel.setVariables(initialFilter, ViewerConstants.INDEX_SEARCH, tableRowList,
      itemsSearchAdvancedFieldsPanel);
    searchPanel.setSearchAdvancedFieldOptionsAddVisible(true);
  }

  private void handleColumnVisibilityChanges(SearchFieldPanel searchFieldPanel, ValueChangeEvent<Boolean> event) {
    String columnDisplayName = searchFieldPanel.getSearchField().getLabel();
    columnDisplayNameToVisibleState.put(columnDisplayName, event.getValue());
    GWT.log("visible state changed: " + columnDisplayName + " is now " + event.getValue());
    tableRowList.refreshColumnVisibility();

    // update other references
    for (int i = 0; i < itemsSearchAdvancedFieldsPanel.getWidgetCount(); i++) {
      SearchFieldPanel otherSearchFieldPanel = (SearchFieldPanel) itemsSearchAdvancedFieldsPanel.getWidget(i);
      if (searchFieldPanel != otherSearchFieldPanel
        && searchFieldPanel.getSearchField().getLabel().equals(otherSearchFieldPanel.getSearchField().getLabel())) {
        otherSearchFieldPanel.setVisibilityCheckboxValue(searchFieldPanel.getVisibilityCheckboxValue(), false);
      }
    }
  }

  public void applySearchInfoJson(String searchInfoJson) {
    setCurrentSearchInfoFromJson(searchInfoJson);
    applyCurrentSearchInfo();
  }

  private void applyCurrentSearchInfo() {
    if (currentSearchInfo != null) {
      // clear existing advanced search fields
      this.searchPanel.openSearchAdvancedPanel();
      itemsSearchAdvancedFieldsPanel.clear();
      itemsSearchAdvancedFieldsPanel.addStyleName("empty");

      // handle creating / editing search fields
      this.searchFields.clear();
      for (SearchField searchField : currentSearchInfo.getFields()) {
        ListboxUtils.insertItemByAlphabeticOrder(searchAdvancedFieldOptions, searchField.getLabel(),
          searchField.getId());
        this.searchFields.put(searchField.getId(), searchField);
      }

      updateSearchFields(currentSearchInfo.getFields());

      // update search panel and trigger a search
      searchPanel.updateSearchPanel(currentSearchInfo);

      // update checkboxes
      for (int i = 0; i < itemsSearchAdvancedFieldsPanel.getWidgetCount(); i++) {
        SearchFieldPanel searchFieldPanel = (SearchFieldPanel) itemsSearchAdvancedFieldsPanel.getWidget(i);

        String columnDisplayName = searchFieldPanel.getSearchField().getLabel();
        Boolean visibility = currentSearchInfo.getFieldVisibility().get(columnDisplayName);
        if (visibility == null) {
          visibility = true;
        }

        searchFieldPanel.setVisibilityCheckboxValue(visibility, false);
      }

      // show only visible columns
      columnDisplayNameToVisibleState.clear();
      columnDisplayNameToVisibleState.putAll(currentSearchInfo.getFieldVisibility());
      tableRowList.refreshColumnVisibility();
    }
  }

  private void updateSearchFields(List<SearchField> newSearchFields) {
    for (SearchField searchField : newSearchFields) {
      if (searchField.isFixed()) {
        final SearchFieldPanel searchFieldPanel = new SearchFieldPanel(columnDisplayNameToVisibleState);
        searchFieldPanel.setSearchAdvancedFields(searchAdvancedFieldOptions);
        searchFieldPanel.setSearchFields(TableSearchPanel.this.searchFields);
        searchFieldPanel.setVisibilityChangedHandler(new ValueChangeHandler<Boolean>() {
          @Override
          public void onValueChange(ValueChangeEvent<Boolean> event) {
            handleColumnVisibilityChanges(searchFieldPanel, event);
          }
        });
        addSearchFieldPanel(searchFieldPanel);
        searchFieldPanel.selectSearchField(searchField.getId());
      }
    }

    searchPanel.addSearchAdvancedFieldAddHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        final SearchFieldPanel searchFieldPanel = new SearchFieldPanel(columnDisplayNameToVisibleState);
        searchFieldPanel.setSearchAdvancedFields(searchAdvancedFieldOptions);
        searchFieldPanel.setSearchFields(TableSearchPanel.this.searchFields);
        searchFieldPanel.setVisibilityChangedHandler(new ValueChangeHandler<Boolean>() {
          @Override
          public void onValueChange(ValueChangeEvent<Boolean> event) {
            handleColumnVisibilityChanges(searchFieldPanel, event);
          }
        });
        searchFieldPanel.selectFirstSearchField();
        addSearchFieldPanel(searchFieldPanel);
      }
    });
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
    searchInfo.setFieldVisibility(columnDisplayNameToVisibleState);
    return searchInfo;
  }

  public boolean isSearchInfoDefined() {
    return currentSearchInfo != null;
  }

  private void saveQuery() {
    SearchInfo currentSearchInfo = createSearchInfo();
    BrowserService.Util.getInstance().saveSearch(messages.searchOnTable(table.getName()), "", table.getUUID(),
      table.getName(), database.getUUID(), currentSearchInfo, new DefaultAsyncCallback<String>() {
        @Override
        public void onFailure(Throwable caught) {
          searchPanel.querySavedHandler(false, database, null);
          super.onFailure(caught);
        }

        @Override
        public void onSuccess(String savedSearchUUID) {
          searchPanel.querySavedHandler(true, database, savedSearchUUID);
        }
      });
  }
}
