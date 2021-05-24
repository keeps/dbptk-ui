/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.visualization.browse.configuration.dataTransformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.common.lists.widgets.MultipleSelectionTablePanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.handler.DataTransformationUtils;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.RelatedColumnConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.RelatedTablesConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class TransformationChildTables {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private DenormalizeConfiguration denormalizeConfiguration;
  private TransformationTable rootTable;
  private ViewerTable childTable;
  private Map<Integer, Boolean> isLoading = new HashMap<>();
  private String uuid;
  List<Button> buttons;

  public static TransformationChildTables createInstance(TableNode childTable,
    DenormalizeConfiguration denormalizeConfiguration, TransformationTable rootTable, List<Button> buttons) {
    return new TransformationChildTables(childTable, denormalizeConfiguration, rootTable, buttons);
  }

  private TransformationChildTables(TableNode childTable, DenormalizeConfiguration denormalizeConfiguration,
    TransformationTable rootTable, List<Button> buttons) {
    this.denormalizeConfiguration = denormalizeConfiguration;
    this.rootTable = rootTable;
    this.childTable = childTable.getTable();
    this.uuid = childTable.getUuid();
    this.buttons = buttons;
    preset();
  }

  /**
   *
   */
  private void preset() {
    for (ViewerColumn columns : childTable.getColumns()) {
      isLoading.put(columns.getColumnIndexInEnclosingTable(), true);
    }
  }

  public MultipleSelectionTablePanel<ViewerColumn> createTable() {
    MultipleSelectionTablePanel<ViewerColumn> selectionTablePanel = new MultipleSelectionTablePanel<>();
    Label header = new Label("");
    selectionTablePanel.createTable(header, new ArrayList<>(), childTable.getColumns().iterator(),
      createCheckbox(selectionTablePanel),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.columnName(), 7, new TextColumn<ViewerColumn>() {
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

  private MultipleSelectionTablePanel.ColumnInfo<ViewerColumn> createCheckbox(
    MultipleSelectionTablePanel<ViewerColumn> selectionTablePanel) {
    return new MultipleSelectionTablePanel.ColumnInfo<>("", 4,
      new Column<ViewerColumn, Boolean>(new CheckboxCell(true, true)) {
        int selectionSize;

        @Override
        public Boolean getValue(ViewerColumn object) {
          int index = object.getColumnIndexInEnclosingTable();
          if (isLoading.get(index)) {
            isLoading.put(index, false);
            if (isSet(uuid, index)) {
              selectionTablePanel.getSelectionModel().setSelected(object, true);
            }
            // save preset size to check if this table has changes
            selectionSize = selectionTablePanel.getSelectionModel().getSelectedSet().size();
          } else {
            if (selectionTablePanel.getSelectionModel().isSelected(object)) {
              DataTransformationUtils.addColumnToInclude(uuid, object, denormalizeConfiguration);
              rootTable.redrawTable(denormalizeConfiguration);
            } else {
              DataTransformationUtils.removeColumnToInclude(uuid, object, denormalizeConfiguration);
              rootTable.redrawTable(denormalizeConfiguration);
            }
            int currentSize = selectionTablePanel.getSelectionModel().getSelectedSet().size();
            if (selectionSize != currentSize) {
              for (Button button : buttons) {
                button.setEnabled(true);
              }
            }
          }

          return selectionTablePanel.getSelectionModel().isSelected(object);
        }
      });
  }

  private boolean isSet(String uuid, int index) {
    if (denormalizeConfiguration != null) {
      RelatedTablesConfiguration relatedTable = denormalizeConfiguration.getRelatedTable(uuid);
      if (relatedTable != null) {
        for (RelatedColumnConfiguration column : relatedTable.getColumnsIncluded()) {
          if (column.getIndex() == index) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
