package com.databasepreservation.main.desktop.client.dbptk.wizard.create.exportOptions;


import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.ViewerConstants;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.main.common.shared.client.common.utils.ApplicationType;
import com.databasepreservation.main.common.shared.client.common.utils.JavascriptUtils;
import com.databasepreservation.main.common.shared.client.tools.FontAwesomeIconManager;
import com.databasepreservation.main.common.shared.client.tools.JSOUtils;
import com.databasepreservation.main.common.shared.client.tools.PathUtils;
import com.databasepreservation.main.common.shared.client.tools.ToolkitModuleName2ViewerModuleName;
import com.databasepreservation.main.desktop.client.common.ComboBoxField;
import com.databasepreservation.main.desktop.client.common.FileUploadField;
import com.databasepreservation.main.desktop.client.common.GenericField;
import com.databasepreservation.main.desktop.client.common.sidebar.ConnectionSidebar;
import com.databasepreservation.main.desktop.client.dbptk.wizard.WizardPanel;
import com.databasepreservation.main.desktop.shared.models.DBPTKModule;
import com.databasepreservation.main.desktop.shared.models.Filter;
import com.databasepreservation.main.desktop.shared.models.PreservationParameter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;

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
  private HashMap<String, TextBox> textBoxInputs = new HashMap<>();
  private HashMap<String, CheckBox> checkBoxInputs = new HashMap<>();
  private HashMap<String, ComboBoxField> comboBoxFieldInputs = new HashMap<>();
  private HashMap<String, FileUploadField> fileInputs = new HashMap<>();
  private DBPTKModule dbptkModule;
  private String pathToSave;

  public static SIARDExportOptions getInstance() {
    if (instance == null) {
      instance = new SIARDExportOptions();
    }
    return instance;
  }

  private SIARDExportOptions() {
    initWidget(binder.createAndBindUi(this));

    Widget spinner = new HTML(SafeHtmlUtils.fromSafeConstant(
        "<div class='spinner'><div class='double-bounce1'></div><div class='double-bounce2'></div></div>"));

    content.add(spinner);

    BrowserService.Util.getInstance().getSIARDExportModules(new DefaultAsyncCallback<DBPTKModule>() {
      @Override
      public void onSuccess(DBPTKModule result) {
        dbptkModule = result;
        content.remove(spinner);
      }
    });

    /* ComboBoxField comboBoxField = ComboBoxField.createInstance(messages.connectionLabels(parameter.getName()));

        final TreeMap<String, ArrayList<PreservationParameter>> modules = new TreeMap<>(dbptkModule.getParameters());

        for (String moduleName : modules.keySet()) {
          comboBoxField.setComboBoxValue(ToolkitModuleName2ViewerModuleName.transform(moduleName));
        }

     */
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

  private void buildGenericWidget(PreservationParameter parameter) {

    GenericField genericField = null;

    switch (parameter.getInputType()) {
      case "CHECKBOX":
        CheckBox checkbox = new CheckBox();
        checkbox.setText(messages.connectionLabels(parameter.getName()));
        checkbox.addStyleName("form-checkbox");
        checkBoxInputs.put(parameter.getName(), checkbox);
        genericField = GenericField.createInstance(checkbox);
        break;
      case "FILE":
        break;
      case "FOLDER":
        FileUploadField fileUploadField = FileUploadField.createInstance(messages.connectionLabels(parameter.getName()), messages.siardExportBrowseButton());
        fileUploadField.setParentCSS("form-row");
        fileUploadField.setLabelCSS("form-label-spaced");
        fileUploadField.setButtonCSS("btn btn-link form-button");
        fileUploadField.setRequired(parameter.isRequired());
        fileUploadField.buttonAction(() -> {
          if (ApplicationType.getType().equals(ViewerConstants.ELECTRON)) {
            JavaScriptObject options = JSOUtils.getOpenDialogOptions(Collections.singletonList("openDirectory"), Collections.emptyList());

            String path = JavascriptUtils.openFileDialog(options);
            if (path != null) {
              pathToSave = path;
              String displayPath = PathUtils.getFileName(path);
              fileUploadField.setPathLocation(displayPath, path);
              fileUploadField.setInformationPathCSS("gwt-Label-disabled information-path");
            }
          }
        });
        fileInputs.put(parameter.getName(), fileUploadField);
        content.add(fileUploadField);
        break;
      case "COMBOBOX":

        break;
      case "NUMBER":
      case "TEXT":
      default: TextBox defaultTextBox = new TextBox();
        defaultTextBox.addStyleName("form-textbox");
        textBoxInputs.put(parameter.getName(), defaultTextBox);
        genericField = GenericField.createInstance(messages.connectionLabels(parameter.getName()), defaultTextBox);
        break;
    }

    if (genericField != null) {
      genericField.setRequired(parameter.isRequired());
      genericField.setCSSMetadata("form-row", "form-label-spaced");
      content.add(genericField);
    }
  }
}