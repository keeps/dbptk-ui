package com.databasepreservation.dbviewer.client.browse;

import java.util.HashMap;
import java.util.Map;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.v2.index.IsIndexed;

import com.databasepreservation.dbviewer.client.BrowserService;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.dbviewer.client.common.search.SearchPanel;
import com.databasepreservation.dbviewer.client.common.sidebar.DatabaseSidebar;
import com.databasepreservation.dbviewer.client.main.BreadcrumbPanel;
import com.databasepreservation.dbviewer.shared.client.Tools.BreadcrumbManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DatabaseUsersPanel extends Composite {
  private static Map<String, DatabaseUsersPanel> instances = new HashMap<>();

  public static DatabaseUsersPanel getInstance(String databaseUUID) {
    String code = databaseUUID;

    DatabaseUsersPanel instance = instances.get(code);
    if (instance == null) {
      instance = new DatabaseUsersPanel(databaseUUID);
      instances.put(code, instance);
    }
    return instance;
  }

  interface DatabasePanelUiBinder extends UiBinder<Widget, DatabaseUsersPanel> {
  }

  private static DatabasePanelUiBinder uiBinder = GWT.create(DatabasePanelUiBinder.class);

  private ViewerDatabase database;

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField(provided = true)
  SearchPanel dbSearchPanel;

  @UiField(provided = true)
  DatabaseSidebar sidebar;
  @UiField
  HTML metadatahtml;

  private DatabaseUsersPanel(final String databaseUUID) {
    dbSearchPanel = new SearchPanel(new Filter(), "", "Search in all tables", false, false);
    sidebar = DatabaseSidebar.getInstance(databaseUUID);

    initWidget(uiBinder.createAndBindUi(this));

    BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.loadingDatabase(databaseUUID));

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

  private void init() {
    // breadcrumb
    BreadcrumbManager.updateBreadcrumb(breadcrumb,
      BreadcrumbManager.forDatabase(database.getMetadata().getName(), database.getUUID()));

    Label tmpNote = new Label("User information will be available on this page in a future release.");
    metadatahtml.setHTML(SafeHtmlUtils.fromSafeConstant(tmpNote.toString()));
  }
}
