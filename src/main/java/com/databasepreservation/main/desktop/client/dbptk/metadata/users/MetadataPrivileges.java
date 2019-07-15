package com.databasepreservation.main.desktop.client.dbptk.metadata.users;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerPrivilegeStructure;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSIARDBundle;
import com.databasepreservation.main.common.shared.client.common.utils.JavascriptUtils;
import com.databasepreservation.main.desktop.client.common.EditableCell;
import com.databasepreservation.main.desktop.client.common.lists.MetadataTableList;
import com.databasepreservation.main.desktop.client.dbptk.metadata.MetadataEditPanel;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import config.i18n.client.ClientMessages;

import java.util.List;

public class MetadataPrivileges implements MetadataEditPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private ViewerSIARDBundle SIARDbundle;
  private List<ViewerPrivilegeStructure> privileges;

  public MetadataPrivileges(ViewerSIARDBundle SIARDbundle, List<ViewerPrivilegeStructure> privileges) {
    this.SIARDbundle = SIARDbundle;
    this.privileges = privileges;
  }

  @Override
  public MetadataTableList createTable() {
    MetadataTableList<ViewerPrivilegeStructure> privilegeMetadata;
    if (privileges.isEmpty()) {
      privilegeMetadata = new MetadataTableList<>(messages.databaseDoesNotContainPrivileges());
    } else {

      privilegeMetadata = new MetadataTableList<>(privileges.iterator(),

        new MetadataTableList.ColumnInfo<>(messages.titleType(), 15, new TextColumn<ViewerPrivilegeStructure>() {
          @Override
          public String getValue(ViewerPrivilegeStructure privilege) {
            return privilege.getType();
          }
        }),

        new MetadataTableList.ColumnInfo<>(messages.titleGrantor(), 15, new TextColumn<ViewerPrivilegeStructure>() {
          @Override
          public String getValue(ViewerPrivilegeStructure privilege) {
            return privilege.getGrantor();
          }
        }),

        new MetadataTableList.ColumnInfo<>(messages.titleGrantee(), 15, new TextColumn<ViewerPrivilegeStructure>() {
          @Override
          public String getValue(ViewerPrivilegeStructure privilege) {
            return privilege.getGrantee();
          }
        }),

        new MetadataTableList.ColumnInfo<>(messages.titleObject(), 15, new TextColumn<ViewerPrivilegeStructure>() {
          @Override
          public String getValue(ViewerPrivilegeStructure privilege) {
            return privilege.getObject();
          }
        }),

        new MetadataTableList.ColumnInfo<>(messages.titleOption(), 15, new TextColumn<ViewerPrivilegeStructure>() {
          @Override
          public String getValue(ViewerPrivilegeStructure privilege) {
            return privilege.getOption();
          }
        }),

        new MetadataTableList.ColumnInfo<>(messages.titleDescription(), 15, getDescriptionColumn()));
    }

    return privilegeMetadata;
  }

  @Override
  public Column<ViewerPrivilegeStructure, String> getDescriptionColumn() {
    Column<ViewerPrivilegeStructure, String> description = new Column<ViewerPrivilegeStructure, String>(
            new EditableCell()) {
      @Override
      public String getValue(ViewerPrivilegeStructure object) { return object.getDescription(); }
    };

    description.setFieldUpdater((index, object, value) -> {
      object.setDescription(value);
      SIARDbundle.setPrivileges(object.getType(), object.getObject(), object.getGrantor(), object.getGrantee(),
              object.getDescription());
      JavascriptUtils.alertUpdatedMetadata();
    });

    return description;
  }

  @Override
  public void updateSIARDbundle(String name, String value) {}
}
