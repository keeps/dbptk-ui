/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.visualization.metadata.users;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.visualization.metadata.MetadataControlPanel;
import com.databasepreservation.common.client.common.visualization.metadata.MetadataPanel;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerSIARDBundle;
import com.databasepreservation.common.client.models.structure.ViewerUserStructure;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MetadataUsersPanel extends MetadataPanel {

  interface MetadataUsersUiBinder extends UiBinder<Widget, MetadataUsersPanel> {

  }

  private static MetadataUsersUiBinder uiBinder = GWT.create(MetadataUsersUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, MetadataUsersPanel> instances = new HashMap<>();
  private ViewerDatabase database;
  private ViewerMetadata metadata = null;
  private final ViewerSIARDBundle SIARDbundle;
  private List<ViewerUserStructure> users;
  private final MetadataControlPanel controls;

  public static MetadataUsersPanel getInstance(ViewerDatabase database, ViewerSIARDBundle SIARDbundle) {
    String code = database.getUuid();

    MetadataUsersPanel instance = instances.get(code);
    if (instance == null) {
      instance = new MetadataUsersPanel(database, SIARDbundle);
      instances.put(code, instance);
    }

    return instance;
  }

  @UiField
  FlowPanel contentItems;

  @UiField
  TabPanel tabPanel;

  private MetadataUsersPanel(ViewerDatabase database, ViewerSIARDBundle SIARDbundle) {
    this.database = database;
    this.SIARDbundle = SIARDbundle;
    this.controls = MetadataControlPanel.getInstance(database.getUuid());
    initWidget(uiBinder.createAndBindUi(this));

    init();
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forSIARDEditMetadataPage(database.getUuid(), database.getMetadata().getName()));
  }

  private void init() {
    metadata = database.getMetadata();

    tabPanel.add(new MetadataUsers(SIARDbundle, metadata.getUsers(), controls).createTable(), messages.titleUsers());
    tabPanel.add(new MetadataRoles(SIARDbundle, metadata.getRoles(), controls).createTable(), messages.titleRoles());
    tabPanel.add(new MetadataPrivileges(SIARDbundle, metadata.getPrivileges(), controls).createTable(),
      messages.titlePrivileges());
    tabPanel.selectTab(0);
  }
}