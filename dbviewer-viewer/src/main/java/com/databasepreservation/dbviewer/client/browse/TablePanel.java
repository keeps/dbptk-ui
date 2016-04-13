package com.databasepreservation.dbviewer.client.browse;

import java.util.HashMap;
import java.util.Map;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.v2.index.IsIndexed;

import com.databasepreservation.dbviewer.ViewerConstants;
import com.databasepreservation.dbviewer.client.BrowserService;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerTable;
import com.databasepreservation.dbviewer.client.common.lists.TableRowList;
import com.databasepreservation.dbviewer.client.common.search.SearchPanel;
import com.databasepreservation.dbviewer.client.main.BreadcrumbPanel;
import com.databasepreservation.dbviewer.shared.client.Tools.BreadcrumbManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

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

  private ViewerDatabase database;
  private ViewerTable table;

  private TableRowList tableRowList;

  private static TablePanelUiBinder uiBinder = GWT.create(TablePanelUiBinder.class);

  private TablePanel(final String databaseUUID, final String tableUUID) {
    dbSearchPanel = new SearchPanel(new Filter(), "", "Search in all tables", false, false);
    sidebar = DatabaseSidebar.getInstance(databaseUUID);
    initWidget(uiBinder.createAndBindUi(this));

    BreadcrumbManager.updateBreadcrumb(breadcrumb,
      BreadcrumbManager.forTable("Database (loading)", databaseUUID, "Table (loading)", tableUUID));

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

          BreadcrumbManager.updateBreadcrumb(breadcrumb,
            BreadcrumbManager.forTable(database.getMetadata().getName(), databaseUUID, table.getName(), tableUUID));

          init();
        }
      });
  }

  private void init() {
    tableRowList = new TableRowList(database, table);

    searchPanel = new SearchPanel(new Filter(), ViewerConstants.SOLR_ROW_SEARCH, "search placeholder", false, false);
    searchPanel.setList(tableRowList);
    searchPanel.setDefaultFilterIncremental(true);

    searchContainer.setWidget(searchPanel);
    tableContainer.setWidget(tableRowList);
  }
}
