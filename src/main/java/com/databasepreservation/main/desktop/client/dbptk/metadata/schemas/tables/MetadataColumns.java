package com.databasepreservation.main.desktop.client.dbptk.metadata.schemas.tables;

import java.util.List;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerColumn;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerPrimaryKey;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSIARDBundle;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.main.common.shared.client.tools.FontAwesomeIconManager;
import com.databasepreservation.main.desktop.client.common.EditableCell;
import com.databasepreservation.main.desktop.client.common.lists.MetadataTableList;
import com.databasepreservation.main.desktop.client.dbptk.metadata.MetadataEditPanel;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MetadataColumns implements MetadataEditPanel {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private ViewerSIARDBundle SIARDbundle;
  private ViewerTable table;
  private ViewerSchema schema;
  private String type = "column";

  MetadataColumns(ViewerSIARDBundle SIARDbundle, ViewerSchema schema, ViewerTable table) {
    this.SIARDbundle = SIARDbundle;
    this.table = table;
    this.schema = schema;
  }

  @Override
  public MetadataTableList createTable() {

    List<ViewerColumn> columns = table.getColumns();

    Label header = new Label("");
    HTMLPanel info = new HTMLPanel(SafeHtmlUtils.EMPTY_SAFE_HTML);

    if (columns.isEmpty()) {
      return new MetadataTableList<>(header, messages.tableDoesNotContainColumns());
    } else {
      return new MetadataTableList<>(header, info, columns.iterator(),
        new MetadataTableList.ColumnInfo<>("", 2, getPrimaryKeyColumn(table)),
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
        }), new MetadataTableList.ColumnInfo<>(messages.description(), 15, getDescriptionColumn()));
    }
  }

  private Column<ViewerColumn, SafeHtml> getPrimaryKeyColumn(ViewerTable table) {
    return new Column<ViewerColumn, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(ViewerColumn object) {
        ViewerPrimaryKey pk = table.getPrimaryKey();
        if (pk != null) {
          Integer pkIndex = pk.getColumnIndexesInViewerTable().get(0);
          String pkName = table.getColumns().get(pkIndex).getDisplayName();
          if (pkName.equals(object.getDisplayName())) {
            return SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.KEY));
          }
        }
        return SafeHtmlUtils.EMPTY_SAFE_HTML;
      }
    };
  }

  @Override
  public Column<ViewerColumn, String> getDescriptionColumn() {
    Column<ViewerColumn, String> description = new Column<ViewerColumn, String>(new EditableCell()) {
      @Override
      public String getValue(ViewerColumn object) {
        return object.getDescription();
      }
    };

    description.setFieldUpdater((index, object, value) -> {
      object.setDescription(value);
      updateSIARDbundle(object.getDisplayName(), value);
    });
    return description;
  }

  @Override
  public void updateSIARDbundle(String name, String value) {
    SIARDbundle.setTableType(schema.getName(), table.getName(), type, name, value);
  }

}
