package com.databasepreservation.visualization.client.browse;

import com.databasepreservation.visualization.shared.ViewerConstants;
import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;

import com.databasepreservation.visualization.shared.SavedSearch;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.client.common.lists.SavedSearchList;
import com.databasepreservation.visualization.client.main.BreadcrumbPanel;
import com.databasepreservation.visualization.shared.client.Tools.BreadcrumbManager;
import com.databasepreservation.visualization.shared.client.Tools.HistoryManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DatabaseSearchesPanel extends RightPanel {
  public static DatabaseSearchesPanel createInstance(ViewerDatabase database) {
    return new DatabaseSearchesPanel(database);
  }

  interface DatabaseSearchesPanelUiBinder extends UiBinder<Widget, DatabaseSearchesPanel> {
  }

  private static DatabaseSearchesPanelUiBinder uiBinder = GWT.create(DatabaseSearchesPanelUiBinder.class);

  private ViewerDatabase database;
  private SavedSearchList savedSearchList;

  @UiField
  FlowPanel content;

  private DatabaseSearchesPanel(ViewerDatabase database) {
    this.database = database;
    initWidget(uiBinder.createAndBindUi(this));

    init();
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(breadcrumb,
      BreadcrumbManager.forDatabase(database.getMetadata().getName(), database.getUUID()));
  }

  private void init() {
    savedSearchList = new SavedSearchList(database.getUUID(),
      new Filter(new BasicSearchFilterParameter(ViewerConstants.SOLR_SEARCHES_DATABASE_UUID, database.getUUID())),
      null, null, false, false);

    savedSearchList.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        SavedSearch selected = savedSearchList.getSelectionModel().getSelectedObject();
        if (selected != null) {
          HistoryManager.gotoSavedSearch(selected.getDatabaseUUID(), selected.getUUID());
        }
      }
    });

    content.add(savedSearchList);
  }
}
