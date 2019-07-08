package com.databasepreservation.main.visualization.client.browse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.main.common.shared.client.common.RightPanel;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;

import com.databasepreservation.main.common.shared.ViewerConstants;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerRow;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.common.shared.client.common.LoadingDiv;
import com.databasepreservation.main.common.shared.client.common.utils.CommonClientUtils;
import com.databasepreservation.main.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.main.common.shared.client.tools.HistoryManager;
import com.databasepreservation.main.common.shared.client.tools.ViewerStringUtils;
import com.databasepreservation.main.common.shared.client.widgets.wcag.AccessibleFocusPanel;
import com.databasepreservation.main.common.shared.client.common.lists.TableRowList;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DatabaseSearchPanel extends RightPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, DatabaseSearchPanel> instances = new HashMap<>();

  public static DatabaseSearchPanel getInstance(ViewerDatabase database) {
    String code = database.getUUID();

    DatabaseSearchPanel instance = instances.get(code);
    if (instance == null) {
      instance = new DatabaseSearchPanel(database);
      instances.put(code, instance);
    }

    return instance;
  }

  interface DatabaseSearchPanelUiBinder extends UiBinder<Widget, DatabaseSearchPanel> {
  }

  private static DatabaseSearchPanelUiBinder uiBinder = GWT.create(DatabaseSearchPanelUiBinder.class);

  @UiField
  HTML mainHeader;

  @UiField
  FlowPanel content;

  @UiField
  HTML description;

  @UiField
  TextBox searchInputBox;

  @UiField
  FlowPanel searchPanel;

  @UiField
  AccessibleFocusPanel searchInputButton;

  @UiField
  Label noResults;

  @UiField
  LoadingDiv loading;

  final List<TableSearchPanelContainer> tableSearchPanelContainers;

  private ViewerDatabase database;

  private DatabaseSearchPanel(ViewerDatabase database) {
    tableSearchPanelContainers = new ArrayList<>();

    initWidget(uiBinder.createAndBindUi(this));

    Callback<TableSearchPanelContainer, Void> searchCompletedCallback = new Callback<TableSearchPanelContainer, Void>() {
      @Override
      public void onFailure(Void reason) {
        // do nothing. errors have already been handled
      }

      @Override
      public void onSuccess(TableSearchPanelContainer eventTriggerer) {

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
          noResults.setVisible(true);
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

    searchInputBox.addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          doSearch();
        }
      }
    });

    searchInputButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        doSearch();
      }
    });
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(breadcrumb,
      BreadcrumbManager.forDatabase(database.getMetadata().getName(), database.getUUID()));
  }

  private void doSearch() {
    // hide everything
    noResults.setVisible(false);
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

      header = CommonClientUtils.getSchemaAndTableHeader(database.getUUID(), table, "h3");
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

      tableRowList.addValueChangeHandler(new ValueChangeHandler<IndexResult<ViewerRow>>() {
        @Override
        public void onValueChange(ValueChangeEvent<IndexResult<ViewerRow>> event) {
          searchCompletedEventHandler(event);
        }
      });

      tableRowList.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
        @Override
        public void onSelectionChange(SelectionChangeEvent event) {
          ViewerRow record = tableRowList.getSelectionModel().getSelectedObject();
          if (record != null) {
            HistoryManager.gotoRecord(database.getUUID(), table.getUUID(), record.getUUID());
          }
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
