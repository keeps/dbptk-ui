package com.databasepreservation.main.desktop.client.dbptk.metadata.users;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerColumn;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerRoleStructure;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSIARDBundle;
import com.databasepreservation.main.desktop.client.common.EditableCell;
import com.databasepreservation.main.desktop.client.common.lists.MetadataTableList;
import com.databasepreservation.main.desktop.client.dbptk.metadata.MetadataEditPanel;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import config.i18n.client.ClientMessages;

import java.util.List;

public class MetadataRoles implements MetadataEditPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private ViewerSIARDBundle SIARDbundle;
  private List<ViewerRoleStructure> roles;

  public MetadataRoles(ViewerSIARDBundle SIARDbundle, List<ViewerRoleStructure> roles) {
    this.SIARDbundle = SIARDbundle;
    this.roles = roles;
  }

  @Override
  public MetadataTableList createTable() {
    MetadataTableList<ViewerRoleStructure> roleMetadata;
    if (roles.isEmpty()) {
      roleMetadata = new MetadataTableList<>(messages.databaseDoesNotContainRoles());
    } else {

      roleMetadata = new MetadataTableList<>(roles.iterator(),

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

        new MetadataTableList.ColumnInfo<>(messages.titleDescription(), 15, getDescriptionColumn()));
    }

    return roleMetadata;
  }

  @Override
  public Column<ViewerRoleStructure, String> getDescriptionColumn() {
    Column<ViewerRoleStructure, String> description = new Column<ViewerRoleStructure, String>(
            new EditableCell()) {
      @Override
      public String getValue(ViewerRoleStructure object) { return object.getDescription(); }
    };

    description.setFieldUpdater(new FieldUpdater<ViewerRoleStructure, String>() {
      @Override
      public void update(int index, ViewerRoleStructure object, String value) {

        object.setDescription(value);
        updateSIARDbundle(object.getName(), object.getDescription());

      }
    });
    return description;
  }

  @Override
  public void updateSIARDbundle(String name, String value) {
    SIARDbundle.setRole(name, value);
  }
}
