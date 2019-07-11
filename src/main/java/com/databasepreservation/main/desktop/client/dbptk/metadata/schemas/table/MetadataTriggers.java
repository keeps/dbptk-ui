package com.databasepreservation.main.desktop.client.dbptk.metadata.schemas.table;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerTrigger;
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
public class MetadataTriggers implements MetadataTabPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private final Map<String, String> SIARDbundle;
  private final ViewerDatabase database;

  public MetadataTriggers(Map<String, String> SIARDbundle, ViewerDatabase database) {
    this.SIARDbundle = SIARDbundle;
    this.database = database;
  }

  @Override
  public MetadataTableList createTable(ViewerTable table, ViewerSchema schema) {
    List<ViewerTrigger> columns = table.getTriggers();
    Label header = new Label(messages.menusidebar_triggers());
    HTMLPanel info = new HTMLPanel("");

    return new MetadataTableList<>(header, info, columns.iterator(),
      new MetadataTableList.ColumnInfo<>(messages.name(), 15, new TextColumn<ViewerTrigger>() {
        @Override
        public String getValue(ViewerTrigger object) {
          return object.getName();
        }
      }), new MetadataTableList.ColumnInfo<>(messages.triggeredAction(), 15, new TextColumn<ViewerTrigger>() {
        @Override
        public String getValue(ViewerTrigger object) {
          return object.getTriggeredAction();
        }
      }), new MetadataTableList.ColumnInfo<>(messages.triggerEvent(), 15, new TextColumn<ViewerTrigger>() {
        @Override
        public String getValue(ViewerTrigger object) {
          return object.getTriggerEvent();
        }
      }), new MetadataTableList.ColumnInfo<>(messages.aliasList(), 15, new TextColumn<ViewerTrigger>() {
        @Override
        public String getValue(ViewerTrigger object) {
          return object.getAliasList();
        }
      }), new MetadataTableList.ColumnInfo<>(messages.actionTime(), 15, new TextColumn<ViewerTrigger>() {
        @Override
        public String getValue(ViewerTrigger object) {
          return object.getActionTime();
        }
      }),new MetadataTableList.ColumnInfo<>(messages.description(), 15, getDescriptionColumn(table, schema)));
  }

  @Override
  public Column<ViewerTrigger, String> getDescriptionColumn(ViewerTable table, ViewerSchema schema) {
    Column<ViewerTrigger, String> description = new Column<ViewerTrigger, String>(new EditTextCell()) {
      @Override
      public String getValue(ViewerTrigger object) {
        return object.getDescription();
      }
    };

    description.setFieldUpdater((index, object, value) -> {
      object.setDescription(value);
      updateSIARDbundle(schema.getName(), table.getName(), object.getDescription(), "trigger", value);
    });

    return description;
  }

  @Override
  public void updateSIARDbundle(String schemaName, String tableName, String displayName, String type, String value) {
    SIARDbundle.put("schema:" + schemaName + "---" + "table:" + tableName + "---" + type + ":" + displayName,
      "description---" + value);
  }
}
