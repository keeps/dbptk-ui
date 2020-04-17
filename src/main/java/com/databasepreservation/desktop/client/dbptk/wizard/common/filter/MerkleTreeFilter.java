package com.databasepreservation.desktop.client.dbptk.wizard.common.filter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.fields.ComboBoxField;
import com.databasepreservation.common.client.common.fields.FileUploadField;
import com.databasepreservation.common.client.common.fields.GenericField;
import com.databasepreservation.common.client.common.utils.ApplicationType;
import com.databasepreservation.common.client.common.utils.JavascriptUtils;
import com.databasepreservation.common.client.models.JSO.ExtensionFilter;
import com.databasepreservation.common.client.models.dbptk.Module;
import com.databasepreservation.common.client.models.parameters.PreservationParameter;
import com.databasepreservation.common.client.models.wizard.filter.MerkleTreeFilterParameters;
import com.databasepreservation.common.client.services.MigrationService;
import com.databasepreservation.common.client.tools.JSOUtils;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.common.client.widgets.Toast;
import com.databasepreservation.desktop.client.dbptk.wizard.WizardPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class MerkleTreeFilter extends WizardPanel<MerkleTreeFilterParameters> {
  @UiField
  ClientMessages messages = GWT.create(ClientMessages.class);

  interface MetadataUiBinder extends UiBinder<Widget, MerkleTreeFilter> {
  }

  private static final MetadataUiBinder binder = GWT.create(MetadataUiBinder.class);

  @UiField
  FlowPanel content;

  @UiField
  HTML description;

  private static MerkleTreeFilter instance = null;
  private final Map<String, TextBox> textBoxInputs = new HashMap<>();
  private final Map<String, ComboBoxField> comboBoxInputs = new HashMap<>();
  private final Map<String, CheckBox> checkBoxInputs = new HashMap<>();
  private final Map<String, String> fileInputs = new HashMap<>();
  private Module module;
  private final Button btnNext;
  private boolean skipFilter = true;

  public static MerkleTreeFilter getInstance(Button btnNext) {
    if (instance == null) {
      instance = new MerkleTreeFilter(btnNext);
    }
    return instance;
  }

  private MerkleTreeFilter(Button btnNext) {
    initWidget(binder.createAndBindUi(this));
    this.btnNext = btnNext;

    Widget spinner = new HTML(SafeHtmlUtils.fromSafeConstant(
      "<div class='spinner'><div class='double-bounce1'></div><div class='double-bounce2'></div></div>"));

    content.add(spinner);

    description.setHTML(messages.wizardMerkleTreeFilterDescription(
      "https://github.com/keeps/db-preservation-toolkit/wiki/Merkle-Tree-Filter-Module"));

    MigrationService.Util.call((List<Module> result) -> {
      content.remove(spinner);
      module = result.get(0);

      CheckBox checkbox = new CheckBox();
      checkbox.setText("Apply Merkle Tree Filter");
      checkbox.addStyleName("form-checkbox");
      checkbox.addValueChangeHandler(e -> {
        if (e.getValue()) {
          skipFilter = false;
          btnNext.setText(messages.basicActionNext());
        } else {
          skipFilter = true;
          btnNext.setText(messages.basicActionSkip());
        }
      });
      GenericField genericField = GenericField.createInstance(checkbox);
      genericField.setCSSMetadata("form-row", "form-label-spaced");
      content.add(genericField);

      for (PreservationParameter p : module.getParameters()) {
        buildGenericWidget(p);
      }

    }).getFilterModules("merkle-tree");
  }

  @Override
  public void clear() {
    instance = null;
  }

  @Override
  public boolean validate() {
    if (!skipFilter) {
      final List<PreservationParameter> requiredParameters = module.getRequiredParameters();

      for (PreservationParameter parameter : requiredParameters) {
        if (ViewerConstants.INPUT_TYPE_FILE_SAVE.equals(parameter.getInputType())) {
          if (fileInputs.get(parameter.getName()) != null) {
            final String s = fileInputs.get(parameter.getName());
            return !ViewerStringUtils.isBlank(s);
          } else {
            return false;
          }
        }
      }
    }

    return true;
  }

  @Override
  public MerkleTreeFilterParameters getValues() {
    MerkleTreeFilterParameters merkleTreeFilterParameters = new MerkleTreeFilterParameters();

    if (!skipFilter) {
      for (PreservationParameter parameter : module.getParameters()) {
        switch (parameter.getInputType()) {
          case ViewerConstants.INPUT_TYPE_CHECKBOX:
            if (checkBoxInputs.get(parameter.getName()) != null) {
              final boolean value = checkBoxInputs.get(parameter.getName()).getValue();
              merkleTreeFilterParameters.getValues().put(parameter.getName(), String.valueOf(value));
            }
            break;
          case ViewerConstants.INPUT_TYPE_TEXT:
            if (textBoxInputs.get(parameter.getName()) != null) {
              final String text = textBoxInputs.get(parameter.getName()).getText();
              merkleTreeFilterParameters.getValues().put(parameter.getName(), text);
            }
            break;
          case ViewerConstants.INPUT_TYPE_FOLDER:
          case ViewerConstants.INPUT_TYPE_FILE_OPEN:
          case ViewerConstants.INPUT_TYPE_FILE_SAVE:
            if (fileInputs.get(parameter.getName()) != null) {
              final String path = fileInputs.get(parameter.getName());
              merkleTreeFilterParameters.getValues().put(parameter.getName(), path);
            }
            break;
          case ViewerConstants.INPUT_TYPE_COMBOBOX:
            if (comboBoxInputs.get(parameter.getName()) != null) {
              merkleTreeFilterParameters.getValues().put(parameter.getName(),
                comboBoxInputs.get(parameter.getName()).getSelectedValue());
            }
            break;
          case ViewerConstants.INPUT_TYPE_DEFAULT:
          case ViewerConstants.INPUT_TYPE_NONE:
          default:
            break;
        }
      }
    }

    return merkleTreeFilterParameters;
  }

  @Override
  public void error() {
    Toast.showError(messages.wizardMerkleTreeFilterTitle(), messages.wizardMerkleTreeFilterErrorMessages());
  }

  public void changeBtnNextText() {
    if (skipFilter) {
      btnNext.setText(messages.basicActionSkip());
    } else {
      btnNext.setText(messages.basicActionNext());
    }
  }

  private void buildGenericWidget(PreservationParameter parameter) {

    GenericField genericField = null;
    String spanCSSClass = "form-text-helper text-muted";

    switch (parameter.getInputType()) {
      case ViewerConstants.INPUT_TYPE_COMBOBOX:
        ComboBoxField comboBoxField = ComboBoxField
          .createInstance(messages.wizardExportOptionsLabels(parameter.getName()));
        parameter.getPossibleValues()
          .forEach(key -> comboBoxField.setComboBoxValue(messages.wizardExportOptionsForPossibleValues(key), key));
        comboBoxField.setCSSMetadata("form-row", "form-label-spaced", "form-combobox");
        comboBoxField.select(parameter.getDefaultIndex());
        comboBoxInputs.put(parameter.getName(), comboBoxField);
        genericField = GenericField.createInstance(comboBoxField);
        break;

      case ViewerConstants.INPUT_TYPE_CHECKBOX:
        CheckBox checkbox = new CheckBox();
        checkbox.setText(messages.wizardExportOptionsLabels(parameter.getName()));
        checkbox.addStyleName("form-checkbox");
        checkBoxInputs.put(parameter.getName(), checkbox);
        genericField = GenericField.createInstance(checkbox);
        spanCSSClass = "form-text-helper-checkbox text-muted";
        break;
      case ViewerConstants.INPUT_TYPE_FILE_SAVE:
        FileUploadField fileUploadField = FileUploadField
          .createInstance(messages.wizardExportOptionsLabels(parameter.getName()), messages.basicActionBrowse());
        fileUploadField.setParentCSS("form-row");
        fileUploadField.setLabelCSS("form-label-spaced");
        fileUploadField.setButtonCSS("btn btn-link form-button");
        fileUploadField.setRequired(parameter.isRequired());
        fileUploadField.buttonAction(() -> {
          if (ApplicationType.getType().equals(ViewerConstants.APPLICATION_ENV_DESKTOP)) {
            ExtensionFilter extensionFilter = new ExtensionFilter()
              .createFilterTypeFromDBPTK(parameter.getFileFilter());
            JavaScriptObject options = JSOUtils.getOpenDialogOptions(Collections.emptyList(),
              Collections.singletonList(extensionFilter));
            String path = null;
            if (parameter.getInputType().equals(ViewerConstants.INPUT_TYPE_FILE_SAVE)) {
              path = JavascriptUtils.saveFileDialog(options);
            }
            if (path != null) {
              fileInputs.put(parameter.getName(), path);
              fileUploadField.setPathLocation(path, path);
              fileUploadField.setInformationPathCSS("gwt-Label-disabled information-path");
            }
          }
        });
        FlowPanel helper = new FlowPanel();
        helper.addStyleName("form-helper");
        InlineHTML span = new InlineHTML();
        span.addStyleName("form-text-helper text-muted");
        span.setText(parameter.getDescription());
        helper.add(fileUploadField);
        helper.add(span);
        content.add(helper);
        break;
      case ViewerConstants.INPUT_TYPE_FILE_OPEN:
      case ViewerConstants.INPUT_TYPE_FOLDER:
      case ViewerConstants.INPUT_TYPE_NONE:
        genericField = null;
        break;
      case ViewerConstants.INPUT_TYPE_NUMBER:
      case ViewerConstants.INPUT_TYPE_TEXT:
      default:
        TextBox defaultTextBox = new TextBox();
        defaultTextBox.addStyleName("form-textbox");
        textBoxInputs.put(parameter.getName(), defaultTextBox);
        genericField = GenericField.createInstance(messages.wizardExportOptionsLabels(parameter.getName()),
          defaultTextBox);
        break;
    }

    if (genericField != null) {
      FlowPanel helper = new FlowPanel();
      helper.addStyleName("form-helper");
      InlineHTML span = new InlineHTML();
      span.addStyleName(spanCSSClass);
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