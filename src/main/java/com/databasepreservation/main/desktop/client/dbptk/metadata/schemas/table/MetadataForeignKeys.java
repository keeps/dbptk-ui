package com.databasepreservation.main.desktop.client.dbptk.metadata.schemas.table;

import com.databasepreservation.main.common.shared.ViewerStructure.*;
import com.databasepreservation.main.desktop.client.common.lists.MetadataTableList;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import config.i18n.client.ClientMessages;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MetadataForeignKeys implements MetadataTabPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private ViewerSIARDBundle SIARDbundle;
  private ViewerDatabase database;

  MetadataForeignKeys(ViewerSIARDBundle SIARDbundle, ViewerDatabase database){
        this.SIARDbundle = SIARDbundle;
        this.database = database;
    }

  @Override
  public MetadataTableList createTable(ViewerTable table, ViewerSchema schema) {

    List<ViewerForeignKey> columns = table.getForeignKeys();
    Label header = new Label(messages.foreignKeys());
    HTMLPanel info = new HTMLPanel(SafeHtmlUtils.EMPTY_SAFE_HTML);

    return new MetadataTableList<>(header, info, columns.iterator(),
      new MetadataTableList.ColumnInfo<>(messages.references_foreignKeyName(), 15, new TextColumn<ViewerForeignKey>() {
        @Override
        public String getValue(ViewerForeignKey object) {
          return object.getName();
        }
      }), new MetadataTableList.ColumnInfo<>(messages.foreignKeys_referencedTable(), 15,
        new TextColumn<ViewerForeignKey>() {
          @Override
          public String getValue(ViewerForeignKey object) {
            return database.getMetadata().getTable(object.getReferencedTableUUID()).getName();
          }
        }),
      new MetadataTableList.ColumnInfo<>(messages.columnName(), 15, new TextColumn<ViewerForeignKey>() {
        @Override
        public String getValue(ViewerForeignKey object) {
          return getReferenceList(object);
        }
      }),
      new MetadataTableList.ColumnInfo<>(messages.foreignKeys_deleteAction(), 15, new TextColumn<ViewerForeignKey>() {
        @Override
        public String getValue(ViewerForeignKey object) {
          return object.getDeleteAction();
        }
      }),
      new MetadataTableList.ColumnInfo<>(messages.foreignKeys_updateAction(), 15, new TextColumn<ViewerForeignKey>() {
        @Override
        public String getValue(ViewerForeignKey object) {
          return object.getUpdateAction();
        }
      }), new MetadataTableList.ColumnInfo<>(messages.description(), 15, getDescriptionColumn(table, schema)));
  }

  private String getReferenceList(ViewerForeignKey object) {
    List<ViewerColumn> tableColumns = database.getMetadata().getTable(object.getReferencedTableUUID())
      .getColumns();
    List<String> referanceColumns = new ArrayList<>();
    for (ViewerReference reference : object.getReferences()) {
      referanceColumns.add(tableColumns.get(reference.getReferencedColumnIndex()).getDisplayName());
    }
    return referanceColumns.toString();
  }

  @Override
  public Column<ViewerForeignKey, String> getDescriptionColumn(ViewerTable table, ViewerSchema schema) {
    Column<ViewerForeignKey, String> description = new Column<ViewerForeignKey, String>(new EditTextCell()) {
      @Override
      public String getValue(ViewerForeignKey object) {
        return object.getDescription();
      }
    };

    description.setFieldUpdater((index, object, value) -> {
      object.setDescription(value);
      updateSIARDbundle(schema.getName(), table.getName(), "foreignKey", object.getName(), value);
    });

    return description;
  }

  @Override
  public void updateSIARDbundle(String schemaName, String tableName, String type, String displayName, String value) {
    SIARDbundle.setTableType(schemaName, tableName, type, displayName, value);
  }
}
