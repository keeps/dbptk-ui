/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.visualization.browse.technicalInformation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.common.RightPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.lists.widgets.BasicTablePanel;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerPrivilegeStructure;
import com.databasepreservation.common.client.models.structure.ViewerRoleStructure;
import com.databasepreservation.common.client.models.structure.ViewerUserStructure;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
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
public class UsersPanel extends RightPanel {
  private static final ClientMessages messages = com.google.gwt.core.shared.GWT.create(ClientMessages.class);
  private static Map<String, UsersPanel> instances = new HashMap<>();

  public static UsersPanel getInstance(ViewerDatabase database) {
    return instances.computeIfAbsent(database.getUuid(), k -> new UsersPanel(database));
  }

  interface DatabaseUsersPanelUiBinder extends UiBinder<Widget, UsersPanel> {
  }

  private static DatabaseUsersPanelUiBinder uiBinder = GWT.create(DatabaseUsersPanelUiBinder.class);

  private ViewerDatabase database;

  @UiField
  FlowPanel content;

  @UiField
  FlowPanel header;

  private UsersPanel(ViewerDatabase database) {
    initWidget(uiBinder.createAndBindUi(this));

    this.database = database;
    init();
  }

  private void init() {
    header.add(CommonClientUtils.getHeaderHTML(FontAwesomeIconManager.getTag(FontAwesomeIconManager.DATABASE_USERS),
      messages.menusidebar_usersRoles(), "h1"));

    MetadataField instance = MetadataField.createInstance(messages.includingStoredProceduresAndFunctions());
    instance.setCSS("table-row-description", "font-size-description");
    content.add(instance);

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


    content.add(users);
    content.add(roles);
    content.add(privileges);
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
      BreadcrumbManager.updateBreadcrumb(breadcrumb,
          BreadcrumbManager.forDatabaseUsers(database.getMetadata().getName(), database.getUuid()));
  }

  private BasicTablePanel<ViewerUserStructure> getBasicTablePanelForUsers(ViewerMetadata metadata) {
    final List<ViewerUserStructure> users = metadata.getUsers();

    Label header = new Label(messages.titleUsers());
    header.addStyleName("card-header");

    if (users.isEmpty()) {
      return new BasicTablePanel<>(header, messages.databaseDoesNotContainUsers());
    } else {
      return new BasicTablePanel<ViewerUserStructure>(header, SafeHtmlUtils.EMPTY_SAFE_HTML, users.iterator(),

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
    header.addStyleName("card-header");

    HTMLPanel info = new HTMLPanel("");

    if (roles.isEmpty()) {
      return new BasicTablePanel<>(header, messages.databaseDoesNotContainRoles());
    } else {
      return new BasicTablePanel<ViewerRoleStructure>(header, info, roles.iterator(),

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
    header.addStyleName("card-header");

    HTMLPanel info = new HTMLPanel("");

    if (privileges.isEmpty()) {
      return new BasicTablePanel<>(header, messages.databaseDoesNotContainPrivileges());
    } else {
      return new BasicTablePanel<ViewerPrivilegeStructure>(header, info, privileges.iterator(),

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
