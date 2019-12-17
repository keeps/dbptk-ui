package com.databasepreservation.common.client.common.visualization.metadata.schemas.tables;

import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.common.client.common.lists.cells.EditableCell;
import com.databasepreservation.common.client.common.lists.widgets.MetadataTableList;
import com.databasepreservation.common.client.common.visualization.metadata.MetadataControlPanel;
import com.databasepreservation.common.client.common.visualization.metadata.MetadataEditPanel;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerForeignKey;
import com.databasepreservation.common.client.models.structure.ViewerReference;
import com.databasepreservation.common.client.models.structure.ViewerSIARDBundle;
import com.databasepreservation.common.client.models.structure.ViewerSchema;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.google.gwt.cell.client.FieldUpdater;
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
public class MetadataForeignKeys implements MetadataEditPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private final MetadataControlPanel controls;
  private ViewerSIARDBundle SIARDbundle;
  private ViewerDatabase database;
  private ViewerTable table;
  private ViewerSchema schema;
  private String type = "foreignKey";

  MetadataForeignKeys(ViewerSIARDBundle SIARDbundle, ViewerDatabase database, ViewerSchema schema, ViewerTable table,
    MetadataControlPanel controls) {
    this.SIARDbundle = SIARDbundle;
    this.database = database;
    this.table = table;
    this.schema = schema;
    this.controls = controls;
  }

  @Override
  public MetadataTableList createTable() {

    List<ViewerForeignKey> columns = table.getForeignKeys();

    if (columns.isEmpty()) {
      return new MetadataTableList<>(messages.tableDoesNotContainForeignKeys());
    } else {

      return new MetadataTableList<>(columns.iterator(), new MetadataTableList.ColumnInfo<>(
        messages.references_foreignKeyName(), 7, new TextColumn<ViewerForeignKey>() {
          @Override
          public String getValue(ViewerForeignKey object) {
            return object.getName();
          }
        }), new MetadataTableList.ColumnInfo<>(messages.foreignKeys_referencedTable(), 7,
          new TextColumn<ViewerForeignKey>() {
            @Override
            public String getValue(ViewerForeignKey object) {
              if (database.getMetadata().getTable(object.getReferencedTableUUID()) == null) {
                return messages.SIARDError();
              }
              return database.getMetadata().getTable(object.getReferencedTableUUID()).getName();
            }
          }),
        new MetadataTableList.ColumnInfo<>(messages.columnName(), 7, new TextColumn<ViewerForeignKey>() {
          @Override
          public String getValue(ViewerForeignKey object) {
            return getReferenceList(object);
          }
        }),
        new MetadataTableList.ColumnInfo<>(messages.foreignKeys_deleteAction(), 7, new TextColumn<ViewerForeignKey>() {
          @Override
          public String getValue(ViewerForeignKey object) {
            return object.getDeleteAction();
          }
        }),
        new MetadataTableList.ColumnInfo<>(messages.foreignKeys_updateAction(), 7, new TextColumn<ViewerForeignKey>() {
          @Override
          public String getValue(ViewerForeignKey object) {
            return object.getUpdateAction();
          }
        }), new MetadataTableList.ColumnInfo<>(messages.description(), 25, getDescriptionColumn()));
    }
  }

  private String getReferenceList(ViewerForeignKey object) {
    if (database.getMetadata().getTable(object.getReferencedTableUUID()) == null) {
      return messages.SIARDError();
    }
    List<ViewerColumn> tableColumns = database.getMetadata().getTable(object.getReferencedTableUUID()).getColumns();
    List<String> referanceColumns = new ArrayList<>();
    for (ViewerReference reference : object.getReferences()) {
      referanceColumns.add(tableColumns.get(reference.getReferencedColumnIndex()).getDisplayName());
    }
    return referanceColumns.toString();
  }

  @Override
  public Column<ViewerForeignKey, String> getDescriptionColumn() {
    Column<ViewerForeignKey, String> description = new Column<ViewerForeignKey, String>(new EditableCell() {
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
      public String getValue(ViewerForeignKey object) {
        return object.getDescription();
      }
    };

    description.setFieldUpdater(new FieldUpdater<ViewerForeignKey, String>() {
      @Override
      public void update(int index, ViewerForeignKey object, String value) {
        object.setDescription(value);
        MetadataForeignKeys.this.updateSIARDbundle(object.getName(), value);
      }
    });

    return description;
  }

  @Override
  public void updateSIARDbundle(String name, String value) {
    SIARDbundle.setTableType(schema.getName(), table.getName(), type, name, value);
    controls.validate();
  }
}
