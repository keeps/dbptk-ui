package com.databasepreservation.main.desktop.client.dbptk.metadata.schemas.table;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerPrimaryKey;
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

import java.util.Arrays;
import java.util.Map;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MetadataPrimaryKey implements MetadataTabPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private Map<String, String> SIARDbundle;
  private ViewerDatabase database;

  MetadataPrimaryKey(Map<String, String> SIARDbundle, ViewerDatabase database) {
    this.SIARDbundle = SIARDbundle;
    this.database = database;
  }

  @Override
  public MetadataTableList createTable(ViewerTable table, ViewerSchema schema) {

    ViewerPrimaryKey columns = table.getPrimaryKey();
    Label header = new Label(messages.primaryKey());
    HTMLPanel info = new HTMLPanel("");

    return new MetadataTableList<>(header, info, Arrays.asList(columns).iterator(),
      new MetadataTableList.ColumnInfo<>(messages.primaryKey(), 15, new TextColumn<ViewerPrimaryKey>() {
        @Override
        public String getValue(ViewerPrimaryKey object) {
          Integer columnIndex = object.getColumnIndexesInViewerTable().get(0);
          return table.getColumns().get(columnIndex).getDisplayName();
        }
      }), new MetadataTableList.ColumnInfo<>(messages.description(), 15, getDescriptionColumn(table, schema)));

  }

  @Override
  public Column<ViewerPrimaryKey, String> getDescriptionColumn(ViewerTable table, ViewerSchema schema) {
    Column<ViewerPrimaryKey, String> description = new Column<ViewerPrimaryKey, String>(new EditTextCell()) {
      @Override
      public String getValue(ViewerPrimaryKey object) {
        return object.getDescription();
      }
    };

    description.setFieldUpdater((index, object, value) -> {
      object.setDescription(value);
      updateSIARDbundle(schema.getName(), table.getName(), object.getName(), "primaryKey", value);
    });

    return description;
  }

  @Override
  public void updateSIARDbundle(String schemaName, String tableName, String displayName, String type, String value) {
    SIARDbundle.put("schema:" + schemaName + "---" + "table:" + tableName + "---" + type + ":" + displayName,
      "description---" + value);
  }
}
