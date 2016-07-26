package com.databasepreservation.visualization.client.browse;

import java.util.HashMap;
import java.util.Map;

import org.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import org.roda.core.data.adapter.filter.Filter;

import com.databasepreservation.visualization.client.SavedSearch;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.client.common.lists.SavedSearchList;
import com.databasepreservation.visualization.client.main.BreadcrumbPanel;
import com.databasepreservation.visualization.shared.ViewerSafeConstants;
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
  private static Map<String, DatabaseSearchesPanel> instances = new HashMap<>();

  public static DatabaseSearchesPanel getInstance(ViewerDatabase database) {
    String code = database.getUUID();

    DatabaseSearchesPanel instance = instances.get(code);
    if (instance == null) {
      instance = new DatabaseSearchesPanel(database);
      instances.put(code, instance);
    }
    return instance;
  }

  interface DatabaseInformationPanelUiBinder extends UiBinder<Widget, DatabaseSearchesPanel> {
  }

  private static DatabaseInformationPanelUiBinder uiBinder = GWT.create(DatabaseInformationPanelUiBinder.class);

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
    savedSearchList = new SavedSearchList(new Filter(new BasicSearchFilterParameter(
      ViewerSafeConstants.SOLR_SEARCHES_DATABASE_UUID, database.getUUID())), null, null, false, false);

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
