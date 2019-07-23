package com.databasepreservation.main.desktop.client.dbptk.metadata.schemas.tables;

import java.util.Arrays;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerPrimaryKey;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSIARDBundle;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.main.desktop.client.common.EditableCell;
import com.databasepreservation.main.desktop.client.common.lists.MetadataTableList;
import com.databasepreservation.main.desktop.client.dbptk.metadata.MetadataControlPanel;
import com.databasepreservation.main.desktop.client.dbptk.metadata.MetadataEditPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MetadataPrimaryKey implements MetadataEditPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private final MetadataControlPanel controls;
  private ViewerSIARDBundle SIARDbundle;
  private ViewerTable table;
  private ViewerSchema schema;
  private String type = "primaryKey";

  MetadataPrimaryKey(ViewerSIARDBundle SIARDbundle, ViewerSchema schema, ViewerTable table, MetadataControlPanel controls) {
    this.SIARDbundle = SIARDbundle;
    this.table = table;
    this.schema = schema;
    this.controls = controls;
  }

  @Override
  public MetadataTableList createTable() {

    ViewerPrimaryKey columns = table.getPrimaryKey();

    if (columns == null) {
      return new MetadataTableList<>(messages.tableDoesNotContainPrimaryKey());
    } else {

      return new MetadataTableList<>(Arrays.asList(columns).iterator(),
        new MetadataTableList.ColumnInfo<>(messages.primaryKey(), 15, new TextColumn<ViewerPrimaryKey>() {
          @Override
          public String getValue(ViewerPrimaryKey object) {
            Integer columnIndex = object.getColumnIndexesInViewerTable().get(0);
            return table.getColumns().get(columnIndex).getDisplayName();
          }
        }), new MetadataTableList.ColumnInfo<>(messages.description(), 15, getDescriptionColumn()));
    }

  }

  @Override
  public Column<ViewerPrimaryKey, String> getDescriptionColumn() {
    Column<ViewerPrimaryKey, String> description = new Column<ViewerPrimaryKey, String>(new EditableCell()) {
      @Override
      public String getValue(ViewerPrimaryKey object) {
        return object.getDescription();
      }
    };

    description.setFieldUpdater((index, object, value) -> {
      object.setDescription(value);
      updateSIARDbundle(object.getName(), value);
    });

    return description;
  }

  @Override
  public void updateSIARDbundle(String name, String value) {
    SIARDbundle.setTableType(schema.getName(), table.getName(), type, name, value);
    controls.validate();
  }
}
