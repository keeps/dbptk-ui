package com.databasepreservation.main.desktop.client.dbptk.wizard.common.exportOptions;


import java.util.ArrayList;
import java.util.TreeMap;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.main.common.shared.client.common.desktop.ComboBoxField;
import com.databasepreservation.main.common.shared.client.tools.ToolkitModuleName2ViewerModuleName;
import com.databasepreservation.main.common.shared.models.DBPTKModule;
import com.databasepreservation.main.common.shared.models.PreservationParameter;
import com.databasepreservation.main.common.shared.models.wizardParameters.ExportOptionsParameters;
import com.databasepreservation.main.desktop.client.dbptk.wizard.WizardPanel;
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

    BrowserService.Util.getInstance().getSIARDExportModules(new DefaultAsyncCallback<String>() {
      @Override
      public void onSuccess(String result) {
        content.remove(spinner);

        ComboBoxField comboBoxField = ComboBoxField.createInstance(messages.wizardExportOptionsLabels("version"));
        DBPTKModuleMapper mapper = GWT.create(DBPTKModuleMapper.class);
        DBPTKModule module = mapper.read(result);
        final TreeMap<String, ArrayList<PreservationParameter>> modules = new TreeMap<>(module.getParameters());
        for (String moduleName : modules.keySet()) {
          comboBoxField.setComboBoxValue(ToolkitModuleName2ViewerModuleName.transform(moduleName), moduleName);
        }
        comboBoxField.addChangeHandler(() -> {
          version = comboBoxField.getSelectedValue();
          dbptkModule = module;
          final SIARDExportOptionsCurrent instance = SIARDExportOptionsCurrent.getInstance(version, dbptkModule);
          if(path != null){
            instance.setDefaultPath(path);
          }
          content.clear();
          content.add(instance);
        });
        comboBoxField.setCSSMetadata("form-row", "form-label-spaced", "form-combobox");
        comboBoxField.select(1);

        listBox.add(comboBoxField);
      }
    });
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