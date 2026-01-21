package com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.virtual;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
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
    List<String> columnIdsContainer, ClientMessages messages) {

    hintPanel.clear();
    hintPanel.add(new Label(messages.columnManagementTextForPossibleFields()));

    final Map<String, Button> buttonMap = new HashMap<>();

    columns.stream().filter(VirtualOptionsPanelUtils::isSupportedColumnType).forEach(column -> {
      String columnName = column.getName();
      Button btn = new Button(columnName);
      btn.setStyleName("btn btn-primary btn-small");
      buttonMap.put(columnName, btn);

      btn.addClickHandler(click -> {
        String placeholder = ViewerConstants.OPEN_TEMPLATE_ENGINE + columnName + ViewerConstants.CLOSE_TEMPLATE_ENGINE;
        String currentText = targetInput.getText();

        if (currentText.contains(placeholder)) {
          targetInput.setText(currentText.replace(placeholder, ""));
        } else {
          targetInput.setText(currentText + placeholder);
        }

        syncButtonsWithText(targetInput, buttonMap, columnIdsContainer);
      });
      hintPanel.add(btn);
    });

    targetInput.addKeyUpHandler(event -> syncButtonsWithText(targetInput, buttonMap, columnIdsContainer));
    targetInput.addBlurHandler(event -> syncButtonsWithText(targetInput, buttonMap, columnIdsContainer));

    syncButtonsWithText(targetInput, buttonMap, columnIdsContainer);
  }

  private static void syncButtonsWithText(TextBox input, Map<String, Button> buttonMap, List<String> container) {
    String text = input.getText();
    container.clear();

    buttonMap.forEach((columnName, btn) -> {
      String placeholder = ViewerConstants.OPEN_TEMPLATE_ENGINE + columnName + ViewerConstants.CLOSE_TEMPLATE_ENGINE;

      if (text.contains(placeholder)) {
        btn.setStyleName("btn btn-default btn-small");
        if (!container.contains(columnName)) {
          container.add(columnName);
        }
      } else {
        btn.setStyleName("btn btn-primary btn-small");
      }
    });
  }

  public static boolean isSupportedColumnType(ColumnStatus column) {
    ViewerType.dbTypes type = column.getType();
    return !ViewerType.dbTypes.NESTED.equals(type) && !ViewerType.dbTypes.CLOB.equals(type)
      && !ViewerType.dbTypes.BINARY.equals(type);
  }

  public static void selectListBoxValue(ListBox listBox, String value) {
    for (int i = 0; i < listBox.getItemCount(); i++) {
      if (listBox.getValue(i).equals(value)) {
        listBox.setSelectedIndex(i);
        break;
      }
    }
  }
}
