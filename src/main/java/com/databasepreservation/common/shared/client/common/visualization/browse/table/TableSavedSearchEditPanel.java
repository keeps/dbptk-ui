package com.databasepreservation.common.shared.client.common.visualization.browse.table;

import com.databasepreservation.common.client.BrowserService;
import com.databasepreservation.common.shared.ViewerStructure.IsIndexed;
import com.databasepreservation.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.shared.client.common.RightPanel;
import com.databasepreservation.common.shared.client.common.search.SavedSearch;
import com.databasepreservation.common.shared.client.common.search.SearchInfo;
import com.databasepreservation.common.shared.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.shared.client.tools.HistoryManager;
import com.databasepreservation.common.shared.client.tools.ViewerJsonUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class TableSavedSearchEditPanel extends RightPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static TableSavedSearchEditPanel createInstance(ViewerDatabase database, String savedSearchUUID) {
    return new TableSavedSearchEditPanel(database, savedSearchUUID);
  }

  interface TableSavedSearchEditPanelUiBinder extends UiBinder<Widget, TableSavedSearchEditPanel> {
  }

  private static TableSavedSearchEditPanelUiBinder uiBinder = GWT.create(TableSavedSearchEditPanelUiBinder.class);

  private ViewerDatabase database;
  private String savedSearchUUID;
  private SavedSearch savedSearch;

  @UiField
  SimplePanel mainHeader;

  @UiField
  TextArea textAreaDescription;

  @UiField
  TextBox textBoxName;

  @UiField
  Button buttonApply;

  @UiField
  Button buttonCancel;

  private TableSavedSearchEditPanel(ViewerDatabase viewerDatabase, final String savedSearchUUID) {
    database = viewerDatabase;
    this.savedSearchUUID = savedSearchUUID;

    initWidget(uiBinder.createAndBindUi(this));

    mainHeader.setWidget(CommonClientUtils.getSavedSearchHeader(database.getUUID(), messages.loading()));

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
    // BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager
    // .forDatabaseSavedSearchEdit(database.getMetadata().getName(),
    // database.getUUID(), savedSearchUUID));
  }

  /**
   * Choose and display the correct panel: a RowPanel when search returns one
   * result, otherwise show a TablePanel
   */
  private void init() {
    String tableUUID = savedSearch.getTableUUID();

    // set UI
    Label savedSearchLabel = new Label(messages.editingSavedSearch());
    savedSearchLabel.addStyleName("h1");
    mainHeader.setWidget(savedSearchLabel);

    // set searchForm and table
    SearchInfo searchInfo = ViewerJsonUtils.getSearchInfoMapper().read(savedSearch.getSearchInfoJson());
    if (SearchInfo.isPresentAndValid(searchInfo)) {
      textBoxName.setText(savedSearch.getName());
      textAreaDescription.setText(savedSearch.getDescription());
    } else {
      GWT.log("search info was invalid. JSON: " + savedSearch.getSearchInfoJson());
    }
  }

  @UiHandler("buttonApply")
  void handleButtonApply(ClickEvent e) {
    buttonApply.setEnabled(false);
    buttonCancel.setEnabled(false);

    // update info & commit
    BrowserService.Util.getInstance().editSearch(database.getUUID(), savedSearchUUID, textBoxName.getText(),
      textAreaDescription.getText(), new DefaultAsyncCallback<Void>() {
        @Override
        public void onFailure(Throwable caught) {
          // error, don't go anywhere
          buttonApply.setEnabled(true);
          buttonCancel.setEnabled(true);
          super.onFailure(caught);
        }

        @Override
        public void onSuccess(Void result) {
          buttonApply.setEnabled(true);
          buttonCancel.setEnabled(true);

          // goto show on success
          HistoryManager.gotoSavedSearch(database.getUUID(), savedSearchUUID);
        }
      });

  }

  @UiHandler("buttonCancel")
  void handleButtonCancel(ClickEvent e) {
    // goto list
    HistoryManager.gotoSavedSearches(database.getUUID());
  }
}
