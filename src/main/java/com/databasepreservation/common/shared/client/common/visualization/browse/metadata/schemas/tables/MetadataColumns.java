package com.databasepreservation.common.shared.client.common.visualization.browse.metadata.schemas.tables;

import java.util.List;

import com.databasepreservation.common.shared.ViewerStructure.ViewerColumn;
import com.databasepreservation.common.shared.ViewerStructure.ViewerPrimaryKey;
import com.databasepreservation.common.shared.ViewerStructure.ViewerSIARDBundle;
import com.databasepreservation.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.common.shared.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.shared.client.common.EditableCell;
import com.databasepreservation.common.shared.client.common.lists.MetadataTableList;
import com.databasepreservation.common.shared.client.common.visualization.browse.metadata.MetadataControlPanel;
import com.databasepreservation.common.shared.client.common.visualization.browse.metadata.MetadataEditPanel;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MetadataColumns implements MetadataEditPanel {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private final MetadataControlPanel controls;
  private ViewerSIARDBundle SIARDbundle;
  private ViewerTable table;
  private ViewerSchema schema;
  private String type = "column";

  MetadataColumns(ViewerSIARDBundle SIARDbundle, ViewerSchema schema, ViewerTable table,
    MetadataControlPanel controls) {
    this.SIARDbundle = SIARDbundle;
    this.table = table;
    this.schema = schema;
    this.controls = controls;
  }

  @Override
  public MetadataTableList createTable() {

    List<ViewerColumn> columns = table.getColumns();

    if (columns.isEmpty()) {
      return new MetadataTableList<>(messages.tableDoesNotContainColumns());
    } else {
      return new MetadataTableList<>(columns.iterator(),
        new MetadataTableList.ColumnInfo<>("", 1.2, getPrimaryKeyColumn(table)),
        new MetadataTableList.ColumnInfo<>(messages.columnName(), 7, new TextColumn<ViewerColumn>() {
          @Override
          public String getValue(ViewerColumn object) {
            return object.getDisplayName();
          }
        }), new MetadataTableList.ColumnInfo<>(messages.typeName(), 7, new TextColumn<ViewerColumn>() {
          @Override
          public String getValue(ViewerColumn object) {
            return object.getType().getTypeName();
          }
        }), new MetadataTableList.ColumnInfo<>(messages.originalTypeName(), 7, new TextColumn<ViewerColumn>() {
          @Override
          public String getValue(ViewerColumn object) {
            return object.getType().getOriginalTypeName();
          }
        }), new MetadataTableList.ColumnInfo<>(messages.nullable(), 4, new TextColumn<ViewerColumn>() {
          @Override
          public String getValue(ViewerColumn object) {
            return object.getNillable() ? "YES" : "NO";
          }
        }), new MetadataTableList.ColumnInfo<>(messages.description(), 25, getDescriptionColumn()));
    }
  }

  private Column<ViewerColumn, SafeHtml> getPrimaryKeyColumn(ViewerTable table) {
    return new Column<ViewerColumn, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(ViewerColumn object) {
        ViewerPrimaryKey pk = table.getPrimaryKey();
        if (pk != null) {
          Integer pkIndex = pk.getColumnIndexesInViewerTable().get(0);
          if (pkIndex != null) {
            String pkName = table.getColumns().get(pkIndex).getDisplayName();
            if (pkName.equals(object.getDisplayName())) {
              return SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.KEY));
            }
          }
        }
        return SafeHtmlUtils.EMPTY_SAFE_HTML;
      }
    };
  }

  @Override
  public Column<ViewerColumn, String> getDescriptionColumn() {
    Column<ViewerColumn, String> description = new Column<ViewerColumn, String>(new EditableCell() {
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
    controls.validate();
  }

}
