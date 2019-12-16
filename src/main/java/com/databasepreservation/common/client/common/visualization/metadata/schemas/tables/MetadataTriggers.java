package com.databasepreservation.common.client.common.visualization.metadata.schemas.tables;

import java.util.List;

import com.databasepreservation.common.client.common.EditableCell;
import com.databasepreservation.common.client.common.lists.widgets.MetadataTableList;
import com.databasepreservation.common.client.common.visualization.metadata.MetadataControlPanel;
import com.databasepreservation.common.client.common.visualization.metadata.MetadataEditPanel;
import com.databasepreservation.common.client.models.structure.ViewerSIARDBundle;
import com.databasepreservation.common.client.models.structure.ViewerSchema;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.models.structure.ViewerTrigger;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MetadataTriggers implements MetadataEditPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private final ViewerSIARDBundle SIARDbundle;
  private final MetadataControlPanel controls;
  private ViewerTable table;
  private ViewerSchema schema;
  private String type = "trigger";

  public MetadataTriggers(ViewerSIARDBundle SIARDbundle, ViewerSchema schema, ViewerTable table,
    MetadataControlPanel controls) {
    this.SIARDbundle = SIARDbundle;
    this.table = table;
    this.schema = schema;
    this.controls = controls;
  }

  @Override
  public MetadataTableList createTable() {
    List<ViewerTrigger> columns = table.getTriggers();

    if (columns.isEmpty()) {
      return new MetadataTableList<>(messages.tableDoesNotContainTriggers());
    } else {

      return new MetadataTableList<>(columns.iterator(),
        new MetadataTableList.ColumnInfo<>(messages.name(), 10, new TextColumn<ViewerTrigger>() {
          @Override
          public String getValue(ViewerTrigger object) {
            return object.getName();
          }
        }), new MetadataTableList.ColumnInfo<>(messages.triggeredAction(), 10, new TextColumn<ViewerTrigger>() {
          @Override
          public String getValue(ViewerTrigger object) {
            return object.getTriggeredAction();
          }
        }), new MetadataTableList.ColumnInfo<>(messages.triggerEvent(), 7, new TextColumn<ViewerTrigger>() {
          @Override
          public String getValue(ViewerTrigger object) {
            return object.getTriggerEvent();
          }
        }), new MetadataTableList.ColumnInfo<>(messages.aliasList(), 7, new TextColumn<ViewerTrigger>() {
          @Override
          public String getValue(ViewerTrigger object) {
            return object.getAliasList();
          }
        }), new MetadataTableList.ColumnInfo<>(messages.actionTime(), 7, new TextColumn<ViewerTrigger>() {
          @Override
          public String getValue(ViewerTrigger object) {
            return object.getActionTime();
          }
        }), new MetadataTableList.ColumnInfo<>(messages.description(), 15, getDescriptionColumn()));
    }
  }

  @Override
  public Column<ViewerTrigger, String> getDescriptionColumn() {
    Column<ViewerTrigger, String> description = new Column<ViewerTrigger, String>(new EditableCell() {
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
    controls.validate();
  }
}
