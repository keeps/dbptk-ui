package com.databasepreservation.main.desktop.client.dbptk.metadata;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerMetadata;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerPrivilegeStructure;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerRoleStructure;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerUserStructure;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.main.common.shared.client.common.RightPanel;
import com.databasepreservation.main.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.main.common.shared.client.widgets.Toast;
import com.databasepreservation.main.desktop.client.common.lists.BasicTablePanel;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class EditMetadataUsers extends MetadataRightPanel {

  interface EditMetadataUsersUiBinder extends UiBinder<Widget, EditMetadataUsers> {

  }

  private static EditMetadataUsersUiBinder uiBinder = GWT.create(EditMetadataUsersUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, EditMetadataUsers> instances = new HashMap<>();
  private ViewerDatabase database;
  private ViewerMetadata metadata = null;
  private final Map<String, String> SIARDbundle;
  private List<ViewerUserStructure> users;
  private List<ViewerRoleStructure> roles;
  private List<ViewerPrivilegeStructure> privileges;

  public static EditMetadataUsers getInstance(ViewerDatabase database, Map<String, String> SIARDbundle) {
    String code = database.getUUID();

    EditMetadataUsers instance = instances.get(code);
    if (instance == null) {
      instance = new EditMetadataUsers(database, SIARDbundle);
      instances.put(code, instance);
    }

    return instance;
  }

  @UiField
  FlowPanel contentItems;

  // @UiField
  // Button buttonCancel, buttonSave;

  private EditMetadataUsers(ViewerDatabase database, Map<String, String> SIARDbundle) {
    this.database = database;
    this.SIARDbundle = SIARDbundle;
    initWidget(uiBinder.createAndBindUi(this));

    init();
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forSIARDEditMetadataPage());
  }

  private void init() {
    GWT.log("Edit Metadata Users init ");
    metadata = database.getMetadata();

    contentItems.add(getBasicTablePanelForUsers(metadata));
    contentItems.add(getBasicTablePanelForRoles(metadata));
    contentItems.add(getBasicTablePanelForPrivileges(metadata));
  }

  private BasicTablePanel<ViewerUserStructure> getBasicTablePanelForUsers(ViewerMetadata metadata) {
    users = metadata.getUsers();

    Label header = new Label(messages.titleUsers());
    header.addStyleName("h4");

    HTMLPanel info = new HTMLPanel("");

    BasicTablePanel<ViewerUserStructure> userMetadata;
    if (users.isEmpty()) {
      userMetadata = new BasicTablePanel<>(header, messages.databaseDoesNotContainUsers());
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
          SIARDbundle.put("user:" + object.getName(), "description---" + object.getDescription());

        }
      });

      userMetadata = new BasicTablePanel<>(header, info, users.iterator(),

        new BasicTablePanel.ColumnInfo<>(messages.name(), 15, new TextColumn<ViewerUserStructure>() {
          @Override
          public String getValue(ViewerUserStructure user) {
            return user.getName();
          }
        }), new BasicTablePanel.ColumnInfo<>(messages.description(), 15, descriptionUser));

    }

    return userMetadata;
  }

  private BasicTablePanel<ViewerRoleStructure> getBasicTablePanelForRoles(ViewerMetadata metadata) {
    roles = metadata.getRoles();

    Label header = new Label(messages.titleRoles());
    header.addStyleName("h4");

    HTMLPanel info = new HTMLPanel("");

    BasicTablePanel<ViewerRoleStructure> roleMetadata;
    if (roles.isEmpty()) {
      roleMetadata = new BasicTablePanel<>(header, messages.databaseDoesNotContainRoles());
    } else {
      Column<ViewerRoleStructure, String> descriptionRole = new Column<ViewerRoleStructure, String>(
        new EditTextCell()) {
        @Override
        public String getValue(ViewerRoleStructure role) {
          return role.getDescription() == null ? messages.metadataDoesNotContainDescription() : role.getDescription();
        }
      };

      descriptionRole.setFieldUpdater(new FieldUpdater<ViewerRoleStructure, String>() {
        @Override
        public void update(int index, ViewerRoleStructure object, String value) {

          object.setDescription(value);
          SIARDbundle.put("role:" + object.getName(), "description---" + object.getDescription());

        }
      });

      roleMetadata = new BasicTablePanel<>(header, info, roles.iterator(),

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

        new BasicTablePanel.ColumnInfo<>(messages.titleDescription(), 15, descriptionRole));
    }

    return roleMetadata;
  }

  private BasicTablePanel<ViewerPrivilegeStructure> getBasicTablePanelForPrivileges(ViewerMetadata metadata) {
    privileges = metadata.getPrivileges();

    Label header = new Label(messages.titlePrivileges());
    header.addStyleName("h4");

    HTMLPanel info = new HTMLPanel("");

    BasicTablePanel<ViewerPrivilegeStructure> privilegeMetadata;
    if (privileges.isEmpty()) {
      privilegeMetadata = new BasicTablePanel<>(header, messages.databaseDoesNotContainPrivileges());
    } else {
      Column<ViewerPrivilegeStructure, String> descriptionPrivileges = new Column<ViewerPrivilegeStructure, String>(
        new EditTextCell()) {
        @Override
        public String getValue(ViewerPrivilegeStructure privilege) {

          return privilege.getDescription() == null ? messages.metadataDoesNotContainDescription()
            : privilege.getDescription();
        }
      };

      descriptionPrivileges.setFieldUpdater(new FieldUpdater<ViewerPrivilegeStructure, String>() {
        @Override
        public void update(int index, ViewerPrivilegeStructure object, String value) {
          object.setDescription(value);
          SIARDbundle
            .put(
              "privileges:[type:" + object.getType() + " object:" + object.getObject() + " grantor:"
                + object.getGrantor() + " grantee:" + object.getGrantee() + "]",
              "description---" + object.getDescription());
        }
      });

      privilegeMetadata = new BasicTablePanel<>(header, info, privileges.iterator(),

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

        new BasicTablePanel.ColumnInfo<>(messages.titleDescription(), 15, descriptionPrivileges));
    }

    return privilegeMetadata;
  }

  private Map<String, String> updateSiardBundle() {
    GWT.log("updateSiardMetadata");
    Map<String, String> bundle = new HashMap<>();

    users.forEach((user) -> {
      bundle.put("user:" + user.getName(), "description---" + user.getDescription());
    });

    roles.forEach((role) -> {
      bundle.put("roles:" + role.getName(), "description---" + role.getDescription());
    });

    privileges.forEach((privilege) -> {
      bundle.put("privileges:" + privilege.getType(), "description---" + privilege.getDescription());
    });

    for (Map.Entry<String, String> entry : bundle.entrySet()) {
      GWT.log(entry.getKey() + "/" + entry.getValue());
    }

    return bundle;
  }
}