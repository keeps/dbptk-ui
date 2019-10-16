package com.databasepreservation.common.shared.client.common.visualization.browse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.common.shared.ViewerStructure.ViewerMetadata;
import com.databasepreservation.common.shared.ViewerStructure.ViewerPrivilegeStructure;
import com.databasepreservation.common.shared.ViewerStructure.ViewerRoleStructure;
import com.databasepreservation.common.shared.ViewerStructure.ViewerUserStructure;
import com.databasepreservation.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.shared.client.common.RightPanel;
import com.databasepreservation.common.shared.client.common.lists.BasicTablePanel;
import com.databasepreservation.common.shared.client.tools.BreadcrumbManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DatabaseUsersPanel extends RightPanel {
  private static final ClientMessages messages = com.google.gwt.core.shared.GWT.create(ClientMessages.class);
  private static Map<String, DatabaseUsersPanel> instances = new HashMap<>();

  public static DatabaseUsersPanel getInstance(ViewerDatabase database) {
    String code = database.getUUID();
    instances.computeIfAbsent(code, k -> new DatabaseUsersPanel(database));
    return instances.get(code);
  }

  interface DatabaseUsersPanelUiBinder extends UiBinder<Widget, DatabaseUsersPanel> {
  }

  private static DatabaseUsersPanelUiBinder uiBinder = GWT.create(DatabaseUsersPanelUiBinder.class);

  private ViewerDatabase database;

  @UiField
  FlowPanel contentItems;

  @UiField
  Label title;

  private DatabaseUsersPanel(ViewerDatabase database) {
    initWidget(uiBinder.createAndBindUi(this));

    this.database = database;
    init();
  }

  private void init() {
    title.setText(messages.menusidebar_usersRoles());

    ViewerMetadata metadata = database.getMetadata();

    FlowPanel users = new FlowPanel();
    users.addStyleName("card");
    users.add(getBasicTablePanelForUsers(metadata));

    FlowPanel roles = new FlowPanel();
    roles.addStyleName("card");
    roles.add(getBasicTablePanelForRoles(metadata));

    FlowPanel privileges = new FlowPanel();
    privileges.addStyleName("card");
    privileges.add(getBasicTablePanelForPrivileges(metadata));


    contentItems.add(users);
    contentItems.add(roles);
    contentItems.add(privileges);
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
      BreadcrumbManager.updateBreadcrumb(breadcrumb,
          BreadcrumbManager.forDesktopDatabaseUsers(database.getMetadata().getName(), database.getUUID()));
  }

  private BasicTablePanel<ViewerUserStructure> getBasicTablePanelForUsers(ViewerMetadata metadata) {
    final List<ViewerUserStructure> users = metadata.getUsers();

    Label header = new Label(messages.titleUsers());
    header.addStyleName("h4");

    HTMLPanel info = new HTMLPanel("");
    if (users.isEmpty()) {
      return new BasicTablePanel<>(header, messages.databaseDoesNotContainUsers());
    } else {
      return new BasicTablePanel<>(header, info, users.iterator(),

        new BasicTablePanel.ColumnInfo<>(messages.name(), 15, new TextColumn<ViewerUserStructure>() {
          @Override
          public String getValue(ViewerUserStructure user) {
            return user.getName();
          }
        }),

        new BasicTablePanel.ColumnInfo<>(messages.titleDescription(), 35, new TextColumn<ViewerUserStructure>() {
          @Override
          public String getValue(ViewerUserStructure user) {
            return user.getDescription();
          }
        })

      );
    }
  }

  private BasicTablePanel<ViewerRoleStructure> getBasicTablePanelForRoles(ViewerMetadata metadata) {
    final List<ViewerRoleStructure> roles = metadata.getRoles();

    Label header = new Label(messages.titleRoles());
    header.addStyleName("h4");

    HTMLPanel info = new HTMLPanel("");

    if (roles.isEmpty()) {
      return new BasicTablePanel<>(header, messages.databaseDoesNotContainRoles());
    } else {
      return new BasicTablePanel<>(header, info, roles.iterator(),

        new BasicTablePanel.ColumnInfo<>(messages.name(), 15, new TextColumn<ViewerRoleStructure>() {
          @Override
          public String getValue(ViewerRoleStructure role) {
            return role.getName();
          }
        }),

        new BasicTablePanel.ColumnInfo<>(messages.titleAdmin(), 15, new TextColumn<ViewerRoleStructure>() {
          @Override
          public String getValue(ViewerRoleStructure role) {
            return role.getAdmin();
          }
        }),

        new BasicTablePanel.ColumnInfo<>(messages.titleDescription(), 35, new TextColumn<ViewerRoleStructure>() {
          @Override
          public String getValue(ViewerRoleStructure role) {
            return role.getDescription();
          }
        })

      );
    }
  }

  private BasicTablePanel<ViewerPrivilegeStructure> getBasicTablePanelForPrivileges(ViewerMetadata metadata) {
    final List<ViewerPrivilegeStructure> privileges = metadata.getPrivileges();

    Label header = new Label(messages.titlePrivileges());
    header.addStyleName("h4");

    HTMLPanel info = new HTMLPanel("");

    if (privileges.isEmpty()) {
      return new BasicTablePanel<>(header, messages.databaseDoesNotContainPrivileges());
    } else {
      return new BasicTablePanel<>(header, info, privileges.iterator(),

        new BasicTablePanel.ColumnInfo<>(messages.titleType(), 15, new TextColumn<ViewerPrivilegeStructure>() {
          @Override
          public String getValue(ViewerPrivilegeStructure privilege) {
            return privilege.getType();
          }
        }),

        new BasicTablePanel.ColumnInfo<>(messages.titleGrantor(), 15, new TextColumn<ViewerPrivilegeStructure>() {
          @Override
          public String getValue(ViewerPrivilegeStructure privilege) {
            return privilege.getGrantor();
          }
        }),

        new BasicTablePanel.ColumnInfo<>(messages.titleGrantee(), 15, new TextColumn<ViewerPrivilegeStructure>() {
          @Override
          public String getValue(ViewerPrivilegeStructure privilege) {
            return privilege.getGrantee();
          }
        }),

        new BasicTablePanel.ColumnInfo<>(messages.titleObject(), 15, new TextColumn<ViewerPrivilegeStructure>() {
          @Override
          public String getValue(ViewerPrivilegeStructure privilege) {
            return privilege.getObject();
          }
        }),

        new BasicTablePanel.ColumnInfo<>(messages.titleOption(), 15, new TextColumn<ViewerPrivilegeStructure>() {
          @Override
          public String getValue(ViewerPrivilegeStructure privilege) {
            return privilege.getOption();
          }
        }),

        new BasicTablePanel.ColumnInfo<>(messages.titleDescription(), 35, new TextColumn<ViewerPrivilegeStructure>() {
          @Override
          public String getValue(ViewerPrivilegeStructure privilege) {
            return privilege.getDescription();
          }
        })

      );
    }
  }
}
