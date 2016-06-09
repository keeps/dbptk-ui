package com.databasepreservation.dbviewer.client.browse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.v2.index.IsIndexed;

import com.databasepreservation.dbviewer.ViewerConstants;
import com.databasepreservation.dbviewer.client.BrowserService;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerRow;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerSchema;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerTable;
import com.databasepreservation.dbviewer.client.common.lists.TableRowList;
import com.databasepreservation.dbviewer.client.common.search.SearchField;
import com.databasepreservation.dbviewer.client.common.search.SearchFieldPanel;
import com.databasepreservation.dbviewer.client.common.search.SearchPanel;
import com.databasepreservation.dbviewer.client.common.utils.ListboxUtils;
import com.databasepreservation.dbviewer.client.main.BreadcrumbPanel;
import com.databasepreservation.dbviewer.shared.client.HistoryManager;
import com.databasepreservation.dbviewer.shared.client.Tools.BreadcrumbManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class TablePanel extends Composite {
  private static Map<String, TablePanel> instances = new HashMap<>();

  public static TablePanel getInstance(String databaseUUID, String tableUUID) {
    String separator = "/";
    String code = databaseUUID + separator + tableUUID;

    TablePanel instance = instances.get(code);
    if (instance == null) {
      instance = new TablePanel(databaseUUID, tableUUID);
      instances.put(code, instance);
    }

    return instance;
  }

  interface TablePanelUiBinder extends UiBinder<Widget, TablePanel> {
  }

  @UiField
  SimplePanel tableContainer;

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField
  SimplePanel searchContainer;
  SearchPanel searchPanel;

  @UiField(provided = true)
  SearchPanel dbSearchPanel;

  @UiField(provided = true)
  DatabaseSidebar sidebar;

  @UiField
  Label mainHeader;

  FlowPanel itemsSearchAdvancedFieldsPanel;

  private ViewerDatabase database;
  private ViewerSchema schema;
  private ViewerTable table;

  private TableRowList tableRowList;

  ListBox searchAdvancedFieldOptions;

  private final Map<String, SearchField> searchFields = new HashMap<String, SearchField>();

  private static final Filter DEFAULT_FILTER = new Filter(new BasicSearchFilterParameter(
    ViewerConstants.SOLR_ROW_SEARCH, "*"));

  private static TablePanelUiBinder uiBinder = GWT.create(TablePanelUiBinder.class);

  private TablePanel(final String databaseUUID, final String tableUUID) {
    dbSearchPanel = new SearchPanel(new Filter(), "", "Search in all tables", false, false);
    sidebar = DatabaseSidebar.getInstance(databaseUUID);

    itemsSearchAdvancedFieldsPanel = new FlowPanel();
    searchAdvancedFieldOptions = new ListBox();

    initWidget(uiBinder.createAndBindUi(this));

    BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.loadingTable(databaseUUID, tableUUID));

    itemsSearchAdvancedFieldsPanel.addStyleName("searchAdvancedFieldsPanel empty");

    BrowserService.Util.getInstance().retrieve(ViewerDatabase.class.getName(), databaseUUID,
      new AsyncCallback<IsIndexed>() {
        @Override
        public void onFailure(Throwable caught) {
          throw new RuntimeException(caught);
        }

        @Override
        public void onSuccess(IsIndexed result) {
          database = (ViewerDatabase) result;
          table = database.getMetadata().getTable(tableUUID);
          schema = database.getMetadata().getSchemaFromTableUUID(tableUUID);

          mainHeader.setText("Searching table `" + table.getName() + "`");
          mainHeader.removeStyleName("hidden");
          BreadcrumbManager.updateBreadcrumb(
            breadcrumb,
            BreadcrumbManager.forTable(database.getMetadata().getName(), databaseUUID, schema.getName(),
              schema.getUUID(), table.getName(), tableUUID));
          init();
        }
      });
  }

  private void init() {
    tableRowList = new TableRowList(database, table);

    searchPanel = new SearchPanel(new Filter(), ViewerConstants.SOLR_ROW_SEARCH, "Search...", false, true);
    searchPanel.setList(tableRowList);
    searchPanel.setDefaultFilterIncremental(true);
    showSearchAdvancedFieldsPanel();

    initAdvancedSearch(searchPanel);

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

  private void initAdvancedSearch(final SearchPanel searchPanel) {
    BrowserService.Util.getInstance().getSearchFields(table, new AsyncCallback<List<SearchField>>() {
      @Override
      public void onFailure(Throwable caught) {
        GWT.log("Error getting search fields", caught);
      }

      @Override
      public void onSuccess(List<SearchField> searchFields) {
        TablePanel.this.searchFields.clear();
        for (SearchField searchField : searchFields) {
          ListboxUtils.insertItemByAlphabeticOrder(searchAdvancedFieldOptions, searchField.getLabel(),
            searchField.getId());
          TablePanel.this.searchFields.put(searchField.getId(), searchField);
        }

        for (SearchField searchField : searchFields) {
          if (searchField.isFixed()) {
            SearchFieldPanel searchFieldPanel = new SearchFieldPanel();
            searchFieldPanel.setSearchAdvancedFields(searchAdvancedFieldOptions);
            searchFieldPanel.setSearchFields(TablePanel.this.searchFields);
            addSearchFieldPanel(searchFieldPanel);
            searchFieldPanel.selectSearchField(searchField.getId());
          }
        }

        searchPanel.addSearchAdvancedFieldAddHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            SearchFieldPanel searchFieldPanel = new SearchFieldPanel();
            searchFieldPanel.setSearchAdvancedFields(searchAdvancedFieldOptions);
            searchFieldPanel.setSearchFields(TablePanel.this.searchFields);
            searchFieldPanel.selectFirstSearchField();
            addSearchFieldPanel(searchFieldPanel);
          }
        });
      }
    });
  }

  public void showSearchAdvancedFieldsPanel() {
    searchPanel.setVariables(DEFAULT_FILTER, ViewerConstants.SOLR_ROW_SEARCH, tableRowList,
      itemsSearchAdvancedFieldsPanel);
    searchPanel.setSearchAdvancedFieldOptionsAddVisible(true);
  }
}
