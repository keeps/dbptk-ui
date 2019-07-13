package com.databasepreservation.main.desktop.client.dbptk.metadata.schemas.views;

import com.databasepreservation.main.common.shared.ViewerStructure.*;
import com.databasepreservation.main.desktop.client.common.lists.MetadataTableList;
import com.databasepreservation.main.desktop.client.dbptk.metadata.schemas.MetadataTabPanel;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import config.i18n.client.ClientMessages;

import java.util.List;

public class MetadataViewColumns {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private ViewerSIARDBundle SIARDbundle;

  public MetadataViewColumns(ViewerSIARDBundle SIARDbundle) {
    this.SIARDbundle = SIARDbundle;
  }

  public MetadataTableList createTable(ViewerView view, ViewerSchema schema) {
    List<ViewerColumn> columns = view.getColumns();

    Label header = new Label("");
    HTMLPanel info = new HTMLPanel(SafeHtmlUtils.EMPTY_SAFE_HTML);

    if (columns.isEmpty()) {
      return new MetadataTableList<>(header, messages.tableDoesNotContainColumns());
    } else {
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
        }), new MetadataTableList.ColumnInfo<>(messages.nullable(), 7, new TextColumn<ViewerColumn>() {
          @Override
          public String getValue(ViewerColumn object) {
            return object.getNillable() ? "YES" : "NO";
          }
        }), new MetadataTableList.ColumnInfo<>(messages.description(), 15, getDescriptionColumn(view, schema)));
    }
  }

  public Column<ViewerColumn, String> getDescriptionColumn(ViewerView view, ViewerSchema schema) {
      Column<ViewerColumn, String> description = new Column<ViewerColumn, String>(new EditTextCell()) {
          @Override
          public String getValue(ViewerColumn object) {
              return object.getDescription();
          }
      };

      description.setFieldUpdater((index, object, value) -> {
          object.setDescription(value);
          updateSIARDbundle(schema.getName(), view.getName(), object.getDisplayName(), value);
      });
      return description;
  }

  public void updateSIARDbundle(String schemaName, String tableName, String displayName, String value) {
      SIARDbundle.setViewColumn(schemaName, tableName, displayName, value);
  }
}
