/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers;

import static com.databasepreservation.common.client.ViewerConstants.DEFAULT_DETAILED_VIEWER_LABEL_TEMPLATE;
import static com.databasepreservation.common.client.ViewerConstants.DEFAULT_DOWNLOAD_LABEL_TEMPLATE;
import static com.databasepreservation.common.client.ViewerConstants.DEFAULT_VIEWER_DOWNLOAD_LABEL_TEMPLATE;

import com.databasepreservation.common.client.ClientConfigurationManager;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.utils.ApplicationTypeActions;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.collection.TemplateStatus;
import com.databasepreservation.common.client.tools.ViewerCelllUtils;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
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

  /* --- Application Type --- */

  @UiField
  FlowPanel applicationTypePanel;

  @UiField
  HTML applicationTypeHintLabel;

  @UiField
  VerticalPanel applicationTypeBtnPanel;

  @UiField
  TextBox applicationTypeValue;

  /* ----------- */

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

  private final String applicationTypeActionsGroup = "applicationTypeActionsGroup";
  private RadioButton autoDetectRadioBtn;
  private RadioButton staticValueRadioBtn;

  public static ColumnOptionsPanel createInstance(TableStatus tableConfiguration, ColumnStatus columnConfiguration) {
    return new BinaryColumnOptionsPanel(tableConfiguration, columnConfiguration);
  }

  @Override
  public TemplateStatus getSearchTemplate() {
    TemplateStatus templateStatus = new TemplateStatus();
    if (ViewerStringUtils.isBlank(this.displayList.getText())) {
      if (ClientConfigurationManager.getBoolean(false, ViewerConstants.VIEWER_ENABLED)) {
        templateStatus.setTemplate(DEFAULT_VIEWER_DOWNLOAD_LABEL_TEMPLATE);
      } else {
        templateStatus.setTemplate(DEFAULT_DOWNLOAD_LABEL_TEMPLATE);
      }
    } else {
      templateStatus.setTemplate(this.displayList.getText());
    }

    return templateStatus;
  }

  @Override
  public TemplateStatus getDetailsTemplate() {
    TemplateStatus templateStatus = new TemplateStatus();
    if (ViewerStringUtils.isBlank(this.detailsList.getText())) {
      if (ClientConfigurationManager.getBoolean(false, ViewerConstants.VIEWER_ENABLED)) {
        templateStatus.setTemplate(DEFAULT_DETAILED_VIEWER_LABEL_TEMPLATE);
      } else {
        templateStatus.setTemplate(DEFAULT_DOWNLOAD_LABEL_TEMPLATE);
      }
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
    if (autoDetectRadioBtn.getValue()) {
      return ViewerCelllUtils.getAutoDetectMimeTypeTemplate();
    } else {
      return applicationTypeValue.getText();
    }
  }

  private BinaryColumnOptionsPanel(TableStatus tableConfiguration, ColumnStatus columnConfiguration) {
    initWidget(binder.createAndBindUi(this));

    templateEngineLabel.setHTML(messages.columnManagementTextForTemplateHint(ViewerConstants.TEMPLATE_ENGINE_LINK));
    templateListHint.add(ColumnOptionUtils.buildHintWithButtons(tableConfiguration, templateList,
      messages.columnManagementTextForPossibleFields(), true));

    templateList.setText(columnConfiguration.getExportStatus().getTemplateStatus().getTemplate());

    displayList
      .setText(ColumnOptionUtils.getDefaultTextOrValue(columnConfiguration.getSearchStatus().getList().getTemplate()));
    displayListHint
      .add(ColumnOptionUtils.buildHintForLabel(displayList, messages.columnManagementTextForPossibleFields()));

    detailsList
      .setText(ColumnOptionUtils.getDefaultTextOrValue(columnConfiguration.getDetailsStatus().getTemplateStatus()));
    detailsListHint
      .add(ColumnOptionUtils.buildHintForLabel(detailsList, messages.columnManagementTextForPossibleFields()));

    staticValueRadioBtn = new RadioButton(applicationTypeActionsGroup,
      messages.columnManagementApplicationTypeAction(ApplicationTypeActions.STATIC_VALUE.toString()));
    autoDetectRadioBtn = new RadioButton(applicationTypeActionsGroup,
      messages.columnManagementApplicationTypeAction(ApplicationTypeActions.AUTO_DETECT.toString()));

    applicationTypeHintLabel.setHTML(messages.columnManagementTextForApplicationTypeHint());
    applicationTypeHintLabel.addStyleName("dialog-blog-mime-type-hint");
    applicationTypeBtnPanel.add(staticValueRadioBtn);
    applicationTypeBtnPanel.add(autoDetectRadioBtn);

    if (ViewerCelllUtils.getAutoDetectMimeTypeTemplate().equals(columnConfiguration.getApplicationType())) {
      autoDetectRadioBtn.setValue(true);
      applicationTypeValue.setVisible(false);
      applicationTypeValue.setText("");
    } else {
      staticValueRadioBtn.setValue(true);
      applicationTypeValue.setVisible(true);
      applicationTypeValue.setText(columnConfiguration.getApplicationType());
    }

    autoDetectRadioBtn.addClickHandler(clickEvent -> {
      applicationTypeValue.setVisible(false);
      if (ViewerCelllUtils.getAutoDetectMimeTypeTemplate().equals(columnConfiguration.getApplicationType())) {
        applicationTypeValue.setText(ViewerConstants.MEDIA_TYPE_APPLICATION_OCTET_STREAM);
      } else {
        applicationTypeValue.setText(columnConfiguration.getApplicationType());
      }
    });

    staticValueRadioBtn.addClickHandler(clickEvent -> {
      applicationTypeValue.setVisible(true);
      if (ViewerCelllUtils.getAutoDetectMimeTypeTemplate().equals(columnConfiguration.getApplicationType())) {
        applicationTypeValue.setText(ViewerConstants.MEDIA_TYPE_APPLICATION_OCTET_STREAM);
      } else {
        applicationTypeValue.setText(columnConfiguration.getApplicationType());
      }
    });

    applicationTypePanel.addStyleName("dialog-blog-mime-type-panel");
  }
}