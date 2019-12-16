package com.databasepreservation.common.client.common.visualization.metadata.users;

import java.util.List;

import com.databasepreservation.common.client.common.EditableCell;
import com.databasepreservation.common.client.common.lists.widgets.MetadataTableList;
import com.databasepreservation.common.client.common.visualization.metadata.MetadataControlPanel;
import com.databasepreservation.common.client.common.visualization.metadata.MetadataEditPanel;
import com.databasepreservation.common.client.models.structure.ViewerRoleStructure;
import com.databasepreservation.common.client.models.structure.ViewerSIARDBundle;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;

import config.i18n.client.ClientMessages;

public class MetadataRoles implements MetadataEditPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private ViewerSIARDBundle SIARDbundle;
  private List<ViewerRoleStructure> roles;
  private MetadataControlPanel controls;

  public MetadataRoles(ViewerSIARDBundle SIARDbundle, List<ViewerRoleStructure> roles, MetadataControlPanel controls) {
    this.SIARDbundle = SIARDbundle;
    this.roles = roles;
    this.controls = controls;
  }

  @Override
  public MetadataTableList createTable() {
    MetadataTableList<ViewerRoleStructure> roleMetadata;
    if (roles.isEmpty()) {
      roleMetadata = new MetadataTableList<>(messages.databaseDoesNotContainRoles());
    } else {

      roleMetadata = new MetadataTableList<>(roles.iterator(),

        new MetadataTableList.ColumnInfo<>(messages.name(), 7, new TextColumn<ViewerRoleStructure>() {
          @Override
          public String getValue(ViewerRoleStructure role) {
            return role.getName();
          }
        }),

        new MetadataTableList.ColumnInfo<>(messages.titleAdmin(), 7, new TextColumn<ViewerRoleStructure>() {
          @Override
          public String getValue(ViewerRoleStructure role) {
            return role.getAdmin();
          }
        }),

        new MetadataTableList.ColumnInfo<>(messages.titleDescription(), 25, getDescriptionColumn()));
    }

    return roleMetadata;
  }

  @Override
  public Column<ViewerRoleStructure, String> getDescriptionColumn() {
    Column<ViewerRoleStructure, String> description = new Column<ViewerRoleStructure, String>(new EditableCell() {
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
      public String getValue(ViewerRoleStructure object) {
        return object.getDescription();
      }
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
    controls.validate();
  }
}
