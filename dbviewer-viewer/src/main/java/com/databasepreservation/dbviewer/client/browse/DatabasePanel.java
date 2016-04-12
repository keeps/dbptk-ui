package com.databasepreservation.dbviewer.client.browse;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.v2.index.IsIndexed;

import com.databasepreservation.dbviewer.client.BrowserService;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerMetadata;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerSchema;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerTable;
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
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DatabasePanel extends Composite {
  private static Map<String,DatabasePanel> instances = new HashMap<>();
  public static DatabasePanel getInstance(String databaseUUID) {
    String code = databaseUUID;

    DatabasePanel instance = instances.get(code);
    if(instance == null){
      instance = new DatabasePanel(databaseUUID);
      instances.put(code, instance);
    }
    return instance;
  }

  interface DatabasePanelUiBinder extends UiBinder<Widget, DatabasePanel> {
  }

  private static DatabasePanelUiBinder uiBinder = GWT.create(DatabasePanelUiBinder.class);

  private ViewerDatabase database;
  private ViewerMetadata metadata;

  @UiField
  VerticalPanel vPanel;

  @UiField
  BreadcrumbPanel breadcrumb;
  @UiField(provided = true)
  SearchPanel searchPanel;

  private DatabasePanel(final String databaseID) {
    searchPanel = new SearchPanel(new Filter(), "", "Search in all tables", false, false);

    initWidget(uiBinder.createAndBindUi(this));

    vPanel.setSpacing(5);
    vPanel.setVisible(true);

    BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forDatabase("loading...", databaseID));

    BrowserService.Util.getInstance().retrieve(ViewerDatabase.class.getName(), databaseID,
      new AsyncCallback<IsIndexed>() {
        @Override
        public void onFailure(Throwable caught) {
          vPanel.clear();
          HTML headingElement = new HTML();
          headingElement.setHTML("<span>" + caught.getMessage() + "</span>");
          vPanel.add(headingElement);
          throw new RuntimeException(caught);
        }

        @Override
        public void onSuccess(IsIndexed result) {
          database = (ViewerDatabase) result;
          metadata = database.getMetadata();
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
    vPanel.clear();
    vPanel.add(new HTML(new SafeHtml() {
      @Override
      public String asString() {
        return "<h4>Database: " + database.getMetadata().getName() + "</h4>";
      }
    }));

    for (final ViewerSchema schema : metadata.getSchemas()) {
      vPanel.add(new HTML(new SafeHtml() {
        @Override
        public String asString() {
          return "<h5>Schema: " + schema.getName() + "</h5>";
        }
      }));
      for (ViewerTable table : schema.getTables()) {
        vPanel.add(getHyperlink(table.getName(), database.getUUID(), table.getUUID()));
      }
    }
  }
}
