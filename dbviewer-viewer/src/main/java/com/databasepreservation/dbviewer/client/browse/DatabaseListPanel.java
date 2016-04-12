package com.databasepreservation.dbviewer.client.browse;

import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.dbviewer.client.common.lists.DatabaseList;
import com.databasepreservation.dbviewer.client.common.search.SearchPanel;
import com.databasepreservation.dbviewer.client.main.BreadcrumbPanel;
import com.databasepreservation.dbviewer.shared.client.HistoryManager;
import com.databasepreservation.dbviewer.shared.client.Tools.BreadcrumbManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import org.roda.core.data.adapter.filter.Filter;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DatabaseListPanel extends Composite {
  interface DatabaseListPanelUiBinder extends UiBinder<Widget, DatabaseListPanel> {
  }

  private static DatabaseListPanelUiBinder uiBinder = GWT.create(DatabaseListPanelUiBinder.class);

  // public DatabaseListPanel() {
  // Widget rootElement = ourUiBinder.createAndBindUi(this);
  // }

  @UiField(provided = true)
  DatabaseList databaseList;

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField(provided = true) SearchPanel searchPanel;

  public DatabaseListPanel() {
    searchPanel = new SearchPanel(new Filter(), "", "(unused)", false, false);

    databaseList = new DatabaseList();
    initWidget(uiBinder.createAndBindUi(this));

    BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forDatabases());

    databaseList.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        ViewerDatabase selected = databaseList.getSelectionModel().getSelectedObject();
        if (selected != null) {
          HistoryManager.gotoDatabase(selected.getUUID());
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
