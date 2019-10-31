package com.databasepreservation.common.shared.client.common.visualization.metadata.users;

import java.util.List;

import com.databasepreservation.common.shared.ViewerStructure.ViewerSIARDBundle;
import com.databasepreservation.common.shared.ViewerStructure.ViewerUserStructure;
import com.databasepreservation.common.shared.client.common.EditableCell;
import com.databasepreservation.common.shared.client.common.lists.MetadataTableList;
import com.databasepreservation.common.shared.client.common.visualization.metadata.MetadataControlPanel;
import com.databasepreservation.common.shared.client.common.visualization.metadata.MetadataEditPanel;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;

import config.i18n.client.ClientMessages;

public class MetadataUsers implements MetadataEditPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private ViewerSIARDBundle SIARDbundle;
  private List<ViewerUserStructure> users;
  private MetadataControlPanel controls;

  public MetadataUsers(ViewerSIARDBundle SIARDbundle, List<ViewerUserStructure> users, MetadataControlPanel controls) {
    this.SIARDbundle = SIARDbundle;
    this.users = users;
    this.controls = controls;
  }

  @Override
  public MetadataTableList createTable() {
    MetadataTableList<ViewerUserStructure> userMetadata;
    if (users.isEmpty()) {
      userMetadata = new MetadataTableList<>(messages.databaseDoesNotContainUsers());
    } else {

      userMetadata = new MetadataTableList<>(users.iterator(),

        new MetadataTableList.ColumnInfo<>(messages.name(),7, new TextColumn<ViewerUserStructure>() {
          @Override
          public String getValue(ViewerUserStructure user) {
            return user.getName();
          }
        }), new MetadataTableList.ColumnInfo<>(messages.description(), 25, getDescriptionColumn()));
    }

    return userMetadata;
  }

  @Override
  public Column<ViewerUserStructure, String> getDescriptionColumn() {
    Column<ViewerUserStructure, String> description = new Column<ViewerUserStructure, String>(new EditableCell() {
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
      public String getValue(ViewerUserStructure object) {
        return object.getDescription();
      }
    };

    description.setFieldUpdater(new FieldUpdater<ViewerUserStructure, String>() {
      @Override
      public void update(int index, ViewerUserStructure object, String value) {

        object.setDescription(value);
        updateSIARDbundle(object.getName(), object.getDescription());

      }
    });
    return description;
  }

  @Override
  public void updateSIARDbundle(String name, String value) {
    SIARDbundle.setUser(name, value);
    controls.validate();
  }
}
