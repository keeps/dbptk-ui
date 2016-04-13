package com.databasepreservation.dbviewer.client.browse;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.ui.Label;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.v2.index.IsIndexed;

import com.databasepreservation.dbviewer.client.BrowserService;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerMetadata;
import com.databasepreservation.dbviewer.client.common.search.SearchPanel;
import com.databasepreservation.dbviewer.client.main.BreadcrumbPanel;
import com.databasepreservation.dbviewer.shared.client.HistoryManager;
import com.databasepreservation.dbviewer.shared.client.Tools.BreadcrumbManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DatabasePanel extends Composite {
  private static Map<String, DatabasePanel> instances = new HashMap<>();

  public static DatabasePanel getInstance(String databaseUUID) {
    String code = databaseUUID;

    DatabasePanel instance = instances.get(code);
    if (instance == null) {
      instance = new DatabasePanel(databaseUUID);
      instances.put(code, instance);
    }
    return instance;
  }

  interface DatabasePanelUiBinder extends UiBinder<Widget, DatabasePanel> {
  }

  private static DatabasePanelUiBinder uiBinder = GWT.create(DatabasePanelUiBinder.class);

  private ViewerDatabase database;

  @UiField
  FlexTable table;

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField(provided = true)
  SearchPanel dbSearchPanel;

  @UiField(provided = true)
  DatabaseSidebar sidebar;

  private DatabasePanel(final String databaseUUID) {
    dbSearchPanel = new SearchPanel(new Filter(), "", "Search in all tables", false, false);
    sidebar = DatabaseSidebar.getInstance(databaseUUID);

    initWidget(uiBinder.createAndBindUi(this));

    BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forDatabase("loading...", databaseUUID));

    table.setHTML(0, 0, new SafeHtml() {
      @Override public String asString() {
        return "<h4>Database information</h4>";
      }
    });
    table.getFlexCellFormatter().setColSpan(0,0,2);

    BrowserService.Util.getInstance().retrieve(ViewerDatabase.class.getName(), databaseUUID,
      new AsyncCallback<IsIndexed>() {
        @Override
        public void onFailure(Throwable caught) {
          throw new RuntimeException(caught);
        }

        @Override
        public void onSuccess(IsIndexed result) {
          database = (ViewerDatabase) result;
          init();
        }
      });
  }

  private Hyperlink getHyperlink(String display_text, String database_uuid, String table_uuid) {
    Hyperlink link = new Hyperlink(display_text, HistoryManager.linkToTable(database_uuid, table_uuid));
    return link;
  }

  private void init() {
    // breadcrumb
    BreadcrumbManager.updateBreadcrumb(breadcrumb,
      BreadcrumbManager.forDatabase(database.getMetadata().getName(), database.getUUID()));

    // database metadata
    ViewerMetadata metadata = database.getMetadata();
    addRow("Name", metadata.getName());
    addRow("ArchivalDate", metadata.getArchivalDate());
    addRow("Archiver", metadata.getArchiver());
    addRow("ArchiverContact", metadata.getArchiverContact());
    addRow("ClientMachine", metadata.getClientMachine());
    addRow("DatabaseProduct", metadata.getDatabaseProduct());
    addRow("DatabaseUser", metadata.getDatabaseUser());
    addRow("DataOriginTimespan", metadata.getDataOriginTimespan());
    addRow("DataOwner", metadata.getDataOwner());
    addRow("Description", metadata.getDescription());
    addRow("ProducerApplication", metadata.getProducerApplication());




  }

  private void addRow(String header, String content){
    int numRows = table.getRowCount();
    table.setText(numRows, 0, header);
    table.getFlexCellFormatter().addStyleName(numRows, 0, "db-info-th");
    table.setText(numRows, 1, content);
  }
}
