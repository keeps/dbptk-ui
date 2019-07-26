package com.databasepreservation.main.desktop.client.dbptk.wizard.sendTo;

import com.databasepreservation.main.common.shared.ViewerConstants;
import com.databasepreservation.main.desktop.client.dbptk.wizard.WizardPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ExportFormat extends WizardPanel<String> {
  @UiField
  static ClientMessages messages = GWT.create(ClientMessages.class);

  interface CustomViewsUiBinder extends UiBinder<Widget, ExportFormat> {
  }

  private static CustomViewsUiBinder binder = GWT.create(CustomViewsUiBinder.class);

  @UiField
  FlowPanel container;

  @UiField
  Button buttonDBMS, buttonSIARD;

  private String exportValue = null;

  private static ExportFormat instance = null;

  public static ExportFormat getInstance(String databaseUUID) {
    if (instance == null) {
      instance = new ExportFormat(databaseUUID);
    }
    return instance;
  }

  private ExportFormat(String databaseUUID) {
    initWidget(binder.createAndBindUi(this));

    final SendToWizardManager instance = SendToWizardManager.getInstance(databaseUUID);

    instance.enableNext(false);

    buttonDBMS.addClickHandler(event -> {
      exportValue = ViewerConstants.EXPORT_FORMAT_DBMS;
      buttonDBMS.addStyleName("btn-selected");
      buttonSIARD.removeStyleName("btn-selected");
      instance.enableNext(true);
    });

    buttonSIARD.addClickHandler(event -> {
      exportValue = ViewerConstants.EXPORT_FORMAT_SIARD;
      buttonSIARD.addStyleName("btn-selected");
      buttonDBMS.removeStyleName("btn-selected");
      instance.enableNext(true);
    });
  }

  @Override
  public void clear() {
    exportValue = null;
    instance = null;
  }

  @Override
  public boolean validate() {
    return true;
  }

  @Override
  public String getValues() {
    return exportValue;
  }

  @Override
  public void error() { }
}
