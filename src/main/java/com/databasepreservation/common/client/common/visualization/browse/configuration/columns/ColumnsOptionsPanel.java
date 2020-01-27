package com.databasepreservation.common.client.common.visualization.browse.configuration.columns;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.widgets.Alert;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ColumnsOptionsPanel extends Composite {
  interface ColumnsOptionsPanelUiBinder extends UiBinder<Widget, ColumnsOptionsPanel> {
  }

  private static ColumnsOptionsPanelUiBinder binder = GWT.create(ColumnsOptionsPanelUiBinder.class);
  private static Map<String, ColumnsOptionsPanel> instances = new HashMap<>();

  @UiField
  ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  TextBox templateList;

  @UiField
  Label templateListHint;

  @UiField
  FlowPanel templateDetailPanel;

  @UiField
  TextBox templateDetail;

  @UiField
  Label templateDetailHint;

  @UiField
  IntegerBox quantityList;

  public static ColumnsOptionsPanel getInstance(String databaseUUID, ColumnStatus columnStatus) {
    return instances.computeIfAbsent(databaseUUID + columnStatus.getId(),
        k -> new ColumnsOptionsPanel(columnStatus));
  }

  private ColumnsOptionsPanel(ColumnStatus columnStatus) {
    initWidget(binder.createAndBindUi(this));

    templateList.setText(columnStatus.getSearchStatus().getList().getTemplate().getTemplate());
    templateList.addChangeHandler(event -> {
      columnStatus.getSearchStatus().getList().getTemplate().setTemplate(templateList.getText());
    });

    templateDetail.setText(columnStatus.getDetailsStatus().getTemplateStatus().getTemplate());
    templateDetail.addChangeHandler(event -> {
      columnStatus.getDetailsStatus().getTemplateStatus().setTemplate(templateDetail.getText());
    });

    if (columnStatus.getNestedColumns() != null) {
      templateListHint
        .setText(messages.columnManagementTextForPossibleFields(columnStatus.getNestedColumns().getNestedFields()));
      templateDetailHint
        .setText(messages.columnManagementTextForPossibleFields(columnStatus.getNestedColumns().getNestedFields()));

      quantityList.setValue(columnStatus.getNestedColumns().getMaxQuantityInList());
      quantityList.setText(columnStatus.getNestedColumns().getMaxQuantityInList().toString());

      quantityList.addChangeHandler(event ->{
        if(quantityList.getValue() > columnStatus.getNestedColumns().getMaxQuantityInList()){
          quantityList.setValue(columnStatus.getNestedColumns().getMaxQuantityInList());
        }
        columnStatus.getNestedColumns().setQuantityInList(quantityList.getValue());
      });
    }

    if(columnStatus.getNestedColumns().getMultiValue()){
      templateDetail.setVisible(false);
      templateDetailHint.setVisible(false);
      templateDetailPanel.insert(new Alert(Alert.MessageAlertType.INFO, messages.columnManagementTextForMultiValueFields()), 2);
    }
  }

}