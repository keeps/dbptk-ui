package com.databasepreservation.main.desktop.client.dbptk.wizard.create.exportOptions;

import com.databasepreservation.main.desktop.client.dbptk.wizard.WizardPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

import java.util.HashMap;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ExternalLOBExportOptions extends WizardPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface ExternalLOBUiBinder extends UiBinder<Widget, ExternalLOBExportOptions> {
  }

  private static ExternalLOBUiBinder binder = GWT.create(ExternalLOBUiBinder.class);

  @UiField
  FlowPanel content;

  private static ExternalLOBExportOptions instance = null;

  public static ExternalLOBExportOptions getInstance() {
    if (instance == null) {
      instance = new ExternalLOBExportOptions();
    }
    return instance;
  }

  private ExternalLOBExportOptions() {
    initWidget(binder.createAndBindUi(this));

    content.add(new HTML("EXTERNAL LOBs"));
  }

  @Override
  public void clear() {

  }

  @Override
  public boolean validate() {
    return false;
  }

  @Override
  public HashMap<String, String> getValues() {
    return null;
  }

  @Override
  public void error() {

  }
}