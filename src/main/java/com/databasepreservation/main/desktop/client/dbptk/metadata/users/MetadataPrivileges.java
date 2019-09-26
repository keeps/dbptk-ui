package com.databasepreservation.main.desktop.client.dbptk.metadata.users;

import java.util.List;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerPrivilegeStructure;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSIARDBundle;
import com.databasepreservation.main.desktop.client.common.EditableCell;
import com.databasepreservation.main.desktop.client.common.lists.MetadataTableList;
import com.databasepreservation.main.desktop.client.dbptk.metadata.MetadataControlPanel;
import com.databasepreservation.main.desktop.client.dbptk.metadata.MetadataEditPanel;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;

import config.i18n.client.ClientMessages;

public class MetadataPrivileges implements MetadataEditPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private ViewerSIARDBundle SIARDbundle;
  private List<ViewerPrivilegeStructure> privileges;
  private MetadataControlPanel controls;

  public MetadataPrivileges(ViewerSIARDBundle SIARDbundle, List<ViewerPrivilegeStructure> privileges,
    MetadataControlPanel controls) {
    this.SIARDbundle = SIARDbundle;
    this.privileges = privileges;
    this.controls = controls;
  }

  @Override
  public MetadataTableList createTable() {
    MetadataTableList<ViewerPrivilegeStructure> privilegeMetadata;
    if (privileges.isEmpty()) {
      privilegeMetadata = new MetadataTableList<>(messages.databaseDoesNotContainPrivileges());
    } else {

      privilegeMetadata = new MetadataTableList<>(privileges.iterator(),

        new MetadataTableList.ColumnInfo<>(messages.titleType(), 5, new TextColumn<ViewerPrivilegeStructure>() {
          @Override
          public String getValue(ViewerPrivilegeStructure privilege) {
            return privilege.getType();
          }
        }),

        new MetadataTableList.ColumnInfo<>(messages.titleGrantor(), 7, new TextColumn<ViewerPrivilegeStructure>() {
          @Override
          public String getValue(ViewerPrivilegeStructure privilege) {
            return privilege.getGrantor();
          }
        }),

        new MetadataTableList.ColumnInfo<>(messages.titleGrantee(), 7, new TextColumn<ViewerPrivilegeStructure>() {
          @Override
          public String getValue(ViewerPrivilegeStructure privilege) {
            return privilege.getGrantee();
          }
        }),

        new MetadataTableList.ColumnInfo<>(messages.titleObject(), 7, new TextColumn<ViewerPrivilegeStructure>() {
          @Override
          public String getValue(ViewerPrivilegeStructure privilege) {
            return privilege.getObject();
          }
        }),

        new MetadataTableList.ColumnInfo<>(messages.titleOption(), 7, new TextColumn<ViewerPrivilegeStructure>() {
          @Override
          public String getValue(ViewerPrivilegeStructure privilege) {
            return privilege.getOption();
          }
        }),

        new MetadataTableList.ColumnInfo<>(messages.titleDescription(), 25, getDescriptionColumn()));
    }

    return privilegeMetadata;
  }

  @Override
  public Column<ViewerPrivilegeStructure, String> getDescriptionColumn() {
    Column<ViewerPrivilegeStructure, String> description = new Column<ViewerPrivilegeStructure, String>(
      new EditableCell() {
        @Override
        public void onBrowserEvent(Context context, Element parent, String value, NativeEvent event,
          ValueUpdater<String> valueUpdater) {
          if (BrowserEvents.KEYUP.equals(event.getType())) {
            controls.validate();
          }
          super.onBrowserEvent(context, parent, value, event, valueUpdater);
        }
      }) {
      @Override
      public String getValue(ViewerPrivilegeStructure object) {
        return object.getDescription();
      }
    };

    description.setFieldUpdater((index, object, value) -> {
      object.setDescription(value);
      SIARDbundle.setPrivileges(object.getType(), object.getObject(), object.getGrantor(), object.getGrantee(),
        object.getDescription());
      controls.validate();
    });

    return description;
  }

  @Override
  public void updateSIARDbundle(String name, String value) {
  }
}
