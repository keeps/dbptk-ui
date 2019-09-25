package com.databasepreservation.main.desktop.client.dbptk.wizard.common.exportOptions;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.ViewerConstants;
import com.databasepreservation.main.common.shared.ViewerStructure.IsIndexed;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerMetadata;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.main.common.shared.client.common.desktop.GenericField;
import com.databasepreservation.main.common.shared.client.tools.ViewerStringUtils;
import com.databasepreservation.main.common.shared.models.DBPTKModule;
import com.databasepreservation.main.common.shared.models.PreservationParameter;
import com.databasepreservation.main.common.shared.models.wizardParameters.MetadataExportOptionsParameters;
import com.databasepreservation.main.desktop.client.dbptk.wizard.WizardPanel;
import com.databasepreservation.modules.siard.SIARD2ModuleFactory;
import com.github.nmorel.gwtjackson.client.ObjectMapper;
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

  interface DBPTKModuleMapper extends ObjectMapper<DBPTKModule> {
  }

  private static MetadataUiBinder binder = GWT.create(MetadataUiBinder.class);

  @UiField
  FlowPanel content;

  private static MetadataExportOptions instance = null;
  private HashMap<String, TextBox> textBoxInputs = new HashMap<>();
  private final boolean populate;
  private ViewerMetadata metadata = null;

  public static MetadataExportOptions getInstance(String moduleName, boolean populate) {
    if (instance == null) {
      instance = new MetadataExportOptions(moduleName, populate, null);
    }
    return instance;
  }

  public static MetadataExportOptions getInstance(String moduleName, boolean populate, String databaseUUID) {
    if (instance == null) {
      instance = new MetadataExportOptions(moduleName, populate, databaseUUID);
    }
    return instance;
  }

  private MetadataExportOptions(String moduleName, boolean populate, String databaseUUID) {
    initWidget(binder.createAndBindUi(this));
    this.populate = populate;

    Widget spinner = new HTML(SafeHtmlUtils.fromSafeConstant(
      "<div class='spinner'><div class='double-bounce1'></div><div class='double-bounce2'></div></div>"));

    content.add(spinner);
    if (populate) {
      BrowserService.Util.getInstance().retrieve(databaseUUID, ViewerDatabase.class.getName(), databaseUUID,
        new DefaultAsyncCallback<IsIndexed>() {
          @Override
          public void onSuccess(IsIndexed result) {
            ViewerDatabase database = (ViewerDatabase) result;
            metadata = database.getMetadata();
            obtainMetadataExportOptions(moduleName, spinner);
            }
          });
    } else {
      obtainMetadataExportOptions(moduleName, spinner);
    }
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
      case ViewerConstants.INPUT_TYPE_NUMBER:
      case ViewerConstants.INPUT_TYPE_TEXT:
        TextBox defaultTextBox = new TextBox();
        defaultTextBox.addStyleName("form-textbox");
        textBoxInputs.put(parameter.getName(), defaultTextBox);
        if (populate) {
          populate(parameter.getName(), defaultTextBox);
        }
        genericField = GenericField.createInstance(messages.wizardExportOptionsLabels(parameter.getName()), defaultTextBox);

        if (parameter.getName().equals(ViewerConstants.SIARD_METADATA_CLIENT_MACHINE)) {
          BrowserService.Util.getInstance().getClientMachine(new DefaultAsyncCallback<String>() {
            @Override
            public void onSuccess(String result) {
              defaultTextBox.setText(result);
            }
          });
        }
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

  public void populate(String parameterName, TextBox textBox) {
    switch (parameterName) {
      case SIARD2ModuleFactory.PARAMETER_META_DESCRIPTION:
        textBox.setText(metadata.getDescription());
        break;
      case SIARD2ModuleFactory.PARAMETER_META_ARCHIVER:
        textBox.setText(metadata.getArchiver());
        break;
      case SIARD2ModuleFactory.PARAMETER_META_ARCHIVER_CONTACT:
        textBox.setText(metadata.getArchiverContact());
        break;
      case SIARD2ModuleFactory.PARAMETER_META_DATA_OWNER:
        textBox.setText(metadata.getDataOwner());
        break;
      case SIARD2ModuleFactory.PARAMETER_META_DATA_ORIGIN_TIMESPAN:
        textBox.setText(metadata.getDataOriginTimespan());
        break;
    }
  }

  private void obtainMetadataExportOptions(final String moduleName, Widget spinner) {
    BrowserService.Util.getInstance().getSIARDExportModule(moduleName, new DefaultAsyncCallback<String>() {
      @Override
      public void onSuccess(String result) {
        DBPTKModuleMapper mapper = GWT.create(DBPTKModuleMapper.class);
        DBPTKModule module = mapper.read(result);

        for (PreservationParameter p : module.getParameters(moduleName)) {
          if (p.getExportOption() != null && p.getExportOption().equals(ViewerConstants.METADATA_EXPORT_OPTIONS)) {
            buildGenericWidget(p);
          }
        }
        content.remove(spinner);
      }
    });
  }
}