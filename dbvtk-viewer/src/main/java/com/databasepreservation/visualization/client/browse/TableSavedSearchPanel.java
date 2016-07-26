package com.databasepreservation.visualization.client.browse;

import org.roda.core.data.v2.index.IsIndexed;

import com.databasepreservation.visualization.client.BrowserService;
import com.databasepreservation.visualization.client.SavedSearch;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.client.common.search.SearchInfo;
import com.databasepreservation.visualization.client.main.BreadcrumbPanel;
import com.databasepreservation.visualization.shared.client.Tools.ViewerJsonUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
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

  private RightPanel innerRightPanel = null;
  private BreadcrumbPanel breadcrumb = null;

  @UiField
  SimplePanel panel;

  private TableSavedSearchPanel(ViewerDatabase viewerDatabase, final String savedSearchUUID) {
    database = viewerDatabase;
    this.savedSearchUUID = savedSearchUUID;

    initWidget(uiBinder.createAndBindUi(this));

    // search (count)
    BrowserService.Util.getInstance().retrieve(SavedSearch.class.getName(), savedSearchUUID,
      new AsyncCallback<IsIndexed>() {
        @Override
        public void onFailure(Throwable caught) {
          throw new RuntimeException(caught);
        }

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
    // set this in case handleBreadcrumb happens before init
    if (breadcrumb != null) {
      this.breadcrumb = breadcrumb;
    }

    // this will be true if init has already run when handleBreadcrumb is called
    // externally; or handleBreadcrumb was called first and init is finishing up
    if (innerRightPanel != null && breadcrumb != null) {
      innerRightPanel.handleBreadcrumb(breadcrumb);
    }
  }

  /**
   * Choose and display the correct panel: a RowPanel when search returns one
   * result, otherwise show a TablePanel
   */
  private void init() {
    // display a RowPanel
    String tableUUID = savedSearch.getTableUUID();

    SearchInfo searchInfo = ViewerJsonUtils.getSearchInfoMapper().read(savedSearch.getSearchInfoJson());
    if (SearchInfo.isPresentAndValid(searchInfo)) {
      innerRightPanel = TablePanel.createInstance(database, database.getMetadata().getTable(tableUUID), searchInfo);
      handleBreadcrumb(breadcrumb);
      panel.setWidget(innerRightPanel);
    } else {
      GWT.log("search info was invalid. JSON: " + savedSearch.getSearchInfoJson());
    }
  }
}
