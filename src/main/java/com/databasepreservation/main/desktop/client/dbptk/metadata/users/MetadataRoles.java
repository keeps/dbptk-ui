package com.databasepreservation.main.desktop.client.dbptk.metadata.users;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerColumn;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerRoleStructure;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSIARDBundle;
import com.databasepreservation.main.desktop.client.common.lists.MetadataTableList;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import config.i18n.client.ClientMessages;

import java.util.List;

public class MetadataRoles {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private ViewerSIARDBundle SIARDbundle;

  public MetadataRoles(ViewerSIARDBundle SIARDbundle) {
    this.SIARDbundle = SIARDbundle;
  }

  public MetadataTableList createTable(List<ViewerRoleStructure> roles) {
    Label header = new Label(messages.titleRoles());
    header.addStyleName("h4");

    HTMLPanel info = new HTMLPanel("");

    MetadataTableList<ViewerRoleStructure> roleMetadata;
    if (roles.isEmpty()) {
      roleMetadata = new MetadataTableList<>(header, messages.databaseDoesNotContainRoles());
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
          SIARDbundle.setRole(object.getName(), object.getDescription());

        }
      });

      roleMetadata = new MetadataTableList<>(header, info, roles.iterator(),

        new MetadataTableList.ColumnInfo<>(messages.name(), 15, new TextColumn<ViewerRoleStructure>() {
          @Override
          public String getValue(ViewerRoleStructure role) {
            return role.getName();
          }
        }),

        new MetadataTableList.ColumnInfo<>(messages.titleAdmin(), 15, new TextColumn<ViewerRoleStructure>() {
          @Override
          public String getValue(ViewerRoleStructure role) {
            return role.getAdmin();
          }
        }),

        new MetadataTableList.ColumnInfo<>(messages.titleDescription(), 15, descriptionRole));
    }

    return roleMetadata;
  }
}
