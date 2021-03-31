package com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.collection.TemplateStatus;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;

import static com.databasepreservation.common.client.ViewerConstants.DEFAULT_DOWNLOAD_LABEL_TEMPLATE;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ClobColumnOptionsPanel extends ColumnOptionsPanel {
  interface ColumnsOptionsPanelUiBinder extends UiBinder<Widget, ClobColumnOptionsPanel> {
  }

  private static ColumnsOptionsPanelUiBinder binder = GWT.create(ColumnsOptionsPanelUiBinder.class);

  @UiField
  ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FlowPanel templateListHint;

  @UiField
  HTML templateEngineLabel;

  @UiField
  TextBox templateList;

  @UiField
  TextBox applicationType;

  @UiField
  TextBox displayList;

  @UiField
  FlowPanel displayListHint;

  @UiField
  FlowPanel content;

  @UiField
  CheckBox showContent;

  public static ColumnOptionsPanel createInstance(TableStatus tableConfiguration, ColumnStatus columnConfiguration) {
    return new ClobColumnOptionsPanel(tableConfiguration, columnConfiguration);
  }

  private ClobColumnOptionsPanel(TableStatus tableConfiguration, ColumnStatus columnConfiguration) {
    initWidget(binder.createAndBindUi(this));

    templateEngineLabel.setHTML(messages.columnManagementTextForTemplateHint(ViewerConstants.TEMPLATE_ENGINE_LINK));
    templateList.setText(columnConfiguration.getExportStatus().getTemplateStatus().getTemplate());
    templateListHint.add(ColumnOptionUtils.buildHintWithButtons(tableConfiguration, templateList, messages.columnManagementTextForPossibleFields()));

    displayList.setText(ColumnOptionUtils.getDefaultTextOrValue(columnConfiguration.getSearchStatus().getList().getTemplate()));
    displayListHint.add(ColumnOptionUtils.buildHintForLabel(displayList, messages.columnManagementTextForPossibleFields()));

    applicationType.setText(columnConfiguration.getApplicationType());

    showContent.setValue(columnConfiguration.getDetailsStatus().isShowContent());
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
    templateStatus.setTemplate(DEFAULT_DOWNLOAD_LABEL_TEMPLATE);
    return templateStatus;
  }

  @Override
  public TemplateStatus getExportTemplate() {
    TemplateStatus templateStatus = new TemplateStatus();
    templateStatus.setTemplate(this.templateList.getText());
    return templateStatus;
  }

  public boolean showContent() {
    return this.showContent.getValue();
  }

  public String getApplicationType() {
    return this.applicationType.getText();
  }

}
