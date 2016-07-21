package com.databasepreservation.visualization.client.browse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.visualization.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerMetadata;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerPrivilegeStructure;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerRoleStructure;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerUserStructure;
import com.databasepreservation.visualization.client.common.lists.BasicTablePanel;
import com.databasepreservation.visualization.client.main.BreadcrumbPanel;
import com.databasepreservation.visualization.shared.client.Tools.BreadcrumbManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DatabaseUsersPanel extends RightPanel {
  private static Map<String, DatabaseUsersPanel> instances = new HashMap<>();

  public static DatabaseUsersPanel getInstance(ViewerDatabase database) {
    String code = database.getUUID();

    DatabaseUsersPanel instance = instances.get(code);
    if (instance == null) {
      instance = new DatabaseUsersPanel(database);
      instances.put(code, instance);
    }
    return instance;
  }

  interface DatabasePanelUiBinder extends UiBinder<Widget, DatabaseUsersPanel> {
  }

  private static DatabasePanelUiBinder uiBinder = GWT.create(DatabasePanelUiBinder.class);

  private ViewerDatabase database;

  @UiField
  FlowPanel contentItems;

  private DatabaseUsersPanel(ViewerDatabase database) {
    initWidget(uiBinder.createAndBindUi(this));

    this.database = database;
    Label tmpNote = new Label("User information will be available on this page in a future release.");
    contentItems.add(tmpNote);
    init();
  }

  private void init() {
    ViewerMetadata metadata = database.getMetadata();
    addBasicTablePanelForUsers(metadata);
    addBasicTablePanelForRoles(metadata);
    addBasicTablePanelForPrivileges(metadata);
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(breadcrumb,
      BreadcrumbManager.forDatabase(database.getMetadata().getName(), database.getUUID()));
  }

  private void addBasicTablePanelForUsers(ViewerMetadata metadata) {
    final List<ViewerUserStructure> users = metadata.getUsers();

    Label header = new Label("Users");
    header.addStyleName("h4");

    HTMLPanel info = new HTMLPanel("");

    contentItems.add(new BasicTablePanel<>(header, info, users.iterator(),

    new BasicTablePanel.ColumnInfo<>("Name", 15, new TextColumn<ViewerUserStructure>() {
      @Override
      public String getValue(ViewerUserStructure user) {
        return user.getName();
      }
    }),

    new BasicTablePanel.ColumnInfo<>("Description", 35, new TextColumn<ViewerUserStructure>() {
      @Override
      public String getValue(ViewerUserStructure user) {
        return user.getDescription();
      }
    })

    ));
  }

  private void addBasicTablePanelForRoles(ViewerMetadata metadata) {
    final List<ViewerRoleStructure> roles = metadata.getRoles();

    Label header = new Label("Roles");
    header.addStyleName("h4");

    HTMLPanel info = new HTMLPanel("");

    contentItems.add(new BasicTablePanel<>(header, info, roles.iterator(),

    new BasicTablePanel.ColumnInfo<>("Name", 15, new TextColumn<ViewerRoleStructure>() {
      @Override
      public String getValue(ViewerRoleStructure role) {
        return role.getName();
      }
    }),

    new BasicTablePanel.ColumnInfo<>("Admin", 15, new TextColumn<ViewerRoleStructure>() {
      @Override
      public String getValue(ViewerRoleStructure role) {
        return role.getAdmin();
      }
    }),

    new BasicTablePanel.ColumnInfo<>("Description", 35, new TextColumn<ViewerRoleStructure>() {
      @Override
      public String getValue(ViewerRoleStructure role) {
        return role.getDescription();
      }
    })

    ));
  }

  private void addBasicTablePanelForPrivileges(ViewerMetadata metadata) {
    final List<ViewerPrivilegeStructure> privileges = metadata.getPrivileges();

    Label header = new Label("Roles");
    header.addStyleName("h4");

    HTMLPanel info = new HTMLPanel("");

    contentItems.add(new BasicTablePanel<>(header, info, privileges.iterator(),

    new BasicTablePanel.ColumnInfo<>("Type", 15, new TextColumn<ViewerPrivilegeStructure>() {
      @Override
      public String getValue(ViewerPrivilegeStructure privilege) {
        return privilege.getType();
      }
    }),

    new BasicTablePanel.ColumnInfo<>("Grantor", 15, new TextColumn<ViewerPrivilegeStructure>() {
      @Override
      public String getValue(ViewerPrivilegeStructure privilege) {
        return privilege.getGrantor();
      }
    }),

    new BasicTablePanel.ColumnInfo<>("Grantee", 15, new TextColumn<ViewerPrivilegeStructure>() {
      @Override
      public String getValue(ViewerPrivilegeStructure privilege) {
        return privilege.getGrantee();
      }
    }),

    new BasicTablePanel.ColumnInfo<>("Object", 15, new TextColumn<ViewerPrivilegeStructure>() {
      @Override
      public String getValue(ViewerPrivilegeStructure privilege) {
        return privilege.getObject();
      }
    }),

    new BasicTablePanel.ColumnInfo<>("Option", 15, new TextColumn<ViewerPrivilegeStructure>() {
      @Override
      public String getValue(ViewerPrivilegeStructure privilege) {
        return privilege.getOption();
      }
    }),

    new BasicTablePanel.ColumnInfo<>("Description", 35, new TextColumn<ViewerPrivilegeStructure>() {
      @Override
      public String getValue(ViewerPrivilegeStructure privilege) {
        return privilege.getDescription();
      }
    })

    ));
  }
}
