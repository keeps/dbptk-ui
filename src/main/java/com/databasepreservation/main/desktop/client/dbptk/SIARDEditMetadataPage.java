package com.databasepreservation.main.desktop.client.dbptk;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.ViewerStructure.IsIndexed;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbItem;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.main.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.main.desktop.client.common.sidebar.MetadataEditSidebar;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;
import com.google.gwt.core.client.GWT;

import java.util.List;

/**
 * @autor Gabriel Barros <gbarros@keep.pt>
 */
public class SIARDEditMetadataPage extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface SIARDEditMetadataPageUiBinder extends UiBinder<Widget, SIARDEditMetadataPage> {
  }

  private static SIARDEditMetadataPageUiBinder binder = GWT.create(SIARDEditMetadataPageUiBinder.class);
  private static SIARDEditMetadataPage instance = null;
  private String databaseUUID;
  private ViewerDatabase database = null;

  public static SIARDEditMetadataPage getInstance(String databaseUUID) {
    if (instance == null) {
      GWT.log("SIARDEditMetadataPage, getInstance ");
      instance = new SIARDEditMetadataPage(databaseUUID);
    }
    return instance;
  }

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField(provided = true)
  MetadataEditSidebar sidebar;

  @UiField
  SimplePanel rightPanelContainer;


  private SIARDEditMetadataPage(final String databaseUUID) {
    this.databaseUUID = databaseUUID;
    this.sidebar = MetadataEditSidebar.getInstance(databaseUUID);

    initWidget(binder.createAndBindUi(this));

    List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forSIARDEditMetadataPage();
    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
  }

  public void load(Composite content){
    GWT.log("load. uuid: " + databaseUUID + ", database: " + database);

    if (databaseUUID != null && (database == null || !ViewerDatabase.Status.METADATA_ONLY.equals(database.getStatus()))) {
      GWT.log("getting db");
      loadPanelWithDatabase(content);
    } else {
      loadPanel(content);
    }
  }

  private void loadPanelWithDatabase(Composite content) {
    BrowserService.Util.getInstance().retrieve(databaseUUID, ViewerDatabase.class.getName(), databaseUUID,
      new DefaultAsyncCallback<IsIndexed>() {
        @Override
        public void onFailure(Throwable caught) {
          GWT.log("SIARDEditMetadataPage onFailure " + caught);
        }

        @Override
        public void onSuccess(IsIndexed result) {
          GWT.log("SIARDEditMetadataPage onSuccess ");
          database = (ViewerDatabase) result;
          loadPanel(content);
        }
      });
  }

  private void loadPanel(Composite content){
    GWT.log("have db: " + database + "sb.init: " + sidebar.isInitialized());
    if (database != null && !sidebar.isInitialized()) {
      sidebar.init(database);
    }

    rightPanelContainer.setWidget(content);
    content.setVisible(true);

  }
}
