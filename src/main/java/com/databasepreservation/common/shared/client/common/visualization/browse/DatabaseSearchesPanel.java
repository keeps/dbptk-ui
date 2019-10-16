package com.databasepreservation.common.shared.client.common.visualization.browse;

import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;

import com.databasepreservation.common.shared.ViewerConstants;
import com.databasepreservation.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.shared.client.common.RightPanel;
import com.databasepreservation.common.shared.client.common.lists.SavedSearchList;
import com.databasepreservation.common.shared.client.common.search.SavedSearch;
import com.databasepreservation.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.common.shared.client.tools.HistoryManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DatabaseSearchesPanel extends RightPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface DatabaseSearchesPanelUiBinder extends UiBinder<Widget, DatabaseSearchesPanel> {
  }

  private static DatabaseSearchesPanelUiBinder uiBinder = GWT.create(DatabaseSearchesPanelUiBinder.class);

  private ViewerDatabase database;
  private SavedSearchList savedSearchList;

  public static DatabaseSearchesPanel createInstance(ViewerDatabase database) {
    return new DatabaseSearchesPanel(database);
  }

  @UiField
  FlowPanel content;

  @UiField
  Label title;

  private DatabaseSearchesPanel(ViewerDatabase database) {
    this.database = database;
    initWidget(uiBinder.createAndBindUi(this));

    init();
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(breadcrumb,
          BreadcrumbManager.forDatabaseSavedSearches(database.getUUID(), database.getMetadata().getName()));
  }

  private void init() {
    title.setText(messages.menusidebar_savedSearches());
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
