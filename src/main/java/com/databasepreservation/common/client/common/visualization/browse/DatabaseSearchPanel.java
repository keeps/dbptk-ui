package com.databasepreservation.common.client.common.visualization.browse;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerSchema;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.LoadingDiv;
import com.databasepreservation.common.client.common.RightPanel;
import com.databasepreservation.common.client.common.lists.TableRowList;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.common.client.widgets.Alert;
import com.databasepreservation.common.client.widgets.wcag.AccessibleFocusPanel;
import com.databasepreservation.common.client.index.IndexResult;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DatabaseSearchPanel extends RightPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, DatabaseSearchPanel> instances = new HashMap<>();

  public static DatabaseSearchPanel getInstance(ViewerDatabase database) {
    String code = database.getUuid();
    instances.computeIfAbsent(code, k -> new DatabaseSearchPanel(database));
    return instances.get(code);
  }

  interface DatabaseSearchPanelUiBinder extends UiBinder<Widget, DatabaseSearchPanel> {
  }

  private static DatabaseSearchPanelUiBinder uiBinder = GWT.create(DatabaseSearchPanelUiBinder.class);

  @UiField
  Label title;

  @UiField
  FlowPanel content;

  @UiField
  HTML description;

  @UiField
  TextBox searchInputBox;

  @UiField
  FlowPanel searchPanel;

  @UiField
  FlowPanel noResultsContent;

  @UiField
  AccessibleFocusPanel searchInputButton;

  @UiField(provided = true)
  Alert noResults;

  @UiField
  LoadingDiv loading;

  private final List<TableSearchPanelContainer> tableSearchPanelContainers;

  private ViewerDatabase database;

  private DatabaseSearchPanel(ViewerDatabase database) {
    tableSearchPanelContainers = new ArrayList<>();
    noResults = new Alert(Alert.MessageAlertType.INFO, messages.noRecordsMatchTheSearchTerms());

    initWidget(uiBinder.createAndBindUi(this));
    title.setText(messages.searchAllRecords());

    Callback<TableSearchPanelContainer, Void> searchCompletedCallback = new Callback<TableSearchPanelContainer, Void>() {
      @Override
      public void onFailure(Void reason) {
        // do nothing. errors have already been handled
      }

      @Override
      public void onSuccess(TableSearchPanelContainer eventTriggered) {

        boolean foundRecords = false;
        for (TableSearchPanelContainer table : tableSearchPanelContainers) {
          if (table.stillSearching()) {
            return;
          }
          foundRecords = foundRecords || table.foundRecords();
        }

        // all searches finished
        loading.setVisible(false);
        if (!foundRecords) {
          // and no records were found.
          noResultsContent.setVisible(true);
        }
      }
    };

    for (ViewerSchema viewerSchema : database.getMetadata().getSchemas()) {
      for (ViewerTable viewerTable : viewerSchema.getTables()) {
        TableSearchPanelContainer tableSearchPanelContainer = new TableSearchPanelContainer(database, viewerTable,
          searchCompletedCallback);
        tableSearchPanelContainers.add(tableSearchPanelContainer);
        content.add(tableSearchPanelContainer);
      }
    }

    this.database = database;

    searchInputBox.getElement().setPropertyString("placeholder", messages.searchPlaceholder());

    searchInputBox.addKeyDownHandler(event -> {
      if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
        doSearch();
      }
    });

    searchInputButton.addClickHandler(event -> doSearch());
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
      BreadcrumbManager.updateBreadcrumb(breadcrumb,
          BreadcrumbManager.forDatabaseSearchPanel(database.getUuid(), database.getMetadata().getName()));
  }

  private void doSearch() {
    // hide everything
    noResultsContent.setVisible(false);
    loading.setVisible(true);
    for (TableSearchPanelContainer tableSearchPanelContainer : tableSearchPanelContainers) {
      tableSearchPanelContainer.hideEverything();
    }

    // start searching
    Filter filter;
    String searchText = searchInputBox.getText();
    if (ViewerStringUtils.isBlank(searchText)) {
      filter = ViewerConstants.DEFAULT_FILTER;
    } else {
      filter = new Filter(new BasicSearchFilterParameter(ViewerConstants.INDEX_SEARCH, searchText));
    }

    for (TableSearchPanelContainer tableSearchPanelContainer : tableSearchPanelContainers) {
      tableSearchPanelContainer.doSearch(filter);
    }

  }

  private static class TableSearchPanelContainer extends FlowPanel {
    private final Widget header;
    private final SimplePanel tableContainer;
    private TableRowList tableRowList;
    private final ViewerDatabase database;
    private final ViewerTable table;
    private final Callback<TableSearchPanelContainer, Void> searchCompletedCallback;
    private boolean foundRecords = false;
    private boolean stillSearching = false;

    public TableSearchPanelContainer(ViewerDatabase database, ViewerTable table,
      Callback<TableSearchPanelContainer, Void> searchCompletedEvent) {
      super();
      this.database = database;
      this.table = table;
      this.searchCompletedCallback = searchCompletedEvent;

      tableContainer = new SimplePanel();
      tableContainer.setVisible(false);

      header = CommonClientUtils.getHeader(table, "h3", database.getMetadata().getSchemas().size() > 1);
      header.setVisible(false);

      add(header);
      add(tableContainer);
    }

    public void init(Filter filter) {
      if (filter == null) {
        filter = ViewerConstants.DEFAULT_FILTER;
      }

      tableRowList = new TableRowList(database, table, filter, null, null, false, false);

      tableContainer.setWidget(tableRowList);

      tableRowList.addValueChangeHandler(this::searchCompletedEventHandler);

      tableRowList.getSelectionModel().addSelectionChangeHandler(event -> {
        ViewerRow record = tableRowList.getSelectionModel().getSelectedObject();
        if (record != null) {
          HistoryManager.gotoRecord(database.getUuid(), table.getUUID(), record.getUuid());
        }
      });
    }

    private void searchCompletedEventHandler(ValueChangeEvent<IndexResult<ViewerRow>> event) {
      long resultCount = event.getValue().getTotalCount();
      GWT.log(table.getName() + " got " + resultCount + " results");
      hideLoading(resultCount > 0);
      searchCompletedCallback.onSuccess(this);
    }

    private void hideEverything() {
      header.setVisible(false);
      tableContainer.setVisible(false);
      stillSearching = true;
      foundRecords = false;
    }

    private void hideLoading(boolean andShowTheTable) {
      header.setVisible(andShowTheTable);
      tableContainer.setVisible(andShowTheTable);
      foundRecords = andShowTheTable;
      stillSearching = false;
    }

    void doSearch(Filter filter) {
      if (tableRowList == null) {
        init(filter);
      } else {
        tableRowList.setFilter(filter);
      }
    }

    boolean stillSearching() {
      return stillSearching;
    }

    boolean foundRecords() {
      return foundRecords;
    }
  }
}
