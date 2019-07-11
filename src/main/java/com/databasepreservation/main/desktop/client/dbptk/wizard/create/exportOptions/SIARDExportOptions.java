package com.databasepreservation.main.desktop.client.dbptk.wizard.create.exportOptions;


import com.databasepreservation.main.desktop.client.common.ComboBoxField;
import com.databasepreservation.main.desktop.client.common.GenericField;
import com.databasepreservation.main.desktop.client.dbptk.wizard.WizardPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;

import java.util.Arrays;
import java.util.HashMap;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class SIARDExportOptions extends WizardPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface SIARDUiBinder extends UiBinder<Widget, SIARDExportOptions> {
  }

  private static SIARDUiBinder binder = GWT.create(SIARDUiBinder.class);

  @UiField
  FlowPanel content;

  private static SIARDExportOptions instance = null;

  public static SIARDExportOptions getInstance() {
    if (instance == null) {
      instance = new SIARDExportOptions();
    }
    return instance;
  }

  private SIARDExportOptions() {
    initWidget(binder.createAndBindUi(this));

    ComboBoxField siardVersions = ComboBoxField.createInstance(messages.siardversionLabel(), Arrays.asList("SIARD Version 1", "SIARD Version 2.0", "SIARD Version 2.1", "SIARD DK"));

    Button browse = new Button();
    browse.setText(messages.siardExportBrowseButton());

    GenericField destinationFolder = GenericField.createInstance(messages.siardDestinationFolderLabel(), browse);

    ComboBoxField compression = ComboBoxField.createInstance(messages.siardcompressionLabel(), Arrays.asList("Stored", "Deflated"));

    CheckBox prettyXMLCheckbox = new CheckBox();
    prettyXMLCheckbox.setText(messages.siardprettyXMLLabel());
    GenericField prettyXML = GenericField.createInstance(prettyXMLCheckbox);

    CheckBox validateCheckbox = new CheckBox();
    validateCheckbox.setText(messages.siardValidateLabel());
    GenericField validate = GenericField.createInstance(validateCheckbox);

    content.add(siardVersions);
    content.add(destinationFolder);
    content.add(compression);
    content.add(prettyXML);
    content.add(validate);
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