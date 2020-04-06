package com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers;

import static com.databasepreservation.common.client.ViewerConstants.DEFAULT_DOWNLOAD_LABEL_TEMPLATE;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.collection.TemplateStatus;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class BinaryColumnOptionsPanel extends ColumnOptionsPanel {
  interface ColumnsOptionsPanelUiBinder extends UiBinder<Widget, BinaryColumnOptionsPanel> {
  }

  private static ColumnsOptionsPanelUiBinder binder = GWT.create(ColumnsOptionsPanelUiBinder.class);

  @UiField
  ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  TextBox templateList;

  @UiField
  FlowPanel templateListHint;

  @UiField
  HTML templateEngineLabel;

  @UiField
  TextBox applicationType;

  @UiField
  TextBox displayList;

  @UiField
  FlowPanel displayListHint;

  @UiField
  TextBox detailsList;

  @UiField
  FlowPanel detailsListHint;

  @UiField
  FlowPanel content;

  public static ColumnOptionsPanel createInstance(TableStatus tableConfiguration, ColumnStatus columnConfiguration) {
    return new BinaryColumnOptionsPanel(tableConfiguration, columnConfiguration);
  }

  @Override
  public TemplateStatus getSearchTemplate() {
    TemplateStatus templateStatus = new TemplateStatus();
    if (ViewerStringUtils.isBlank(this.displayList.getText())) {
      templateStatus.setTemplate(DEFAULT_DOWNLOAD_LABEL_TEMPLATE);
    } else {
      templateStatus.setTemplate(this.displayList.getText());
    }

    return templateStatus;
  }

  @Override
  public TemplateStatus getDetailsTemplate() {
    TemplateStatus templateStatus = new TemplateStatus();
    if (ViewerStringUtils.isBlank(this.detailsList.getText())) {
      templateStatus.setTemplate(DEFAULT_DOWNLOAD_LABEL_TEMPLATE);
    } else {
      templateStatus.setTemplate(this.detailsList.getText());
    }
    return templateStatus;
  }

  @Override
  public TemplateStatus getExportTemplate() {
    TemplateStatus templateStatus = new TemplateStatus();
    templateStatus.setTemplate(this.templateList.getText());
    return templateStatus;
  }

  public String getApplicationType() {
    return this.applicationType.getText();
  }

  private BinaryColumnOptionsPanel(TableStatus tableConfiguration, ColumnStatus columnConfiguration) {
    initWidget(binder.createAndBindUi(this));

    templateEngineLabel.setHTML(messages.columnManagementTextForTemplateHint(ViewerConstants.TEMPLATE_ENGINE_LINK));
    templateList.setText(columnConfiguration.getExportStatus().getTemplateStatus().getTemplate());
    templateListHint.add(buildHintWithButtons(tableConfiguration, templateList));

    displayList.setText(getDefaultTextOrValue(columnConfiguration.getSearchStatus().getList().getTemplate()));
    displayListHint.add(buildHintForLabel(displayList));

    detailsList.setText(getDefaultTextOrValue(columnConfiguration.getDetailsStatus().getTemplateStatus()));
    detailsListHint.add(buildHintForLabel(detailsList));

    applicationType.setText(columnConfiguration.getApplicationType());
  }

  private String getDefaultTextOrValue(TemplateStatus templateConfig) {
    String template = templateConfig.getTemplate();

    if (ViewerStringUtils.isBlank(template)) {
      template = DEFAULT_DOWNLOAD_LABEL_TEMPLATE;
    }

    return template;
  }

  private FlowPanel buildHintForLabel(TextBox target) {
    FlowPanel hintPanel = new FlowPanel();
    hintPanel.setStyleName("data-transformation-title");
    hintPanel.add(new Label(messages.columnManagementTextForPossibleFields()));

    Button btnDownloadLink = new Button(ViewerConstants.TEMPLATE_LOB_DOWNLOAD_LINK);
    btnDownloadLink.setStyleName("btn btn-primary btn-small");
    btnDownloadLink.addClickHandler(event -> {
      target.setText(target.getText() + ViewerConstants.OPEN_TEMPLATE_ENGINE
        + ViewerConstants.TEMPLATE_LOB_DOWNLOAD_LINK + ViewerConstants.CLOSE_TEMPLATE_ENGINE);
    });
    Button btnDownloadLabel = new Button(ViewerConstants.TEMPLATE_LOB_DOWNLOAD_LABEL);
    btnDownloadLabel.setStyleName("btn btn-primary btn-small");
    btnDownloadLabel.addClickHandler(event -> {
      target.setText(target.getText() + ViewerConstants.OPEN_TEMPLATE_ENGINE
        + ViewerConstants.TEMPLATE_LOB_DOWNLOAD_LABEL + ViewerConstants.CLOSE_TEMPLATE_ENGINE);
    });

    hintPanel.add(btnDownloadLink);
    hintPanel.add(btnDownloadLabel);

    return hintPanel;
  }

  private FlowPanel buildHintWithButtons(TableStatus tableConfiguration, TextBox target) {
    FlowPanel hintPanel = new FlowPanel();
    hintPanel.setStyleName("data-transformation-title");
    hintPanel.add(new Label(messages.columnManagementTextForPossibleFields()));

    for (ColumnStatus column : tableConfiguration.getColumns()) {
      Button btnField = new Button(column.getCustomName());
      btnField.setStyleName("btn btn-primary btn-small");
      btnField.addClickHandler(handler -> {
        target.setText(target.getText() + ViewerConstants.OPEN_TEMPLATE_ENGINE
          + ViewerStringUtils.replaceAllFor(btnField.getText(), "\\s", "_") + ViewerConstants.CLOSE_TEMPLATE_ENGINE);
      });

      hintPanel.add(btnField);
    }

    return hintPanel;
  }

}