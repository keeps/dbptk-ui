package com.databasepreservation.desktop.client.dbptk.wizard.common.exportOptions;


import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.databasepreservation.common.client.common.desktop.ComboBoxField;
import com.databasepreservation.common.client.tools.ToolkitModuleName2ViewerModuleName;
import com.databasepreservation.common.client.models.DBPTKModule;
import com.databasepreservation.common.client.models.parameters.PreservationParameter;
import com.databasepreservation.common.client.models.parameters.ExportOptionsParameters;
import com.databasepreservation.common.client.services.ModulesService;
import com.databasepreservation.desktop.client.dbptk.wizard.WizardPanel;
import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class SIARDExportOptions extends WizardPanel<ExportOptionsParameters> {
  @UiField
  ClientMessages messages = GWT.create(ClientMessages.class);

  static int EXTERNAL_LOBS_ERROR = 1;
  static int MISSING_FIELD = 2;
  static int MISSING_FILE = 3;
  static int SIARDDK_FOLDER_NAME = 4;
  static int OK = -1;

  interface SIARDUiBinder extends UiBinder<Widget, SIARDExportOptions> {
  }

  interface DBPTKModuleMapper extends ObjectMapper<DBPTKModule> {
  }

  private static SIARDUiBinder binder = GWT.create(SIARDUiBinder.class);

  @UiField
  FlowPanel content;

  @UiField
  SimplePanel listBox;

  private static SIARDExportOptions instance = null;
  private String version;
  private DBPTKModule dbptkModule;
  private int validationError = 0;

  public static SIARDExportOptions getInstance(String path) {
    if (instance == null) {
      instance = new SIARDExportOptions(path);
    }
    return instance;
  }

  public static SIARDExportOptions getInstance() {
    return SIARDExportOptions.getInstance(null);
  }

  private SIARDExportOptions(String path) {
    initWidget(binder.createAndBindUi(this));

    Widget spinner = new HTML(SafeHtmlUtils.fromSafeConstant(
        "<div class='spinner'><div class='double-bounce1'></div><div class='double-bounce2'></div></div>"));

    content.add(spinner);

    ModulesService.Util.call((DBPTKModule result) -> {
      content.remove(spinner);
      ComboBoxField comboBoxField = ComboBoxField.createInstance(messages.wizardExportOptionsLabels("version"));
      final Map<String, List<PreservationParameter>> modules = new TreeMap<>(result.getParameters());
      for (String moduleName : modules.keySet()) {
        comboBoxField.setComboBoxValue(ToolkitModuleName2ViewerModuleName.transform(moduleName), moduleName);
      }
      comboBoxField.addChangeHandler(() -> {
        version = comboBoxField.getSelectedValue();
        dbptkModule = result;
        final SIARDExportOptionsCurrent instance = SIARDExportOptionsCurrent.getInstance(version, dbptkModule, path);
        content.clear();
        content.add(instance);
      });
      comboBoxField.setCSSMetadata("form-row", "form-label-spaced", "form-combobox");
      comboBoxField.select(1);

      listBox.add(comboBoxField);
    }).getSIARDExportModules();
  }

  @Override
  public void clear() {
    SIARDExportOptionsCurrent.getInstance(version, dbptkModule).clear();
    instance = null;
    validationError = 0;
  }

  @Override
  public boolean validate() {
    validationError = SIARDExportOptionsCurrent.getInstance(version, dbptkModule).validate();
    return validationError == SIARDExportOptions.OK;
  }

  @Override
  public ExportOptionsParameters getValues() {
    return SIARDExportOptionsCurrent.getInstance(version, dbptkModule).getValues();
  }

  @Override
  public void error() {
    SIARDExportOptionsCurrent.getInstance(version, dbptkModule).error();
  }
}