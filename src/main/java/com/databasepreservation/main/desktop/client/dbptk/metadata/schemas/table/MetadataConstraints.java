package com.databasepreservation.main.desktop.client.dbptk.metadata.schemas.table;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerCheckConstraint;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
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

import java.util.List;
import java.util.Map;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MetadataConstraints implements MetadataTabPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private final Map<String, String> SIARDbundle;
  private final ViewerDatabase database;

  MetadataConstraints(Map<String, String> SIARDbundle, ViewerDatabase database) {
    this.SIARDbundle = SIARDbundle;
    this.database = database;
  }

  @Override
  public MetadataTableList createTable(ViewerTable table, ViewerSchema schema) {

    List<ViewerCheckConstraint> columns = table.getCheckConstraints();
    Label header = new Label(messages.primaryKey());
    HTMLPanel info = new HTMLPanel("");

    return new MetadataTableList<>(header, info, columns.iterator(),
      new MetadataTableList.ColumnInfo<>(messages.name(), 15, new TextColumn<ViewerCheckConstraint>() {
        @Override
        public String getValue(ViewerCheckConstraint object) {
          return object.getName();
        }
      }),
      new MetadataTableList.ColumnInfo<>(messages.constraints_condition(), 15, new TextColumn<ViewerCheckConstraint>() {
        @Override
        public String getValue(ViewerCheckConstraint object) {
          return database.getMetadata().getTable(object.getCondition()).getName();
        }
      }), new MetadataTableList.ColumnInfo<>(messages.description(), 15, getDescriptionColumn(table, schema)));
  }

  @Override
  public Column<ViewerCheckConstraint, String> getDescriptionColumn(ViewerTable table, ViewerSchema schema) {
    Column<ViewerCheckConstraint, String> description = new Column<ViewerCheckConstraint, String>(new EditTextCell()) {
      @Override
      public String getValue(ViewerCheckConstraint object) {
        return object.getDescription();
      }
    };

    description.setFieldUpdater((index, object, value) -> {
      object.setDescription(value);
      updateSIARDbundle(schema.getName(), table.getName(), object.getDescription(), "constraint", value);
    });

    return description;
  }

  @Override
  public void updateSIARDbundle(String schemaName, String tableName, String displayName, String type, String value) {
    SIARDbundle.put("schema:" + schemaName + "---" + "table:" + tableName + "---" + type + ":" + displayName,
      "description---" + value);
  }
}
