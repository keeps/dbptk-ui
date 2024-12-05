/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.visualization.manager.databasePanel.user;

import java.util.List;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.ContentPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbItem;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.lists.CrossDatabaseList;
import com.databasepreservation.common.client.common.lists.DatabaseList;
import com.databasepreservation.common.client.common.lists.utils.AsyncTableCellOptions;
import com.databasepreservation.common.client.common.lists.utils.ListBuilder;
import com.databasepreservation.common.client.common.search.SearchWrapper;
import com.databasepreservation.common.client.common.utils.ApplicationType;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class UserDatabaseListPanel extends ContentPanel {
  @UiField
  public ClientMessages messages = GWT.create(ClientMessages.class);

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forManageDatabase();
    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
  }

  interface ManageUiBinder extends UiBinder<Widget, UserDatabaseListPanel> {
  }

  private static ManageUiBinder binder = GWT.create(ManageUiBinder.class);

  @UiField
  SimplePanel mainHeader;

  @UiField
  SimplePanel description;

  @UiField
  SimplePanel adminInfo;

  @UiField(provided = true)
  SearchWrapper search;

  private static UserDatabaseListPanel instance = null;

  public static UserDatabaseListPanel getInstance() {
    if (instance == null) {
      instance = new UserDatabaseListPanel();
    }
    return instance;
  }

  private UserDatabaseListPanel() {

    ListBuilder<ViewerDatabase> databaseMetadataList = new ListBuilder<>(() -> {
      DatabaseList metadataDatabaseList = new DatabaseList();
      metadataDatabaseList.getSelectionModel().addSelectionChangeHandler(event -> {
        ViewerDatabase selected = metadataDatabaseList.getSelectionModel().getSelectedObject();
        if (selected != null) {
          if (ApplicationType.getType().equals(ViewerConstants.APPLICATION_ENV_SERVER)) {
            HistoryManager.gotoDatabase(selected.getUuid());
          }
          metadataDatabaseList.getSelectionModel().clear();
        }
      });
      return metadataDatabaseList;
    }, new AsyncTableCellOptions<>(ViewerDatabase.class, "DatabaseList_metadata"));
    ListBuilder<ViewerDatabase> databaseSearchAll = new ListBuilder<>(() -> {
      CrossDatabaseList allDatabaseList = new CrossDatabaseList();
      allDatabaseList.getSelectionModel().addSelectionChangeHandler(event -> {
        allDatabaseList.setSearchValue(search.getComponents().getSearchPanel("DatabaseList_all").getCurrentFilter());
      });
      return allDatabaseList;
    }, new AsyncTableCellOptions<>(ViewerDatabase.class, "DatabaseList_all"));
    search = new SearchWrapper(true).createListAndSearchPanel(databaseMetadataList, false)
      .createListAndSearchPanel(databaseSearchAll, true);

    initWidget(binder.createAndBindUi(this));

    mainHeader.setWidget(CommonClientUtils.getHeader(FontAwesomeIconManager.getTag(FontAwesomeIconManager.SERVER),
      messages.menusidebar_databases(), "h1"));

    MetadataField instance = MetadataField.createInstance(messages.manageDatabasePageDescription());
    instance.setCSS("table-row-description", "font-size-description");

    description.setWidget(instance);

    MetadataField adminInfoInstance = MetadataField
      .createInstance(messages.manageDatabaseContactAdministratorDescription());
    adminInfoInstance.setCSS("siards-contact-admin-info", "font-size-description");
    adminInfo.setWidget(adminInfoInstance);

  }

  /**
   * This method is called immediately after a widget becomes attached to the
   * browser's document.
   */
  @Override
  protected void onLoad() {
    super.onLoad();
  }
}