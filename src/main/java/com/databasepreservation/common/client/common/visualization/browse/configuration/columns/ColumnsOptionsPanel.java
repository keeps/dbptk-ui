package com.databasepreservation.common.client.common.visualization.browse.configuration.columns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.widgets.Alert;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
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
  private final String COMMA_SEPARATOR=",";
  private final String BREAK_LINE_SEPARATOR="<br>";
  private final String WHITE_LINE_SEPARATOR=" ";

  @UiField
  ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  TextBox templateList, templateDetail, templateExport;

  @UiField
  FlowPanel templateListHint, templateDetailHint, templateExportHint;

  @UiField
  HTML templateEngineLabel;

  @UiField
  FlowPanel content, templateDetailPanel, templateExportPanel, separatorPanel;

  @UiField
  ListBox items;

  @UiField
  IntegerBox quantityList;

  public static ColumnsOptionsPanel getInstance(String databaseUUID, ColumnStatus columnStatus) {
    return instances.computeIfAbsent(databaseUUID + columnStatus.getId(),
        k -> new ColumnsOptionsPanel(columnStatus));
  }

  private ColumnsOptionsPanel(ColumnStatus columnStatus) {
    initWidget(binder.createAndBindUi(this));

    templateEngineLabel.setHTML(messages.columnManagementTextForTemplateHint(ViewerConstants.TEMPLATE_ENGINE_LINK));

    //Template list, used on tablePanel
    templateList.setText(columnStatus.getSearchStatus().getList().getTemplate().getTemplate());
    templateList.addChangeHandler(event -> {
      columnStatus.getSearchStatus().getList().getTemplate().setTemplate(templateList.getText());
    });

    items.addItem(messages.columnManagementLabelForSeparatorComma(), COMMA_SEPARATOR);
    items.addItem(messages.columnManagementLabelForSeparatorBreakLine(),BREAK_LINE_SEPARATOR);
    items.addItem(messages.columnManagementLabelForSeparatorSpace(),WHITE_LINE_SEPARATOR);
    String separator = columnStatus.getSearchStatus().getList().getTemplate().getSeparator();

    if(separator == null){
      items.setSelectedIndex(0);
    } else {
      for (int i = 0; i < items.getItemCount(); i++) {
        GWT.log("items.getItemText(i): " + items.getItemText(i));
        GWT.log("separator: " + separator);
        if (items.getValue(i).equals(separator)) {
          items.setSelectedIndex(i);
        }
      }
    }

    items.addChangeHandler(event -> {
      columnStatus.getSearchStatus().getList().getTemplate().setSeparator(items.getSelectedValue());
    });

    //Template detail, used on rowPanel
    templateDetail.setText(columnStatus.getDetailsStatus().getTemplateStatus().getTemplate());
    templateDetail.addChangeHandler(event -> {
      columnStatus.getDetailsStatus().getTemplateStatus().setTemplate(templateDetail.getText());
    });

    //Template export, used on export data to CSV
    templateExport.setText(columnStatus.getExportStatus().getTemplateStatus().getTemplate());
    templateExport.addChangeHandler(event -> {
      columnStatus.getExportStatus().getTemplateStatus().setTemplate(templateExport.getText());
    });

    if (columnStatus.getNestedColumns() != null) {

      templateListHint.add(buildHintWithButtons(columnStatus, templateList));
      templateDetailHint.add(buildHintWithButtons(columnStatus, templateDetail));
      templateExportHint.add(buildHintWithButtons(columnStatus, templateExport));

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
      separatorPanel.setVisible(true);
      templateDetail.setVisible(false);
      templateDetailHint.setVisible(false);
      templateDetailPanel.insert(new Alert(Alert.MessageAlertType.INFO, messages.columnManagementTextForMultiValueFields()), 2);
    }
  }

  private FlowPanel buildHintWithButtons(ColumnStatus columnStatus, TextBox target){
    FlowPanel hintPanel = new FlowPanel();
    hintPanel.setStyleName("data-transformation-title");
    hintPanel.add(new Label(messages.columnManagementTextForPossibleFields()));

    for (String nestedField : columnStatus.getNestedColumns().getNestedFields()) {
      Button btnField = new Button(messages.columnManagementBtnTextForFields(nestedField));
      btnField.setStyleName("btn btn-primary btn-small");
      btnField.addClickHandler(click -> {
        target.setText(target.getText() + btnField.getText());
      });

      hintPanel.add(btnField);
    }

    return hintPanel;
  }
}