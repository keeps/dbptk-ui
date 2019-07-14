package com.databasepreservation.main.desktop.client.dbptk.metadata.schemas.tables;

import com.databasepreservation.main.common.shared.ViewerStructure.*;
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

import java.util.Arrays;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MetadataPrimaryKey implements MetadataEditPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private ViewerSIARDBundle SIARDbundle;
  private ViewerTable table;
  private ViewerSchema schema;
  private String type = "primaryKey";

  MetadataPrimaryKey(ViewerSIARDBundle SIARDbundle, ViewerSchema schema, ViewerTable table) {
    this.SIARDbundle = SIARDbundle;
    this.table = table;
    this.schema = schema;
  }

  @Override
  public MetadataTableList createTable() {

    ViewerPrimaryKey columns = table.getPrimaryKey();
    Label header = new Label("");
    HTMLPanel info = new HTMLPanel(SafeHtmlUtils.EMPTY_SAFE_HTML);

    if (columns == null) {
      return new MetadataTableList<>(header, messages.tableDoesNotContainPrimaryKey());
    } else {

      return new MetadataTableList<>(header, info, Arrays.asList(columns).iterator(),
        new MetadataTableList.ColumnInfo<>(messages.primaryKey(), 15, new TextColumn<ViewerPrimaryKey>() {
          @Override
          public String getValue(ViewerPrimaryKey object) {
            Integer columnIndex = object.getColumnIndexesInViewerTable().get(0);
            return table.getColumns().get(columnIndex).getDisplayName();
          }
        }), new MetadataTableList.ColumnInfo<>(messages.description(), 15, getDescriptionColumn()));
    }

  }

  @Override
  public Column<ViewerPrimaryKey, String> getDescriptionColumn() {
    Column<ViewerPrimaryKey, String> description = new Column<ViewerPrimaryKey, String>(new EditableCell()) {
      @Override
      public String getValue(ViewerPrimaryKey object) {
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
  }
}
