package com.databasepreservation.main.desktop.client.dbptk.metadata.schemas.tables;

import java.util.List;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSIARDBundle;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerTrigger;
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

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MetadataTriggers implements MetadataEditPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private final ViewerSIARDBundle SIARDbundle;
  private ViewerTable table;
  private ViewerSchema schema;
  private String type = "trigger";

  public MetadataTriggers(ViewerSIARDBundle SIARDbundle, ViewerSchema schema, ViewerTable table) {
    this.SIARDbundle = SIARDbundle;
    this.table = table;
    this.schema = schema;
  }

  @Override
  public MetadataTableList createTable() {
    List<ViewerTrigger> columns = table.getTriggers();

    if (columns.isEmpty()) {
      return new MetadataTableList<>(messages.tableDoesNotContainTriggers());
    } else {

      return new MetadataTableList<>(columns.iterator(),
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
        }), new MetadataTableList.ColumnInfo<>(messages.description(), 15, getDescriptionColumn()));
    }
  }

  @Override
  public Column<ViewerTrigger, String> getDescriptionColumn() {
    Column<ViewerTrigger, String> description = new Column<ViewerTrigger, String>(new EditableCell()) {
      @Override
      public String getValue(ViewerTrigger object) {
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
    JavascriptUtils.alertUpdatedMetadata();
  }
}
