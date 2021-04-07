package com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers;

import static com.databasepreservation.common.client.ViewerConstants.DEFAULT_DOWNLOAD_LABEL_TEMPLATE;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.collection.TemplateStatus;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.common.client.widgets.ColumnTemplateOptions;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

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
  HTML templateEngineLabel;

  @UiField
  CheckBox overallOption;

  @UiField(provided = true)
  ColumnTemplateOptions templateToOverallView;

  @UiField
  CheckBox detailedOption;

  @UiField(provided = true)
  ColumnTemplateOptions templateToDetailedView;

  @UiField(provided = true)
  ColumnTemplateOptions templateToRenderFile;

  @UiField(provided = true)
  ColumnTemplateOptions applicationType;

  public static ColumnOptionsPanel createInstance(TableStatus tableConfiguration, ColumnStatus columnConfiguration) {
    return new ClobColumnOptionsPanel(tableConfiguration, columnConfiguration);
  }

  private ClobColumnOptionsPanel(TableStatus tableConfiguration, ColumnStatus columnConfiguration) {
    buildOverallTemplatePanel(columnConfiguration);
    buildDetailedViewTemplatePanel(columnConfiguration);
    buildTemplateForFileName(tableConfiguration, columnConfiguration);
    buildMIMETypeTextBox(columnConfiguration);

    initWidget(binder.createAndBindUi(this));

    overallOption.setValue(columnConfiguration.getSearchStatus().getList().isShowContent());
    overallOption.addValueChangeHandler(valueChangeEvent -> {
      templateToOverallView.setVisible(!valueChangeEvent.getValue());
    });

    detailedOption.setValue(columnConfiguration.getDetailsStatus().isShowContent());
    detailedOption.addValueChangeHandler(valueChangeEvent -> {
      templateToDetailedView.setVisible(!valueChangeEvent.getValue());
    });

    templateEngineLabel.setHTML(messages.columnManagementTextForTemplateHint(ViewerConstants.TEMPLATE_ENGINE_LINK));
  }

  @Override
  public TemplateStatus getSearchTemplate() {
    if (!overallOption.getValue()) {
      return getTemplateStatus(templateToOverallView);
    }
    return new TemplateStatus();
  }

  @Override
  public TemplateStatus getDetailsTemplate() {
    if (!detailedOption.getValue()) {
      return getTemplateStatus(templateToDetailedView);
    }
    return new TemplateStatus();
  }

  @Override
  public TemplateStatus getExportTemplate() {
    TemplateStatus templateStatus = new TemplateStatus();
    templateStatus.setTemplate(templateToRenderFile.getText());
    return templateStatus;
  }

  public boolean showContentInList() {
    return this.overallOption.getValue();
  }

  public boolean showContentInDetails() {
    return this.detailedOption.getValue();
  }

  public String getApplicationType() {
    return applicationType.getText();
  }

  private void buildDetailedViewTemplatePanel(ColumnStatus columnConfiguration) {
    templateToDetailedView = new ColumnTemplateOptions(messages.columnManagementLabelForTemplateDetail(),
      ColumnOptionUtils.getDefaultTextOrValue(columnConfiguration.getSearchStatus().getList().getTemplate()), true);

    templateToDetailedView.addStyleNameToLabel("form-label");
    templateToDetailedView.addStyleNameToTextBox("form-textbox");
    templateToDetailedView.buildHintForLabel(messages.columnManagementTextForPossibleFields());

    if (columnConfiguration.getDetailsStatus().isShowContent()) {
      templateToDetailedView.setVisible(false);
    }
  }

  private void buildOverallTemplatePanel(ColumnStatus columnConfiguration) {
    templateToOverallView = new ColumnTemplateOptions(messages.columnManagementLabelForTemplateList(),
      ColumnOptionUtils.getDefaultTextOrValue(columnConfiguration.getSearchStatus().getList().getTemplate()), true);

    templateToOverallView.addStyleNameToLabel("form-label");
    templateToOverallView.addStyleNameToTextBox("form-textbox");
    templateToOverallView.buildHintForLabel(messages.columnManagementTextForPossibleFields());

    if (columnConfiguration.getSearchStatus().getList().isShowContent()) {
      templateToOverallView.setVisible(false);
    }
  }

  private void buildTemplateForFileName(TableStatus tableConfiguration, ColumnStatus columnConfiguration) {
    templateToRenderFile = new ColumnTemplateOptions(messages.binaryColumnTemplateForFilename(),
      ColumnOptionUtils.getDefaultTextOrValue(columnConfiguration.getExportStatus().getTemplateStatus()), false);

    templateToRenderFile.addStyleNameToLabel("form-label");
    templateToRenderFile.addStyleNameToTextBox("form-textbox");
    templateToRenderFile.buildHintForButtons(tableConfiguration, messages.columnManagementTextForPossibleFields());
  }

  private void buildMIMETypeTextBox(ColumnStatus columnConfiguration) {
    applicationType = new ColumnTemplateOptions(messages.binaryColumnMIMEType(),
      columnConfiguration.getApplicationType(), false);
    applicationType.addStyleNameToLabel("form-label");
    applicationType.addStyleNameToTextBox("form-textbox");

  }

  private TemplateStatus getTemplateStatus(ColumnTemplateOptions options) {
    TemplateStatus templateStatus = new TemplateStatus();

    String str = options.getText();
    if (ViewerStringUtils.isBlank(str)) {
      templateStatus.setTemplate(DEFAULT_DOWNLOAD_LABEL_TEMPLATE);
    } else {
      templateStatus.setTemplate(str);
    }

    return templateStatus;
  }
}
