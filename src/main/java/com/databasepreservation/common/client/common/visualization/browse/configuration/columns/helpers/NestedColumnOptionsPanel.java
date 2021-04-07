package com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TemplateStatus;
import com.databasepreservation.common.client.widgets.Alert;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class NestedColumnOptionsPanel extends ColumnOptionsPanel {
  interface ColumnsOptionsPanelUiBinder extends UiBinder<Widget, NestedColumnOptionsPanel> {
  }

  private static ColumnsOptionsPanelUiBinder binder = GWT.create(ColumnsOptionsPanelUiBinder.class);

  @UiField
  ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  TextBox templateList, templateDetail, templateExport, items;

  @UiField
  FlowPanel templateListHint, templateDetailHint, templateExportHint;

  @UiField
  HTML templateEngineLabel;

  @UiField
  FlowPanel content, templateDetailPanel, templateExportPanel, separatorPanel;

  @UiField
  IntegerBox quantityList;

  public static ColumnOptionsPanel createInstance(ColumnStatus columnStatus) {
    return new NestedColumnOptionsPanel(columnStatus);
  }

  @Override
  public TemplateStatus getSearchTemplate() {
    TemplateStatus templateStatus = new TemplateStatus();
    templateStatus.setTemplate(templateList.getText());
    templateStatus.setSeparator(items.getText());
    return templateStatus;
  }

  @Override
  public TemplateStatus getDetailsTemplate() {
    TemplateStatus templateStatus = new TemplateStatus();
    templateStatus.setTemplate(templateDetail.getText());
    return templateStatus;
  }

  @Override
  public TemplateStatus getExportTemplate() {
    TemplateStatus templateStatus = new TemplateStatus();
    templateStatus.setTemplate(templateExport.getText());
    return templateStatus;
  }

  public int getQuantityInList() {
    return quantityList.getValue();
  }

  private NestedColumnOptionsPanel(ColumnStatus columnStatus) {
    initWidget(binder.createAndBindUi(this));

    templateEngineLabel.setHTML(messages.columnManagementTextForTemplateHint(ViewerConstants.TEMPLATE_ENGINE_LINK));

    // Template list, used on tablePanel
    templateList.setText(columnStatus.getSearchStatus().getList().getTemplate().getTemplate());

    // separator, used on tablePanel
    items.setText(columnStatus.getSearchStatus().getList().getTemplate().getSeparator());

    // Template detail, used on rowPanel
    templateDetail.setText(columnStatus.getDetailsStatus().getTemplateStatus().getTemplate());

    // Template export, used on export data to CSV
    templateExport.setText(columnStatus.getExportStatus().getTemplateStatus().getTemplate());

    if (columnStatus.getNestedColumns() != null) {

      templateListHint.add(buildHintWithButtons(columnStatus, templateList));
      templateDetailHint.add(buildHintWithButtons(columnStatus, templateDetail));
      templateExportHint.add(buildHintWithButtons(columnStatus, templateExport));

      if (columnStatus.getNestedColumns().getQuantityInList() == null) {
        quantityList.setValue(columnStatus.getNestedColumns().getMaxQuantityInList());
        quantityList.setText(columnStatus.getNestedColumns().getMaxQuantityInList().toString());
      } else {
        quantityList.setValue(columnStatus.getNestedColumns().getQuantityInList());
        quantityList.setText(columnStatus.getNestedColumns().getQuantityInList().toString());
      }

      quantityList.addChangeHandler(event -> {
        if (quantityList.getValue() > columnStatus.getNestedColumns().getMaxQuantityInList()) {
          quantityList.setValue(columnStatus.getNestedColumns().getMaxQuantityInList());
          quantityList.setText(columnStatus.getNestedColumns().getMaxQuantityInList().toString());
        }
      });
    }

    if (columnStatus.getNestedColumns() != null && columnStatus.getNestedColumns().getMultiValue()) {
      separatorPanel.setVisible(true);
      templateDetail.setVisible(false);
      templateDetailHint.setVisible(false);
      templateDetailPanel
        .insert(new Alert(Alert.MessageAlertType.INFO, messages.columnManagementTextForMultiValueFields()), 2);
    }
  }

  private FlowPanel buildHintWithButtons(ColumnStatus columnStatus, TextBox target) {
    FlowPanel hintPanel = new FlowPanel();
    hintPanel.setStyleName("data-transformation-title");
    hintPanel.add(new Label(messages.columnManagementTextForPossibleFields()));

    for (String nestedField : columnStatus.getNestedColumns().getNestedFields()) {
      Button btnField = new Button(nestedField);
      btnField.setStyleName("btn btn-primary btn-small");
      btnField.addClickHandler(click -> {
        target.setText(target.getText() + ViewerConstants.OPEN_TEMPLATE_ENGINE + btnField.getText()
          + ViewerConstants.CLOSE_TEMPLATE_ENGINE);
      });

      hintPanel.add(btnField);
    }

    return hintPanel;
  }
}