package com.databasepreservation.common.client.common.visualization.browse.table;

import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.RightPanel;
import com.databasepreservation.common.client.common.search.SavedSearch;
import com.databasepreservation.common.client.common.search.SearchInfo;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.tools.ViewerJsonUtils;
import com.databasepreservation.common.client.services.SearchService;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;

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

    mainHeader.setWidget(CommonClientUtils.getHeader(FontAwesomeIconManager.getTag(FontAwesomeIconManager.SAVED_SEARCH),
      messages.loading(), "h1"));


    SearchService.Util.call((SavedSearch result) ->{
      savedSearch = result;
      init();
    } ).retrieveSavedSearch(database.getUuid(), savedSearchUUID);
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
    // set UI
    mainHeader.setWidget(CommonClientUtils.getHeader(FontAwesomeIconManager.getTag(FontAwesomeIconManager.SAVED_SEARCH),
      savedSearch.getName(), "h1"));

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
    SearchService.Util.call((Void result) -> {
      buttonApply.setEnabled(true);
      buttonCancel.setEnabled(true);

      // goto show on success
      HistoryManager.gotoSavedSearch(database.getUuid(), savedSearchUUID);
    }, (String errorMessage) -> {
      buttonApply.setEnabled(true);
      buttonCancel.setEnabled(true);
    }).editSearch(database.getUuid(), savedSearchUUID, textBoxName.getText(), textAreaDescription.getText());
  }

  @UiHandler("buttonCancel")
  void handleButtonCancel(ClickEvent e) {
    // goto list
    HistoryManager.gotoSavedSearches(database.getUuid());
  }
}
