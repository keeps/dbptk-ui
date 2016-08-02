package com.databasepreservation.visualization.client.browse;

import java.util.HashMap;
import java.util.Map;

import org.roda.core.data.v2.index.IsIndexed;

import com.databasepreservation.visualization.client.BrowserService;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.client.common.DefaultAsyncCallback;
import com.databasepreservation.visualization.client.common.sidebar.DatabaseSidebar;
import com.databasepreservation.visualization.client.common.utils.RightPanelLoader;
import com.databasepreservation.visualization.client.main.BreadcrumbPanel;
import com.databasepreservation.visualization.shared.client.Tools.BreadcrumbManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
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

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField(provided = true)
  DatabaseSidebar sidebar;

  @UiField
  SimplePanel rightPanelContainer;

  private String databaseUUID;
  private ViewerDatabase database;

  private DatabasePanel(String databaseUUID) {
    this.databaseUUID = databaseUUID;
    this.sidebar = DatabaseSidebar.getInstance(databaseUUID);

    initWidget(uiBinder.createAndBindUi(this));

    BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.loadingDatabase(databaseUUID));
  }

  public void load(RightPanelLoader rightPanelLoader) {
    if (database == null) {
      loadPanelWithDatabase(rightPanelLoader);
    } else {
      loadPanel(rightPanelLoader);
    }
  }

  private void loadPanelWithDatabase(final RightPanelLoader rightPanelLoader) {
    BrowserService.Util.getInstance().retrieve(ViewerDatabase.class.getName(), databaseUUID,
      new DefaultAsyncCallback<IsIndexed>() {
        @Override
        public void onSuccess(IsIndexed result) {
          database = (ViewerDatabase) result;
          loadPanel(rightPanelLoader);
        }
      });
  }

  private void loadPanel(RightPanelLoader rightPanelLoader) {
    RightPanel rightPanel = rightPanelLoader.load(database);

    if (rightPanel != null) {
      rightPanel.handleBreadcrumb(breadcrumb);
      rightPanelContainer.setWidget(rightPanel);
      rightPanel.setVisible(true);
    }
  }
}
