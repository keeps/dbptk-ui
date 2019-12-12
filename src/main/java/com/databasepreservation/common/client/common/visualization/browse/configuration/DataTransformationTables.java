package com.databasepreservation.common.client.common.visualization.browse.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.common.EditableCell;
import com.databasepreservation.common.client.common.lists.MetadataTableList;
import com.databasepreservation.common.client.common.lists.MultipleSelectionTablePanel;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DataTransformationTables {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private ViewerTable table;

  DataTransformationTables(ViewerTable table) {
    this.table = table;
  }

  public MetadataTableList<ViewerColumn> createTargetTable() {
    List<ViewerColumn> columns = table.getColumns();

    if (columns.isEmpty()) {
      return new MetadataTableList<>(messages.tableDoesNotContainColumns());
    } else {
      return new MetadataTableList<ViewerColumn>(columns.iterator(),
        new MetadataTableList.ColumnInfo<>(messages.columnName(), 7, new TextColumn<ViewerColumn>() {
          @Override
          public String getValue(ViewerColumn object) {
            return object.getDisplayName();
          }
        }), new MetadataTableList.ColumnInfo<>("Custom", 7, getCustomLabelColumn()),
        new MetadataTableList.ColumnInfo<>(messages.description(), 25, new TextColumn<ViewerColumn>() {
          @Override
          public String getValue(ViewerColumn object) {
            return object.getDescription();
          }
        }));
    }
  }

  private void redrawTargetTable(ViewerTable targetTable, FlowPanel targetTablePanel,
    Map<String, ViewerColumn> columnsToIncludeMap) {
    MetadataTableList<ViewerColumn> targetTableUpdated;
    targetTablePanel.clear();

    List<ViewerColumn> columns = new ArrayList<>(targetTable.getColumns());

    for (Map.Entry<String, ViewerColumn> entry : columnsToIncludeMap.entrySet()) {
      columns.add(entry.getValue());
    }

    if (columns.isEmpty()) {
      targetTableUpdated = new MetadataTableList<>(messages.tableDoesNotContainColumns());
    } else {
      targetTableUpdated = new MetadataTableList<ViewerColumn>(columns.iterator(),
        new MetadataTableList.ColumnInfo<>(messages.columnName(), 7, new TextColumn<ViewerColumn>() {
          @Override
          public String getValue(ViewerColumn object) {
            return object.getDisplayName();
          }
        }), new MetadataTableList.ColumnInfo<>("Custom", 7, getCustomLabelColumn()),
        new MetadataTableList.ColumnInfo<>(messages.description(), 25, new TextColumn<ViewerColumn>() {
          @Override
          public String getValue(ViewerColumn object) {
            return object.getDescription();
          }
        }));
    }

    targetTablePanel.add(targetTableUpdated);
  }

  public Column<ViewerColumn, String> getCustomLabelColumn() {
    Column<ViewerColumn, String> customLabel = new Column<ViewerColumn, String>(new EditableCell()) {
      @Override
      public String getValue(ViewerColumn object) {
        return object.getDisplayName();
      }
    };
    customLabel.setFieldUpdater((index, object, value) -> {
      object.setDisplayName(value);
    });

    return customLabel;
  }

  public MultipleSelectionTablePanel createRelatedTable(ViewerTable targetTable, FlowPanel targetTablePanel,
    Map<String, ViewerColumn> columnsToIncludeMap) {
    List<ViewerColumn> columns = table.getColumns();
    Label header = new Label("");

    MultipleSelectionTablePanel<ViewerColumn> selectionTablePanel = new MultipleSelectionTablePanel<>();
    selectionTablePanel.createTable(header, columns.iterator(), new MultipleSelectionTablePanel.ColumnInfo<>("", 4,
      new Column<ViewerColumn, Boolean>(new CheckboxCell(true, true)) {
        Boolean hasUpdate = false;

        @Override
        public Boolean getValue(ViewerColumn object) {
          if (selectionTablePanel.getSelectionModel().isSelected(object)) {
            columnsToIncludeMap.put(table.getId() + "." + object.getDisplayName(), object);
            redrawTargetTable(targetTable, targetTablePanel, columnsToIncludeMap);
            hasUpdate = true;
          } else if (hasUpdate) {
            columnsToIncludeMap.remove(table.getId() + "." + object.getDisplayName());
            redrawTargetTable(targetTable, targetTablePanel, columnsToIncludeMap);
          }

          return selectionTablePanel.getSelectionModel().isSelected(object);
        }
      }), new MultipleSelectionTablePanel.ColumnInfo<>(messages.columnName(), 7, new TextColumn<ViewerColumn>() {
        @Override
        public String getValue(ViewerColumn object) {
          return object.getDisplayName();
        }
      }), new MultipleSelectionTablePanel.ColumnInfo<>(messages.description(), 25, new TextColumn<ViewerColumn>() {
        @Override
        public String getValue(ViewerColumn object) {
          return object.getDescription();
        }
      }));

    return selectionTablePanel;
  }
}
