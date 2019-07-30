package com.databasepreservation.main.desktop.client.dbptk.wizard.create.exportOptions;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.ViewerConstants;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.main.common.shared.client.tools.ViewerStringUtils;
import com.databasepreservation.main.desktop.client.common.GenericField;
import com.databasepreservation.main.desktop.client.dbptk.wizard.WizardPanel;
import com.databasepreservation.main.desktop.shared.models.DBPTKModule;
import com.databasepreservation.main.desktop.shared.models.PreservationParameter;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.MetadataExportOptionsParameters;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class MetadataExportOptions extends WizardPanel<MetadataExportOptionsParameters> {
  @UiField
  ClientMessages messages = GWT.create(ClientMessages.class);

  interface MetadataUiBinder extends UiBinder<Widget, MetadataExportOptions> {
  }

  private static MetadataUiBinder binder = GWT.create(MetadataUiBinder.class);

  @UiField
  FlowPanel content;

  private static MetadataExportOptions instance = null;
  private HashMap<String, TextBox> textBoxInputs = new HashMap<>();

  public static MetadataExportOptions getInstance(String moduleName) {
    if (instance == null) {
      instance = new MetadataExportOptions(moduleName);
    }
    return instance;
  }

  private MetadataExportOptions(String moduleName) {
    initWidget(binder.createAndBindUi(this));

    Widget spinner = new HTML(SafeHtmlUtils.fromSafeConstant(
      "<div class='spinner'><div class='double-bounce1'></div><div class='double-bounce2'></div></div>"));

    content.add(spinner);

    BrowserService.Util.getInstance().getSIARDExportModule(moduleName, new DefaultAsyncCallback<DBPTKModule>() {
      @Override
      public void onSuccess(DBPTKModule result) {
        content.remove(spinner);

        for (PreservationParameter p : result.getParameters(moduleName)) {
          if (p.getExportOption() != null && p.getExportOption().equals(ViewerConstants.METADATA_EXPORT_OPTIONS)) {
            buildGenericWidget(p);
          }
        }
      }
    });
  }

  @Override
  public void clear() {
    instance = null;
  }

  @Override
  public boolean validate() {
    return true;
  }

  @Override
  public MetadataExportOptionsParameters getValues() {
    MetadataExportOptionsParameters metadataExportOptionsParameters = new MetadataExportOptionsParameters();
    HashMap<String, String> metadataValues = new HashMap<>();
    if (validate()) {
      for (Map.Entry<String, TextBox> entry : textBoxInputs.entrySet()) {
        if (ViewerStringUtils.isNotBlank(entry.getValue().getText())) {
          metadataValues.put(entry.getKey(), entry.getValue().getText());
        } else {
          metadataValues.put(entry.getKey(), "unspecified");
        }
      }
      metadataExportOptionsParameters.setValues(metadataValues);
    }

    return metadataExportOptionsParameters;
  }

  @Override
  public void error() {

  }

  private void buildGenericWidget(PreservationParameter parameter) {

    GenericField genericField = null;

    switch (parameter.getInputType()) {
      case "NUMBER":
      case "TEXT":
        TextBox defaultTextBox = new TextBox();
        defaultTextBox.addStyleName("form-textbox");
        textBoxInputs.put(parameter.getName(), defaultTextBox);
        genericField = GenericField.createInstance(messages.wizardExportOptionsLabels(parameter.getName()), defaultTextBox);
        break;
      default:
        break;
    }

    if (genericField != null) {
      FlowPanel helper = new FlowPanel();
      helper.addStyleName("form-helper");
      InlineHTML span = new InlineHTML();
      span.addStyleName("form-text-helper text-muted");
      span.setText(messages.wizardExportOptionsHelperText(parameter.getName()));

      genericField.setRequired(parameter.isRequired());
      genericField.setCSSMetadata("form-row", "form-label-spaced");
      genericField.addHelperText(span);
      helper.add(genericField);
      helper.add(span);
      content.add(helper);
    }
  }
}