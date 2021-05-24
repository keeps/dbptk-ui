/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.visualization.metadata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbItem;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.sidebar.MetadataEditSidebar;
import com.databasepreservation.common.client.common.utils.ApplicationType;
import com.databasepreservation.common.client.index.IsIndexed;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.client.models.structure.ViewerSIARDBundle;
import com.databasepreservation.common.client.services.DatabaseService;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @autor Gabriel Barros <gbarros@keep.pt>
 */
public class SIARDEditMetadataPage extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface SIARDEditMetadataPageUiBinder extends UiBinder<Widget, SIARDEditMetadataPage> {
  }

  private static SIARDEditMetadataPageUiBinder binder = GWT.create(SIARDEditMetadataPageUiBinder.class);
  private static Map<String, SIARDEditMetadataPage> instances = new HashMap<>();
  private String databaseUUID;
  private ViewerDatabase database = null;
  private ViewerSIARDBundle SIARDbundle = new ViewerSIARDBundle();
  private BreadcrumbPanel breadcrumb = null;

  public static SIARDEditMetadataPage getInstance(String databaseUUID) {

    SIARDEditMetadataPage instance = instances.get(databaseUUID);
    if (instance == null) {
      instance = new SIARDEditMetadataPage(databaseUUID);
      instances.put(databaseUUID, instance);
    }
    return instance;
  }

  @UiField
  BreadcrumbPanel breadcrumbServer, breadcrumbDesktop;

  @UiField(provided = true)
  MetadataEditSidebar sidebar;

  @UiField(provided = true)
  MetadataControlPanel controls;

  @UiField
  SimplePanel rightPanelContainer;

  @UiField
  MenuBar menu;

  @UiField
  FlowPanel toplevel, toolbar;

  private SIARDEditMetadataPage(final String databaseUUID) {
    this.databaseUUID = databaseUUID;
    this.sidebar = MetadataEditSidebar.getInstance(databaseUUID);
    this.controls = MetadataControlPanel.getInstance(databaseUUID);

    initWidget(binder.createAndBindUi(this));

    if (ApplicationType.getType().equals(ViewerConstants.APPLICATION_ENV_SERVER)) {
      toolbar.getElement().addClassName("filePreviewToolbar");
      breadcrumb = breadcrumbServer;
      breadcrumbDesktop.removeFromParent();
    } else {
      toolbar.removeFromParent();
      breadcrumb = breadcrumbDesktop;
    }
    breadcrumb.setVisible(true);
  }

  public void load(MetadataPanelLoad rightPanelLoader, String sidebarSelected) {
    if (databaseUUID != null
      && (database == null || !ViewerDatabaseStatus.METADATA_ONLY.equals(database.getStatus()))) {
      loadPanelWithDatabase(rightPanelLoader, sidebarSelected);
    } else {
      loadPanel(rightPanelLoader, sidebarSelected);
    }
  }

  private void loadPanelWithDatabase(MetadataPanelLoad rightPanelLoader, String sidebarSelected) {
    DatabaseService.Util.call((IsIndexed result) -> {
      database = (ViewerDatabase) result;
      List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forSIARDEditMetadataPage(databaseUUID,
        database.getMetadata().getName());
      BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
      loadPanel(rightPanelLoader, sidebarSelected);
    }).retrieve(databaseUUID);
  }

  private void loadPanel(MetadataPanelLoad rightPanelLoader, String sidebarSelected) {
    GWT.log("have db: " + database + "sb.init: " + sidebar.isInitialized());

    if (database != null && !sidebar.isInitialized()) {
      sidebar.init(database);
      controls.init(database, SIARDbundle);
    }

    sidebar.select(sidebarSelected);

    MetadataPanel rightPanel = rightPanelLoader.load(database, SIARDbundle);
    if (rightPanel != null) {
      rightPanel.handleBreadcrumb(breadcrumb);
      rightPanelContainer.setWidget(rightPanel);
      rightPanel.setVisible(true);
    }

  }

  public void setTopLevelPanelCSS(String css) {
    toplevel.addStyleName(css);
  }
}
