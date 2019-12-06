package com.databasepreservation.common.client.common.visualization.metadata.schemas.tables;

import java.util.List;

import com.databasepreservation.common.client.common.EditableCell;
import com.databasepreservation.common.client.common.lists.MetadataTableList;
import com.databasepreservation.common.client.common.visualization.metadata.MetadataControlPanel;
import com.databasepreservation.common.client.common.visualization.metadata.MetadataEditPanel;
import com.databasepreservation.common.client.models.structure.ViewerCheckConstraint;
import com.databasepreservation.common.client.models.structure.ViewerSIARDBundle;
import com.databasepreservation.common.client.models.structure.ViewerSchema;
import com.databasepreservation.common.client.models.structure.ViewerTable;
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
public class MetadataConstraints implements MetadataEditPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private final ViewerSIARDBundle SIARDbundle;
  private final MetadataControlPanel controls;
  private ViewerTable table;
  private ViewerSchema schema;
  private String type = "constraint";

  MetadataConstraints(ViewerSIARDBundle SIARDbundle, ViewerSchema schema, ViewerTable table,
    MetadataControlPanel controls) {
    this.SIARDbundle = SIARDbundle;
    this.table = table;
    this.schema = schema;
    this.controls = controls;
  }

  @Override
  public MetadataTableList createTable() {

    List<ViewerCheckConstraint> columns = table.getCheckConstraints();

    if (columns.isEmpty()) {
      return new MetadataTableList<>(messages.tableDoesNotContainConstraints());
    } else {

      return new MetadataTableList<ViewerCheckConstraint>(columns.iterator(),
        new MetadataTableList.ColumnInfo<>(messages.name(), 7, new TextColumn<ViewerCheckConstraint>() {
          @Override
          public String getValue(ViewerCheckConstraint object) {
            return object.getName();
          }
        }), new MetadataTableList.ColumnInfo<>(messages.constraints_condition(), 7,
          new TextColumn<ViewerCheckConstraint>() {
            @Override
            public String getValue(ViewerCheckConstraint object) {
              return object.getCondition();
            }
          }),
        new MetadataTableList.ColumnInfo<>(messages.description(), 25, getDescriptionColumn()));
    }
  }

  @Override
  public Column<ViewerCheckConstraint, String> getDescriptionColumn() {
    Column<ViewerCheckConstraint, String> description = new Column<ViewerCheckConstraint, String>(new EditableCell() {
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
      public String getValue(ViewerCheckConstraint object) {
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
