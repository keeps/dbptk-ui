/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.visualization.browse;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.RightPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.lists.SavedSearchList;
import com.databasepreservation.common.client.common.search.SavedSearch;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.index.filter.BasicSearchFilterParameter;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

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
  SimplePanel mainHeader;

  @UiField
  SimplePanel description;

  private DatabaseSearchesPanel(ViewerDatabase database) {
    this.database = database;
    initWidget(uiBinder.createAndBindUi(this));

    init();
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(breadcrumb,
      BreadcrumbManager.forDatabaseSavedSearches(database.getMetadata().getName(), database.getUuid()));
  }

  private void init() {
    mainHeader.setWidget(CommonClientUtils.getHeaderHTML(
      FontAwesomeIconManager.getTag(FontAwesomeIconManager.SAVED_SEARCH), messages.menusidebar_savedSearches(), "h1"));
    savedSearchList = new SavedSearchList(database.getUuid(),
      new Filter(new BasicSearchFilterParameter(ViewerConstants.SOLR_SEARCHES_DATABASE_UUID, database.getUuid())), null,
      null, false, false);

    savedSearchList.getSelectionModel().addSelectionChangeHandler(event -> {
      SavedSearch selected = savedSearchList.getSelectionModel().getSelectedObject();
      if (selected != null) {
        HistoryManager.gotoSavedSearch(selected.getDatabaseUUID(), selected.getUuid());
      }
    });

    content.add(savedSearchList);
  }
}
