package com.databasepreservation.main.desktop.client.dbptk.metadata.schemas.views;

import com.databasepreservation.main.common.shared.ViewerStructure.*;
import com.databasepreservation.main.common.shared.client.common.utils.JavascriptUtils;
import com.databasepreservation.main.desktop.client.common.EditableCell;
import com.databasepreservation.main.desktop.client.common.lists.MetadataTableList;
import com.databasepreservation.main.desktop.client.dbptk.metadata.MetadataEditPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import config.i18n.client.ClientMessages;

import java.util.List;

public class MetadataViewColumns implements MetadataEditPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private ViewerSIARDBundle SIARDbundle;
  private ViewerSchema schema;
  private ViewerView view;

  public MetadataViewColumns(ViewerSIARDBundle SIARDbundle, ViewerSchema schema, ViewerView view) {
    this.SIARDbundle = SIARDbundle;
    this.schema = schema;
    this.view = view;
  }

  @Override
  public MetadataTableList createTable() {
    List<ViewerColumn> columns = view.getColumns();

    if (columns.isEmpty()) {
      return new MetadataTableList<>(messages.tableDoesNotContainColumns());
    } else {
      return new MetadataTableList<>(columns.iterator(),
        new MetadataTableList.ColumnInfo<>(messages.columnName(), 15, new TextColumn<ViewerColumn>() {
          @Override
          public String getValue(ViewerColumn object) {
            return object.getDisplayName();
          }
        }), new MetadataTableList.ColumnInfo<>(messages.typeName(), 15, new TextColumn<ViewerColumn>() {
          @Override
          public String getValue(ViewerColumn object) {
            return object.getType().getTypeName();
          }
        }), new MetadataTableList.ColumnInfo<>(messages.originalTypeName(), 15, new TextColumn<ViewerColumn>() {
          @Override
          public String getValue(ViewerColumn object) {
            return object.getType().getOriginalTypeName();
          }
        }), new MetadataTableList.ColumnInfo<>(messages.nullable(), 7, new TextColumn<ViewerColumn>() {
          @Override
          public String getValue(ViewerColumn object) {
            return object.getNillable() ? "YES" : "NO";
          }
        }), new MetadataTableList.ColumnInfo<>(messages.description(), 15, getDescriptionColumn()));
    }
  }

  @Override
  public Column<ViewerColumn, String> getDescriptionColumn() {
    Column<ViewerColumn, String> description = new Column<ViewerColumn, String>(new EditableCell()) {
      @Override
      public String getValue(ViewerColumn object) {
        return object.getDescription();
      }
    };

    description.setFieldUpdater((index, object, value) -> {
      object.setDescription(value);
      updateSIARDbundle(object.getDisplayName(), value);
    });
    return description;
  }

  @Override
  public void updateSIARDbundle(String name, String value) {
    SIARDbundle.setViewColumn(schema.getName(), view.getName(), name, value);
    JavascriptUtils.alertUpdatedMetadata();
  }
}
