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

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class MetadataExportOptions extends WizardPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface MetadataUiBinder extends UiBinder<Widget, MetadataExportOptions> {
  }

  private static MetadataUiBinder binder = GWT.create(MetadataUiBinder.class);

  @UiField
  FlowPanel content;

  private static MetadataExportOptions instance = null;

  public static MetadataExportOptions getInstance() {
    if (instance == null) {
      instance = new MetadataExportOptions();
    }
    return instance;
  }

  private MetadataExportOptions() {
    initWidget(binder.createAndBindUi(this));

    content.add(new HTML("METADATA EXPORT OPTIONS"));
  }

  @Override
  public void clear() {

  }

  @Override
  public void validate() {

  }

  @Override
  public void getValues() {

  }
}