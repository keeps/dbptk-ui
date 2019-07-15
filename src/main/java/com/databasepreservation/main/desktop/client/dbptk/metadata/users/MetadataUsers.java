package com.databasepreservation.main.desktop.client.dbptk.metadata.users;

import java.util.List;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSIARDBundle;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerUserStructure;
import com.databasepreservation.main.common.shared.client.common.utils.JavascriptUtils;
import com.databasepreservation.main.desktop.client.common.EditableCell;
import com.databasepreservation.main.desktop.client.common.lists.MetadataTableList;
import com.databasepreservation.main.desktop.client.dbptk.metadata.MetadataEditPanel;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

import config.i18n.client.ClientMessages;

public class MetadataUsers implements MetadataEditPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private ViewerSIARDBundle SIARDbundle;
  private List<ViewerUserStructure> users;

  public MetadataUsers(ViewerSIARDBundle SIARDbundle, List<ViewerUserStructure> users) {
    this.SIARDbundle = SIARDbundle;
    this.users = users;
  }

  @Override
  public MetadataTableList createTable() {
    MetadataTableList<ViewerUserStructure> userMetadata;
    if (users.isEmpty()) {
      userMetadata = new MetadataTableList<>(messages.databaseDoesNotContainUsers());
    } else {

      userMetadata = new MetadataTableList<>(users.iterator(),

        new MetadataTableList.ColumnInfo<>(messages.name(), 15, new TextColumn<ViewerUserStructure>() {
          @Override
          public String getValue(ViewerUserStructure user) {
            return user.getName();
          }
        }), new MetadataTableList.ColumnInfo<>(messages.description(), 15, getDescriptionColumn()));
    }

    return userMetadata;
  }

  @Override
  public Column<ViewerUserStructure, String> getDescriptionColumn() {
    Column<ViewerUserStructure, String> description = new Column<ViewerUserStructure, String>(new EditableCell()) {
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
    JavascriptUtils.alertUpdatedMetadata();
  }
}
