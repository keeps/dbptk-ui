package com.databasepreservation.main.desktop.client.dbptk.metadata.users;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerPrivilegeStructure;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSIARDBundle;
import com.databasepreservation.main.desktop.client.common.lists.MetadataTableList;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import config.i18n.client.ClientMessages;

import java.util.List;

public class MetadataPrivileges {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private ViewerSIARDBundle SIARDbundle;

  public MetadataPrivileges(ViewerSIARDBundle SIARDbundle) {
    this.SIARDbundle = SIARDbundle;
  }

  public MetadataTableList createTable(List<ViewerPrivilegeStructure> privileges) {
    Label header = new Label(messages.titlePrivileges());
    header.addStyleName("h4");

    HTMLPanel info = new HTMLPanel("");

    MetadataTableList<ViewerPrivilegeStructure> privilegeMetadata;
    if (privileges.isEmpty()) {
      privilegeMetadata = new MetadataTableList<>(header, messages.databaseDoesNotContainPrivileges());
    } else {
      Column<ViewerPrivilegeStructure, String> descriptionPrivileges = new Column<ViewerPrivilegeStructure, String>(
        new EditTextCell()) {
        @Override
        public String getValue(ViewerPrivilegeStructure privilege) {

          return privilege.getDescription() == null ? messages.metadataDoesNotContainDescription()
            : privilege.getDescription();
        }
      };

      descriptionPrivileges.setFieldUpdater((index, object, value) -> {
        object.setDescription(value);
        SIARDbundle.setPrivileges(object.getType(), object.getObject(), object.getGrantor(), object.getGrantee(),
          object.getDescription());
      });

      privilegeMetadata = new MetadataTableList<>(header, info, privileges.iterator(),

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

        new MetadataTableList.ColumnInfo<>(messages.titleDescription(), 15, descriptionPrivileges));
    }

    return privilegeMetadata;
  }
}
