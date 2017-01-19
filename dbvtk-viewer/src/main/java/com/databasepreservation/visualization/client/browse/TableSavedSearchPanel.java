package com.databasepreservation.visualization.client.browse;

import org.roda.core.data.v2.index.IsIndexed;

import com.databasepreservation.visualization.client.BrowserService;
import com.databasepreservation.visualization.client.SavedSearch;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.client.common.DefaultAsyncCallback;
import com.databasepreservation.visualization.client.common.search.SearchInfo;
import com.databasepreservation.visualization.client.common.search.TableSearchPanel;
import com.databasepreservation.visualization.client.common.utils.CommonClientUtils;
import com.databasepreservation.visualization.client.main.BreadcrumbPanel;
import com.databasepreservation.visualization.shared.client.Tools.BreadcrumbManager;
import com.databasepreservation.visualization.shared.client.Tools.ViewerJsonUtils;
import com.databasepreservation.visualization.shared.client.Tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class TableSavedSearchPanel extends RightPanel {
  public static TableSavedSearchPanel createInstance(ViewerDatabase database, String savedSearchUUID) {
    return new TableSavedSearchPanel(database, savedSearchUUID);
  }

  interface TableSavedSearchPanelUiBinder extends UiBinder<Widget, TableSavedSearchPanel> {
  }

  private static TableSavedSearchPanelUiBinder uiBinder = GWT.create(TableSavedSearchPanelUiBinder.class);

  private ViewerDatabase database;
  private String savedSearchUUID;
  private SavedSearch savedSearch;

  @UiField
  SimplePanel mainHeader;

  @UiField
  HTML description;

  @UiField
  SimplePanel tableSearchPanelContainer;

  private TableSearchPanel tableSearchPanel;

  private TableSavedSearchPanel(ViewerDatabase viewerDatabase, final String savedSearchUUID) {
    database = viewerDatabase;
    this.savedSearchUUID = savedSearchUUID;

    initWidget(uiBinder.createAndBindUi(this));

    mainHeader.setWidget(CommonClientUtils.getSavedSearchHeader(database.getUUID(), "Loading..."));

    BrowserService.Util.getInstance().retrieve(database.getUUID(), SavedSearch.class.getName(), savedSearchUUID,
      new DefaultAsyncCallback<IsIndexed>() {
        @Override
        public void onSuccess(IsIndexed result) {
          savedSearch = (SavedSearch) result;
          init();
        }
      });
  }

  /**
   * Delegates the method to the innerRightPanel
   *
   * @param breadcrumb
   *          the BreadcrumbPanel for this database
   */
  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(breadcrumb,
      BreadcrumbManager.forDatabaseSavedSearch(database.getMetadata().getName(), database.getUUID(), savedSearchUUID));
  }

  /**
   * Choose and display the correct panel: a RowPanel when search returns one
   * result, otherwise show a TablePanel
   */
  private void init() {
    String tableUUID = savedSearch.getTableUUID();

    // set UI
    mainHeader.setWidget(CommonClientUtils.getSavedSearchHeader(database.getUUID(), savedSearch.getName()));
    if (ViewerStringUtils.isNotBlank(savedSearch.getDescription())) {
      description.setHTML(CommonClientUtils.getFieldHTML("Description", savedSearch.getDescription()));
    }

    // set searchForm and table
    SearchInfo searchInfo = ViewerJsonUtils.getSearchInfoMapper().read(savedSearch.getSearchInfoJson());
    if (SearchInfo.isPresentAndValid(searchInfo)) {
      tableSearchPanel = new TableSearchPanel(searchInfo);
      tableSearchPanel.provideSource(database, database.getMetadata().getTable(tableUUID));
      tableSearchPanelContainer.setWidget(tableSearchPanel);
    } else {
      GWT.log("search info was invalid. JSON: " + savedSearch.getSearchInfoJson());
    }
  }
}
