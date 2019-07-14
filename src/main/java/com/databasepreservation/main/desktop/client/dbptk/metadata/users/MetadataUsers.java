package com.databasepreservation.main.desktop.client.dbptk.metadata.users;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerMetadata;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSIARDBundle;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerUserStructure;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.main.desktop.client.common.lists.MetadataTableList;
import com.databasepreservation.main.desktop.client.dbptk.metadata.MetadataPanel;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MetadataUsers extends MetadataPanel {

  interface EditMetadataUsersUiBinder extends UiBinder<Widget, MetadataUsers> {

  }

  private static EditMetadataUsersUiBinder uiBinder = GWT.create(EditMetadataUsersUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, MetadataUsers> instances = new HashMap<>();
  private ViewerDatabase database;
  private ViewerMetadata metadata = null;
  private final ViewerSIARDBundle SIARDbundle;
  private List<ViewerUserStructure> users;

  public static MetadataUsers getInstance(ViewerDatabase database, ViewerSIARDBundle SIARDbundle) {
    String code = database.getUUID();

    MetadataUsers instance = instances.get(code);
    if (instance == null) {
      instance = new MetadataUsers(database, SIARDbundle);
      instances.put(code, instance);
    }

    return instance;
  }

  @UiField
  FlowPanel contentItems;

  @UiField
  TabPanel tabPanel;

  private MetadataUsers(ViewerDatabase database, ViewerSIARDBundle SIARDbundle) {
    this.database = database;
    this.SIARDbundle = SIARDbundle;
    initWidget(uiBinder.createAndBindUi(this));

    init();
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forSIARDEditMetadataPage(database.getUUID()));
  }

  private void init() {
    GWT.log("Edit Metadata Users init ");
    metadata = database.getMetadata();

    tabPanel.add(getMetadataEditTableForUsers(metadata), messages.titleUsers());
    tabPanel.add(new MetadataRoles(SIARDbundle).createTable(metadata.getRoles()), messages.titleRoles());
    tabPanel.add(new MetadataPrivileges(SIARDbundle).createTable(metadata.getPrivileges()), messages.titlePrivileges());
    tabPanel.selectTab(0);
  }

  private MetadataTableList<ViewerUserStructure> getMetadataEditTableForUsers(ViewerMetadata metadata) {
    users = metadata.getUsers();

    Label header = new Label(messages.titleUsers());
    header.addStyleName("h4");

    HTMLPanel info = new HTMLPanel("");

    MetadataTableList<ViewerUserStructure> userMetadata;
    if (users.isEmpty()) {
      userMetadata = new MetadataTableList<>(header, messages.databaseDoesNotContainUsers());
    } else {
      Column<ViewerUserStructure, String> descriptionUser = new Column<ViewerUserStructure, String>(
        new EditTextCell()) {
        @Override
        public String getValue(ViewerUserStructure user) {
          return user.getDescription() == null ? messages.metadataDoesNotContainDescription() : user.getDescription();
        }
      };

      descriptionUser.setFieldUpdater(new FieldUpdater<ViewerUserStructure, String>() {
        @Override
        public void update(int index, ViewerUserStructure object, String value) {

          object.setDescription(value);
          SIARDbundle.setUser(object.getName(), object.getDescription());

        }
      });

      userMetadata = new MetadataTableList<>(header, info, users.iterator(),

        new MetadataTableList.ColumnInfo<>(messages.name(), 15, new TextColumn<ViewerUserStructure>() {
          @Override
          public String getValue(ViewerUserStructure user) {
            return user.getName();
          }
        }), new MetadataTableList.ColumnInfo<>(messages.description(), 15, descriptionUser));

    }

    return userMetadata;
  }
}