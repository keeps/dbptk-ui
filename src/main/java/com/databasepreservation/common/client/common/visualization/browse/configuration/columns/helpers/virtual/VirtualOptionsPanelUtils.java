package com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.virtual;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.structure.ViewerSourceType;
import com.databasepreservation.common.client.models.structure.ViewerType;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class VirtualOptionsPanelUtils {

  public static void renderColumnTemplateButtons(List<ColumnStatus> columns, FlowPanel hintPanel, TextBox targetInput,
    List<String> columnIdsContainer, ClientMessages messages, boolean allowVirtualColumns) {

    hintPanel.clear();
    hintPanel.add(new Label(messages.columnManagementTextForPossibleFields()));

    final Map<String, Button> buttonMap = new HashMap<>();
    final Map<String, ColumnStatus> columnMap = new HashMap<>();

    columns.stream().filter(c -> isSupportedColumnType(c, allowVirtualColumns)).forEach(column -> {
      String columnName = column.getName();
      Button btn = new Button(columnName);
      btn.setStyleName("btn btn-primary btn-small");
      buttonMap.put(columnName, btn);
      columnMap.put(columnName, column);

      btn.addClickHandler(click -> {
        String placeholder = ViewerConstants.OPEN_TEMPLATE_ENGINE + columnName + ViewerConstants.CLOSE_TEMPLATE_ENGINE;
        String currentText = targetInput.getText();

        if (currentText.contains(placeholder)) {
          targetInput.setText(currentText.replace(placeholder, ""));
        } else {
          targetInput.setText(currentText + placeholder);
        }

        syncButtonsWithText(targetInput, buttonMap, columnMap, columnIdsContainer);
      });
      hintPanel.add(btn);
    });

    targetInput.addKeyUpHandler(event -> syncButtonsWithText(targetInput, buttonMap, columnMap, columnIdsContainer));
    targetInput.addBlurHandler(event -> syncButtonsWithText(targetInput, buttonMap, columnMap, columnIdsContainer));

    syncButtonsWithText(targetInput, buttonMap, columnMap, columnIdsContainer);
  }

  private static void syncButtonsWithText(TextBox input, Map<String, Button> buttonMap,
    Map<String, ColumnStatus> columnMap, List<String> container) {
    String text = input.getText();
    container.clear();

    columnMap.forEach((columnName, column) -> {
      String placeholder = ViewerConstants.OPEN_TEMPLATE_ENGINE + columnName + ViewerConstants.CLOSE_TEMPLATE_ENGINE;
      Button btn = buttonMap.get(columnName);

      if (text.contains(placeholder)) {
        btn.setStyleName("btn btn-default btn-small");
        if (!container.contains(column.getId())) {
          container.add(column.getId());
        }
      } else {
        btn.setStyleName("btn btn-primary btn-small");
      }
    });
  }

  public static boolean isSupportedColumnType(ColumnStatus column, boolean allowVirtualColumns) {
    ViewerType.dbTypes type = column.getType();
    if (ViewerType.dbTypes.NESTED.equals(type) || ViewerType.dbTypes.CLOB.equals(type)
      || ViewerType.dbTypes.BINARY.equals(type)) {
      return false;
    }

    return allowVirtualColumns || !ViewerSourceType.VIRTUAL.equals(column.getSourceType());
  }

  public static void selectListBoxValue(ListBox listBox, String value) {
    for (int i = 0; i < listBox.getItemCount(); i++) {
      if (listBox.getValue(i).equals(value)) {
        listBox.setSelectedIndex(i);
        break;
      }
    }
  }

  public static String getVirtualColumnSolrSuffix(ViewerType.dbTypes type) {
    switch (type) {
      case BINARY:
      case CLOB:
        return ViewerConstants.SOLR_DYN_STRING;
      case BOOLEAN:
        return ViewerConstants.SOLR_DYN_BOOLEAN;
      case DATETIME:
      case DATETIME_JUST_DATE:
      case DATETIME_JUST_TIME:
        return ViewerConstants.SOLR_DYN_TDATETIME;
      case ENUMERATION:
        return ViewerConstants.SOLR_DYN_STRING;
      case TIME_INTERVAL:
        return ViewerConstants.SOLR_DYN_DATES;
      case NUMERIC_FLOATING_POINT:
        return ViewerConstants.SOLR_DYN_DOUBLE;
      case NUMERIC_INTEGER:
        return ViewerConstants.SOLR_DYN_LONG;
      default:
        return ViewerConstants.SOLR_DYN_TEXT_GENERAL;
    }
  }
}
