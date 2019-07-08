package com.databasepreservation.main.visualization.client.browse;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.common.shared.client.common.RightPanel;
import com.databasepreservation.main.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.main.common.shared.client.tools.HistoryManager;
import com.databasepreservation.main.common.shared.client.widgets.Toast;
import com.databasepreservation.main.visualization.client.common.lists.DatabaseList;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DatabaseListPanel extends RightPanel {
  /**
   * Uses BreadcrumbManager to show available information in the breadcrumbPanel
   *
   * @param breadcrumb
   *          the BreadcrumbPanel for this database
   */
  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forDatabases());
  }

  interface DatabaseListPanelUiBinder extends UiBinder<Widget, DatabaseListPanel> {
  }

  private static DatabaseListPanel instance = null;

  public static DatabaseListPanel getInstance() {
    if (instance == null) {
      instance = new DatabaseListPanel();
    }
    return instance;
  }

  private static DatabaseListPanelUiBinder uiBinder = GWT.create(DatabaseListPanelUiBinder.class);

  @UiField(provided = true)
  DatabaseList databaseList;

  private DatabaseListPanel() {
    databaseList = new DatabaseList();
    initWidget(uiBinder.createAndBindUi(this));

    databaseList.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        ViewerDatabase selected = databaseList.getSelectionModel().getSelectedObject();
        if (selected != null) {
          if (ViewerDatabase.Status.AVAILABLE.equals(selected.getStatus())) {
            HistoryManager.gotoDatabase(selected.getUUID());
          } else if (ViewerDatabase.Status.INGESTING.equals(selected.getStatus())) {
            HistoryManager.gotoUpload(selected.getUUID());
          } else {
            Toast.showError("Unavailable", "This database can not be accessed.");
            databaseList.getSelectionModel().clear();
          }
        }
      }
    });
  }

  /**
   * This method is called immediately after a widget becomes attached to the
   * browser's document.
   */
  @Override
  protected void onLoad() {
    super.onLoad();
    databaseList.getSelectionModel().clear();
  }
}
