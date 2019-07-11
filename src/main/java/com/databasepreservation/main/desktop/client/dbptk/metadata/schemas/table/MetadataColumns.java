package com.databasepreservation.main.desktop.client.dbptk.metadata.schemas.table;

import java.util.List;
import java.util.Map;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerColumn;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSIARDBundle;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.main.desktop.client.common.lists.MetadataTableList;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MetadataColumns implements MetadataTabPanel {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private ViewerSIARDBundle SIARDbundle;

  MetadataColumns(ViewerSIARDBundle SIARDbundle) {
    this.SIARDbundle = SIARDbundle;
  }

  @Override
  public MetadataTableList createTable(ViewerTable table, ViewerSchema schema) {

    List<ViewerColumn> columns = table.getColumns();
    Label header = new Label(messages.columnName());
    HTMLPanel info = new HTMLPanel("");

    return new MetadataTableList<>(header, info, columns.iterator(),
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
      }), new MetadataTableList.ColumnInfo<>(messages.nullable(), 15, new TextColumn<ViewerColumn>() {
        @Override
        public String getValue(ViewerColumn object) {
          return object.getNillable() ? "YES" : "NO";
        }
      }), new MetadataTableList.ColumnInfo<>(messages.description(), 15, getDescriptionColumn(table, schema)));

  }

  @Override
  public Column<ViewerColumn, String> getDescriptionColumn(ViewerTable table, ViewerSchema schema) {
    Column<ViewerColumn, String> description = new Column<ViewerColumn, String>(new EditTextCell()) {
      @Override
      public String getValue(ViewerColumn object) {
        return object.getDescription();
      }
    };

    description.setFieldUpdater((index, object, value) -> {
      object.setDescription(value);
      updateSIARDbundle(schema.getName(), table.getName(), "column" , object.getDisplayName(), value);
    });
    return description;
  }

  @Override
  public void updateSIARDbundle(String schemaName, String tableName, String type, String displayName, String value) {
    SIARDbundle.setTableType(schemaName, tableName, type, displayName, value);
  }

}
