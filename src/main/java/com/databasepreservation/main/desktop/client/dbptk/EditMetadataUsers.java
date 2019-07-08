package com.databasepreservation.main.desktop.client.dbptk;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerMetadata;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerPrivilegeStructure;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerRoleStructure;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerUserStructure;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.common.shared.client.common.RightPanel;
import com.databasepreservation.main.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.main.desktop.client.common.lists.BasicTablePanel;
import com.google.gwt.cell.client.EditTextCell;
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
public class EditMetadataUsers extends RightPanel {

  interface EditMetadataUsersUiBinder extends UiBinder<Widget, EditMetadataUsers> {
  }

  private static EditMetadataUsersUiBinder uiBinder = GWT.create(EditMetadataUsersUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, EditMetadataUsers> instances = new HashMap<>();
  private ViewerDatabase database;
  private ViewerMetadata metadata = null;

  public static EditMetadataUsers getInstance(ViewerDatabase database) {
    String code = database.getUUID();

    EditMetadataUsers instance = instances.get(code);
    if (instance == null) {
      instance = new EditMetadataUsers(database);
      instances.put(code, instance);
    }

    return instance;
  }

  @UiField
  FlowPanel contentItems;

  @UiField
  Button buttonCancel, buttonSave;

  private EditMetadataUsers(ViewerDatabase database) {
    this.database = database;
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
    final List<ViewerUserStructure> users = metadata.getUsers();

    Label header = new Label(messages.titleUsers());
    header.addStyleName("h4");

    HTMLPanel info = new HTMLPanel("");

    if (users.isEmpty()) {
      return new BasicTablePanel<>(header, messages.databaseDoesNotContainUsers());
    } else {
      return new BasicTablePanel<>(header, info, users.iterator(),

        new BasicTablePanel.ColumnInfo<>(messages.name(), 15,
          new Column<ViewerUserStructure, String>(new EditTextCell()) {
            @Override
            public String getValue(ViewerUserStructure user) {
              return user.getName();
            }
          }),
        new BasicTablePanel.ColumnInfo<>(messages.description(), 15,
          new Column<ViewerUserStructure, String>(new EditTextCell()) {
            @Override
            public String getValue(ViewerUserStructure user) {
              return user.getDescription();
            }
          }));
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

        new BasicTablePanel.ColumnInfo<>(messages.name(), 15,
          new Column<ViewerRoleStructure, String>(new EditTextCell()) {
            @Override
            public String getValue(ViewerRoleStructure role) {
              return role.getName();
            }
          }),

        new BasicTablePanel.ColumnInfo<>(messages.titleAdmin(), 15,
          new Column<ViewerRoleStructure, String>(new EditTextCell()) {
            @Override
            public String getValue(ViewerRoleStructure role) {
              return role.getAdmin();
            }
          }),

        new BasicTablePanel.ColumnInfo<>(messages.titleDescription(), 15,
          new Column<ViewerRoleStructure, String>(new EditTextCell()) {
            @Override
            public String getValue(ViewerRoleStructure role) {
              return role.getDescription();
            }
          }));
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

        new BasicTablePanel.ColumnInfo<>(messages.titleType(), 15,
          new Column<ViewerPrivilegeStructure, String>(new EditTextCell()) {
            @Override
            public String getValue(ViewerPrivilegeStructure privilege) {
              return privilege.getType();
            }
          }),

        new BasicTablePanel.ColumnInfo<>(messages.titleGrantor(), 15,
          new Column<ViewerPrivilegeStructure, String>(new EditTextCell()) {
            @Override
            public String getValue(ViewerPrivilegeStructure privilege) {
              return privilege.getGrantor();
            }
          }),

        new BasicTablePanel.ColumnInfo<>(messages.titleGrantee(), 15,
          new Column<ViewerPrivilegeStructure, String>(new EditTextCell()) {
            @Override
            public String getValue(ViewerPrivilegeStructure privilege) {
              return privilege.getGrantee();
            }
          }),

        new BasicTablePanel.ColumnInfo<>(messages.titleObject(), 15,
          new Column<ViewerPrivilegeStructure, String>(new EditTextCell()) {
            @Override
            public String getValue(ViewerPrivilegeStructure privilege) {
              return privilege.getObject();
            }
          }),

        new BasicTablePanel.ColumnInfo<>(messages.titleOption(), 15,
          new Column<ViewerPrivilegeStructure, String>(new EditTextCell()) {
            @Override
            public String getValue(ViewerPrivilegeStructure privilege) {
              return privilege.getOption();
            }
          }),

        new BasicTablePanel.ColumnInfo<>(messages.titleDescription(), 15,
          new Column<ViewerPrivilegeStructure, String>(new EditTextCell()) {
            @Override
            public String getValue(ViewerPrivilegeStructure privilege) {
              return privilege.getDescription();
            }
          })
      );
    }
  }

  private Map<String, String> updateSiardBundle() {
    GWT.log("updateSiardMetadata");
    Map<String, String> bundle = new HashMap<>();

    return bundle;
  }

  private void updateSolrMetadata() {

  }

  @UiHandler("buttonSave")
  void buttonSaveHandler(ClickEvent e) {
    GWT.log("Save Metadata User");

    Map<String, String> bundleSiard = updateSiardBundle();
    updateSolrMetadata();

  }

}