package com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.configuration.collection.ViewerColumnConfiguration;
import com.databasepreservation.common.client.models.configuration.collection.ViewerTemplateConfiguration;
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

  public static ColumnOptionsPanel createInstance(ViewerColumnConfiguration viewerColumnConfiguration) {
    return new NestedColumnOptionsPanel(viewerColumnConfiguration);
  }

  @Override
  public ViewerTemplateConfiguration getSearchTemplate() {
    ViewerTemplateConfiguration viewerTemplateConfiguration = new ViewerTemplateConfiguration();
    viewerTemplateConfiguration.setTemplate(templateList.getText());
    viewerTemplateConfiguration.setSeparator(items.getText());
    return viewerTemplateConfiguration;
  }

  @Override
  public ViewerTemplateConfiguration getDetailsTemplate() {
    ViewerTemplateConfiguration viewerTemplateConfiguration = new ViewerTemplateConfiguration();
    viewerTemplateConfiguration.setTemplate(templateDetail.getText());
    return viewerTemplateConfiguration;
  }

  @Override
  public ViewerTemplateConfiguration getExportTemplate() {
    ViewerTemplateConfiguration viewerTemplateConfiguration = new ViewerTemplateConfiguration();
    viewerTemplateConfiguration.setTemplate(templateExport.getText());
    return viewerTemplateConfiguration;
  }

  public int getQuantityInList() {
    return quantityList.getValue();
  }

  private NestedColumnOptionsPanel(ViewerColumnConfiguration viewerColumnConfiguration) {
    initWidget(binder.createAndBindUi(this));

    templateEngineLabel.setHTML(messages.columnManagementTextForTemplateHint(ViewerConstants.TEMPLATE_ENGINE_LINK));

    //Template list, used on tablePanel
    templateList.setText(viewerColumnConfiguration.getViewerSearchConfiguration().getList().getTemplate().getTemplate());

    //separator, used on tablePanel
    items.setText(viewerColumnConfiguration.getViewerSearchConfiguration().getList().getTemplate().getSeparator());

    //Template detail, used on rowPanel
    templateDetail.setText(viewerColumnConfiguration.getViewerDetailsConfiguration().getViewerTemplateConfiguration().getTemplate());

    //Template export, used on export data to CSV
    templateExport.setText(viewerColumnConfiguration.getViewerExportConfiguration().getViewerTemplateConfiguration().getTemplate());

    if (viewerColumnConfiguration.getNestedColumns() != null) {

      templateListHint.add(buildHintWithButtons(viewerColumnConfiguration, templateList));
      templateDetailHint.add(buildHintWithButtons(viewerColumnConfiguration, templateDetail));
      templateExportHint.add(buildHintWithButtons(viewerColumnConfiguration, templateExport));

      if (viewerColumnConfiguration.getNestedColumns().getQuantityInList() == null) {
        quantityList.setValue(viewerColumnConfiguration.getNestedColumns().getMaxQuantityInList());
        quantityList.setText(viewerColumnConfiguration.getNestedColumns().getMaxQuantityInList().toString());
      } else {
        quantityList.setValue(viewerColumnConfiguration.getNestedColumns().getQuantityInList());
        quantityList.setText(viewerColumnConfiguration.getNestedColumns().getQuantityInList().toString());
      }

      quantityList.addChangeHandler(event -> {
        if(quantityList.getValue() > viewerColumnConfiguration.getNestedColumns().getMaxQuantityInList()){
          quantityList.setValue(viewerColumnConfiguration.getNestedColumns().getMaxQuantityInList());
          quantityList.setText(viewerColumnConfiguration.getNestedColumns().getMaxQuantityInList().toString());
        }
      });
    }

    if(viewerColumnConfiguration.getNestedColumns() != null && viewerColumnConfiguration.getNestedColumns().getMultiValue()){
      separatorPanel.setVisible(true);
      templateDetail.setVisible(false);
      templateDetailHint.setVisible(false);
      templateDetailPanel.insert(new Alert(Alert.MessageAlertType.INFO, messages.columnManagementTextForMultiValueFields()), 2);
    }
  }

  private FlowPanel buildHintWithButtons(ViewerColumnConfiguration viewerColumnConfiguration, TextBox target){
    FlowPanel hintPanel = new FlowPanel();
    hintPanel.setStyleName("data-transformation-title");
    hintPanel.add(new Label(messages.columnManagementTextForPossibleFields()));

    for (String nestedField : viewerColumnConfiguration.getNestedColumns().getNestedFields()) {
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