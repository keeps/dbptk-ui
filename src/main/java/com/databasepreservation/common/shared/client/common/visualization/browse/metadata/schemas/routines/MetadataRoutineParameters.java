package com.databasepreservation.common.shared.client.common.visualization.browse.metadata.schemas.routines;

import java.util.List;

import com.databasepreservation.common.shared.ViewerStructure.ViewerRoutineParameter;
import com.databasepreservation.common.shared.ViewerStructure.ViewerSIARDBundle;
import com.databasepreservation.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.common.shared.client.common.EditableCell;
import com.databasepreservation.common.shared.client.common.lists.MetadataTableList;
import com.databasepreservation.common.shared.client.common.visualization.browse.metadata.MetadataControlPanel;
import com.databasepreservation.common.shared.client.common.visualization.browse.metadata.MetadataEditPanel;
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
public class MetadataRoutineParameters implements MetadataEditPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private final List<ViewerRoutineParameter> parameters;
  private final MetadataControlPanel controls;
  private ViewerSIARDBundle SIARDbundle;
  private ViewerSchema schema;

  public MetadataRoutineParameters(ViewerSIARDBundle SIARDbundle, ViewerSchema schema,
    List<ViewerRoutineParameter> parameters, MetadataControlPanel controls) {
    this.SIARDbundle = SIARDbundle;
    this.schema = schema;
    this.parameters = parameters;
    this.controls = controls;
  }

  @Override
  public MetadataTableList createTable() {
    if (parameters.isEmpty()) {
      return new MetadataTableList<>(messages.tableDoesNotContainColumns());
    } else {
      return new MetadataTableList<>(parameters.iterator(),
        new MetadataTableList.ColumnInfo<>(messages.name(), 15, new TextColumn<ViewerRoutineParameter>() {
          @Override
          public String getValue(ViewerRoutineParameter object) {
            return object.getName();
          }
        }), new MetadataTableList.ColumnInfo<>(messages.routineParameter_mode(), 15,
          new TextColumn<ViewerRoutineParameter>() {
            @Override
            public String getValue(ViewerRoutineParameter object) {
              return object.getMode();
            }
          }),
        new MetadataTableList.ColumnInfo<>(messages.titleType(), 15, new TextColumn<ViewerRoutineParameter>() {
          @Override
          public String getValue(ViewerRoutineParameter object) {
            return object.getType().getTypeName();
          }
        }), new MetadataTableList.ColumnInfo<>(messages.titleType(), 15, getDescriptionColumn()));
    }

  }

  @Override
  public Column<ViewerRoutineParameter, String> getDescriptionColumn() {
    Column<ViewerRoutineParameter, String> description = new Column<ViewerRoutineParameter, String>(new EditableCell() {
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
      public String getValue(ViewerRoutineParameter object) {
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
    // TODO: update routines parameters
  }
}
