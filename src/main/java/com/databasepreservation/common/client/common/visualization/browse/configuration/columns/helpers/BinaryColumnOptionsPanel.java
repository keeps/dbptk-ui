/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers;

import static com.databasepreservation.common.client.ViewerConstants.DEFAULT_DOWNLOAD_LABEL_TEMPLATE;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.utils.ApplicationTypeActions;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.collection.TemplateStatus;
import com.databasepreservation.common.client.tools.MimeTypeUtils;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

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
    if (autoDetectRadioBtn.getValue()) {
      return MimeTypeUtils.getAutoDetectMimeTypeTemplate();
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

    if (columnConfiguration.getApplicationType().equals(MimeTypeUtils.getAutoDetectMimeTypeTemplate())) {
      autoDetectRadioBtn.setValue(true);
      applicationTypeValue.setVisible(false);
      applicationTypeValue.setText("");
    } else {
      staticValueRadioBtn.setValue(true);
      applicationTypeValue.setVisible(true);
      applicationTypeValue.setText(columnConfiguration.getApplicationType());
    }

    autoDetectRadioBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        applicationTypeValue.setVisible(false);
        if (columnConfiguration.getApplicationType().equals(MimeTypeUtils.getAutoDetectMimeTypeTemplate())) {
          applicationTypeValue.setText(ViewerConstants.MEDIA_TYPE_APPLICATION_OCTET_STREAM);
        } else {
          applicationTypeValue.setText(columnConfiguration.getApplicationType());
        }
      }
    });

    staticValueRadioBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        applicationTypeValue.setVisible(true);
        if (columnConfiguration.getApplicationType().equals(MimeTypeUtils.getAutoDetectMimeTypeTemplate())) {
          applicationTypeValue.setText(ViewerConstants.MEDIA_TYPE_APPLICATION_OCTET_STREAM);
        } else {
          applicationTypeValue.setText(columnConfiguration.getApplicationType());
        }
      }
    });

    applicationTypePanel.addStyleName("dialog-blog-mime-type-panel");
  }
}