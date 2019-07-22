package com.databasepreservation.main.desktop.client.dbptk.wizard.create.exportOptions;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.main.common.shared.client.tools.ToolkitModuleName2ViewerModuleName;
import com.databasepreservation.main.desktop.client.common.ComboBoxField;
import com.databasepreservation.main.desktop.client.common.FileUploadField;
import com.databasepreservation.main.desktop.client.dbptk.wizard.WizardPanel;
import com.databasepreservation.main.desktop.shared.models.DBPTKModule;
import com.databasepreservation.main.desktop.shared.models.PreservationParameter;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.ExportOptionsParameters;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class SIARDExportOptions extends WizardPanel<ExportOptionsParameters> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface SIARDUiBinder extends UiBinder<Widget, SIARDExportOptions> {
  }

  private static SIARDUiBinder binder = GWT.create(SIARDUiBinder.class);

  @UiField
  FlowPanel content;

  @UiField
  SimplePanel listBox;

  private static SIARDExportOptions instance = null;
  private String version;
  private DBPTKModule dbptkModule;

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
        content.remove(spinner);

        ComboBoxField comboBoxField = ComboBoxField.createInstance(messages.exportOptionsLabels("version"));
        final TreeMap<String, ArrayList<PreservationParameter>> modules = new TreeMap<>(result.getParameters());
        for (String moduleName : modules.keySet()) {
          comboBoxField.setComboBoxValue(ToolkitModuleName2ViewerModuleName.transform(moduleName), moduleName);
        }
        comboBoxField.addChangeHandler(() -> {
          version = comboBoxField.getComboBoxValue();
          dbptkModule = result;
          final SIARDExportOptionsCurrent instance = SIARDExportOptionsCurrent.getInstance(version, dbptkModule);
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

    instance = null;
  }

  @Override
  public boolean validate() {
    return SIARDExportOptionsCurrent.getInstance(version, dbptkModule).validate();
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