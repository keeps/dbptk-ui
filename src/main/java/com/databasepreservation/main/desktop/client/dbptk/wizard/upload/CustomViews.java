package com.databasepreservation.main.desktop.client.dbptk.wizard.upload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.main.common.shared.client.common.dialogs.Dialogs;
import com.databasepreservation.main.common.shared.client.tools.HistoryManager;
import com.databasepreservation.main.common.shared.client.tools.ViewerStringUtils;
import com.databasepreservation.main.common.shared.client.widgets.Toast;
import com.databasepreservation.main.desktop.client.common.ComboBoxField;
import com.databasepreservation.main.desktop.client.common.sidebar.CustomViewsSidebar;
import com.databasepreservation.main.desktop.client.dbptk.wizard.WizardPanel;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.ConnectionParameters;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.CustomViewsParameter;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.CustomViewsParameters;
import com.google.gwt.core.client.GWT;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class CustomViews extends WizardPanel<CustomViewsParameters> {
  @UiField
  public ClientMessages messages = GWT.create(ClientMessages.class);

  interface CustomViewsUiBinder extends UiBinder<Widget, CustomViews> {
  }

  private static CustomViewsUiBinder binder = GWT.create(CustomViewsUiBinder.class);

  @UiField
  FlowPanel customViewsList, rightSideContainer, schemasCombobox, customViewsButtons;

  @UiField
  TextBox customViewName, customViewDescription;

  @UiField
  TextArea customViewQuery;

  @UiField
  Label customViewNameLabel, customViewDescriptionLabel, customViewQueryLabel;

  private static CustomViews instance = null;
  private CustomViewsSidebar customViewsSidebar;
  private HashMap<String, CustomViewsParameter> customViewsParameters = new HashMap<>();
  private int counter = 0;
  private boolean toSave;
  private ComboBoxField customViewSchemaName;
  private Button btnNext;
  private ConnectionParameters connectionParameters;
  private String databaseUUID;
  private boolean queryResult;

  public static CustomViews getInstance(List<String> schemas, Button btnNext, ConnectionParameters connectionParameters,
    String databaseUUID) {
    if (instance == null) {
      instance = new CustomViews(schemas, btnNext, connectionParameters, databaseUUID);
    }
    return instance;
  }

  private CustomViews(List<String> schemas, Button btnNext, ConnectionParameters connectionParameters,
    String databaseUUID) {
    initWidget(binder.createAndBindUi(this));

    this.btnNext = btnNext;
    this.connectionParameters = connectionParameters;
    this.databaseUUID = databaseUUID;
    customViewsSidebar = CustomViewsSidebar.getInstance();
    customViewsList.add(customViewsSidebar);

    customViewSchemaName = ComboBoxField.createInstance(messages.customViewSchemaNameLabel(), schemas);
    customViewSchemaName.setCSSMetadata("form-row","form-label-spaced", "form-combobox");
    customViewSchemaName.setRequired();

    schemasCombobox.add(customViewSchemaName);

    setRequired(customViewNameLabel, true);
    setRequired(customViewDescriptionLabel, false);
    setRequired(customViewQueryLabel, true);

    customViewsButtons.add(createCustomViewButton());
  }

  void checkIfHaveCustomViews(){
    if(customViewsParameters.isEmpty()){
      btnNext.setText(messages.basicActionSkip());
    } else {
      btnNext.setText(messages.basicActionNext());
    }
  }

  @Override
  public void clear() {
    if (customViewsParameters != null && !customViewsParameters.isEmpty()) {
      customViewsParameters.clear();
    }
    customViewsParameters = new HashMap<>();
    if (customViewsSidebar != null) {
      customViewsSidebar.selectNone();
      customViewsSidebar.clear();
      customViewsSidebar = null;
    }
    instance = null;
  }

  @Override
  public boolean validate() {
    boolean empty = ViewerStringUtils.isBlank(customViewName.getText()) && ViewerStringUtils.isBlank(customViewQuery.getText());

    if (empty) {
      toSave = false;
      return true;
    } else {
      toSave = true;
    }

    CustomViewsParameter parameter = new CustomViewsParameter(customViewSchemaName.getSelectedValue(), counter,
        customViewName.getText(),
        customViewDescription.getText(), customViewQuery.getText());

    toSave = !customViewsParameters.containsValue(parameter);

    return customViewsParameters.containsValue(parameter);
  }

  @Override
  public CustomViewsParameters getValues() {
    CustomViewsParameters customViewsParameters = new CustomViewsParameters();
    ArrayList<CustomViewsParameter> parameters = new ArrayList<>(this.customViewsParameters.values());
    if (toSave) {
      CustomViewsParameter parameter = new CustomViewsParameter(customViewSchemaName.getSelectedValue(), counter,
        customViewName.getText(), customViewDescription.getText(), customViewQuery.getText());
      parameters.add(parameter);
    }
    customViewsParameters.setCustomViewsParameter(parameters);

    return customViewsParameters;
  }

  @Override
  public void error() { }

  public void sideBarHighlighter(String customViewUUID, String action) {

    final CustomViewsParameter parameter = customViewsParameters.get(customViewUUID);

    if (action != null && action.equals(HistoryManager.ACTION_DELETE)) {
      Dialogs.showConfirmDialog(messages.customViewsTitle(), messages.customViewsDialogConfirmDelete(), messages.basicActionCancel(), messages.basicActionConfirm(),
        new DefaultAsyncCallback<Boolean>() {
          @Override
          public void onSuccess(Boolean result) {
            if (result) {
              deleteCustomView(parameter.getCustomViewID());
              setTextboxText("", "", "", "");
              customViewName.getElement().removeAttribute("required");
              customViewQuery.getElement().removeAttribute("required");
              customViewSchemaName.getElement().removeAttribute("required");
              customViewsButtons.clear();
              customViewsButtons.add(createCustomViewButton());
              customViewsSidebar.selectNone();
              HistoryManager.gotoCreateSIARD();
            } else {
              HistoryManager.gotoCreateSIARD();
            }
          }
        });
    } else {
      customViewsButtons.clear();
      setTextboxText(parameter.getSchema(), parameter.getCustomViewName(), parameter.getCustomViewDescription(),
          parameter.getCustomViewQuery());

      Button btnNew = new Button();
      btnNew.setText(messages.customViewsBtnNew());
      btnNew.addStyleName("btn btn-primary btn-new");
      btnNew.addClickHandler(event -> {
        setTextboxText("", "", "", "");
        customViewName.getElement().removeAttribute("required");
        customViewQuery.getElement().removeAttribute("required");
        customViewSchemaName.getElement().removeAttribute("required");
        customViewsSidebar.selectNone();
        customViewsButtons.clear();
        customViewsButtons.add(createCustomViewButton());
        HistoryManager.gotoCreateSIARD();
      });

      Button btnTest = new Button();
      btnTest.setText(messages.customViewsBtnTest());
      btnTest.addStyleName("btn btn-primary btn-run");

      btnTest.addClickHandler(event -> {
        if (validateCustomViewQueryText()) {
          BrowserService.Util.getInstance().validateCustomViewQuery(databaseUUID, connectionParameters,
            customViewQuery.getText(), new DefaultAsyncCallback<List<List<String>>>() {
              @Override
              public void onSuccess(List<List<String>> result) {
                Dialogs.showQueryResult(messages.customViewsQueryResultsDialogTitle(), messages.basicActionClose(),
                  result);
              }

              @Override
              public void onFailure(Throwable caught) {
                Toast.showError(messages.customViewToastErrorTitle(), caught.getMessage());
              }
            });
        } else {
          Toast.showError(messages.customViewsTestQueryError());
        }
      });

      Button btnUpdate = new Button();
      btnUpdate.setText(messages.customViewsBtnSave());
      btnUpdate.addStyleName("btn btn-primary btn-save");
      btnUpdate.addClickHandler(event -> {
        final int valid = customViewFormValidatorUpdate(parameter.getCustomViewName());
        if (valid == -1) {
          BrowserService.Util.getInstance().validateCustomViewQuery(databaseUUID, connectionParameters,
            customViewQuery.getText(), new DefaultAsyncCallback<List<List<String>>>() {
              @Override
              public void onSuccess(List<List<String>> result) {
                if (!result.isEmpty()) {
                  updateCustomViewParameters(parameter.getCustomViewID(), customViewSchemaName.getSelectedValue(),
                    customViewName.getText(), customViewDescription.getText(), customViewQuery.getText());
                  Toast.showInfo(messages.customViewsTitle(), messages.customViewsUpdateMessage());
                } else {
                  Toast.showError("Empty");
                }
              }

              @Override
              public void onFailure(Throwable caught) {
                Toast.showError(messages.customViewToastErrorTitle(), caught.getMessage());
              }
            });
        } else {
          Toast.showError(messages.errorMessagesCustomViewsTitle(), messages.errorMessagesCustomViews(valid));
          highlightFieldsWhenRequired();
        }
      });

      SimplePanel simplePanelForUpdateButton = new SimplePanel();
      simplePanelForUpdateButton.addStyleName("btn-item");
      simplePanelForUpdateButton.add(btnUpdate);
      customViewsButtons.add(simplePanelForUpdateButton);

      SimplePanel simplePanelForTestButton = new SimplePanel();
      simplePanelForTestButton.addStyleName("btn-item");
      simplePanelForTestButton.add(btnTest);
      customViewsButtons.add(simplePanelForTestButton);

      SimplePanel simplePanelForNewButton = new SimplePanel();
      simplePanelForNewButton.addStyleName("btn-item");
      simplePanelForNewButton.add(btnNew);
      customViewsButtons.add(simplePanelForNewButton);

      customViewsSidebar.select(customViewUUID);
    }
  }

  void refreshCustomButtons() {
    customViewsButtons.clear();
    customViewsButtons.add(createCustomViewButton());
    checkIfHaveCustomViews();
  }

  private int customViewFormValidator() {
    String viewNameText = customViewName.getText();
    String viewQueryText = customViewQuery.getText();

    boolean value = ViewerStringUtils.isBlank(viewNameText) || ViewerStringUtils.isBlank(viewQueryText);

    if (value)
      return 1;

    boolean sameName = false;
    for (CustomViewsParameter p : customViewsParameters.values()) {
      if (p.getCustomViewName().toLowerCase().equals(viewNameText.toLowerCase())) {
        sameName = true;
      }
    }

    if (sameName)
      return 2;

    return -1;
  }

  private int customViewFormValidatorUpdate(final String customViewName) {
    String viewNameText = this.customViewName.getText();
    String viewQueryText = this.customViewQuery.getText();

    boolean value = ViewerStringUtils.isBlank(viewNameText) || ViewerStringUtils.isBlank(viewQueryText);

    if (value)
      return 1;

    boolean sameName = false;
    for (CustomViewsParameter p : customViewsParameters.values()) {
      if (p.getCustomViewName().toLowerCase().equals(viewNameText.toLowerCase())
        && !p.getCustomViewName().toLowerCase().equals(customViewName.toLowerCase())) {
        sameName = true;
      }
    }

    if (sameName)
      return 2;

    return -1;
  }

  private void setRequired(Widget label, boolean required) {
    if (required)
      label.addStyleName("form-label-mandatory");
    else
      label.removeStyleName("form-label-mandatory");
  }

  private void updateCustomViewParameters(final int id, final String customViewSchemaName,
    final String customViewNameText,
    final String customViewDescriptionText, final String customViewQueryText) {
    String customViewUUID = String.valueOf(id);
    final CustomViewsParameter parameter = customViewsParameters.get(customViewUUID);
    parameter.setSchemaName(customViewSchemaName);
    parameter.setCustomViewName(customViewNameText);
    parameter.setCustomViewDescription(customViewDescriptionText);
    parameter.setCustomViewQuery(customViewQueryText);

    customViewsParameters.put(customViewUUID, parameter);
    customViewsSidebar.updateSidarHyperLink(customViewUUID, customViewNameText);
  }

  private void deleteCustomView(Integer id) {
    String customViewUUID = String.valueOf(id);
    customViewsParameters.remove(customViewUUID);
    customViewsSidebar.removeSideBarHyperLink(customViewUUID);
    checkIfHaveCustomViews();
  }

  private void setTextboxText(final String schemaName, final String customViewNameText,
    final String customViewDescriptionText,
    final String customViewQueryText) {
    customViewName.setText(customViewNameText);
    customViewDescription.setText(customViewDescriptionText);
    customViewQuery.setText(customViewQueryText);
  }

  private FlowPanel createCustomViewButton() {
    Button btnSave = new Button();
    btnSave.setText(messages.customViewsBtnSave());
    btnSave.addStyleName("btn btn-primary btn-save");

    btnSave.addClickHandler(event -> {
      final int valid = customViewFormValidator();
      if (valid == -1) {
        BrowserService.Util.getInstance().validateCustomViewQuery(databaseUUID, connectionParameters,
          customViewQuery.getText(), new DefaultAsyncCallback<List<List<String>>>() {
            @Override
            public void onSuccess(List<List<String>> result) {
              if (!result.isEmpty()) {
                customViewsSidebar.addSideBarHyperLink(customViewName.getText(), String.valueOf(counter),
                  HistoryManager.linkToCreateWizardCustomViewsDelete(String.valueOf(counter)));

                CustomViewsParameter parameter = new CustomViewsParameter(customViewSchemaName.getSelectedValue(),
                  counter, customViewName.getText(), customViewDescription.getText(), customViewQuery.getText());
                customViewsParameters.put(String.valueOf(counter), parameter);
                counter++;
                setTextboxText("", "", "", "");
                customViewName.getElement().removeAttribute("required");
                customViewQuery.getElement().removeAttribute("required");
                customViewSchemaName.getElement().removeAttribute("required");
                customViewsSidebar.selectNone();
              } else {
                Toast.showError("Empty");
              }
              checkIfHaveCustomViews();
            }

            @Override
            public void onFailure(Throwable caught) {
              Toast.showError(messages.customViewToastErrorTitle(), caught.getMessage());
              checkIfHaveCustomViews();
            }
          });
      } else {
        Toast.showError(messages.errorMessagesCustomViewsTitle(), messages.errorMessagesCustomViews(valid));
        highlightFieldsWhenRequired();
        checkIfHaveCustomViews();
      }
    });

    Button btnTest = new Button();
    btnTest.setText(messages.customViewsBtnTest());
    btnTest.addStyleName("btn btn-primary btn-run");

    btnTest.addClickHandler(event -> {
      if (validateCustomViewQueryText()) {
        BrowserService.Util.getInstance().validateCustomViewQuery(databaseUUID, connectionParameters,
          customViewQuery.getText(), new DefaultAsyncCallback<List<List<String>>>() {
            @Override
            public void onSuccess(List<List<String>> result) {
              Dialogs.showQueryResult(messages.customViewsQueryResultsDialogTitle(), messages.basicActionClose(),
                result);
            }

            @Override
            public void onFailure(Throwable caught) {
              Toast.showError(messages.customViewToastErrorTitle(), caught.getMessage());
            }
          });
      } else {
        Toast.showError(messages.customViewsTestQueryError());
      }
    });

    FlowPanel FlowPanelForSaveButton = new FlowPanel();
    FlowPanelForSaveButton.addStyleName("btn-item");
    FlowPanelForSaveButton.add(btnSave);

    FlowPanel FlowPanelForTestButton = new FlowPanel();
    FlowPanelForTestButton.addStyleName("btn-item");
    FlowPanelForTestButton.add(btnTest);

    FlowPanel FlowPanelForOptionsButtons = new FlowPanel();
    FlowPanelForOptionsButtons.add(FlowPanelForSaveButton);
    FlowPanelForOptionsButtons.add(FlowPanelForTestButton);

    return FlowPanelForOptionsButtons;
  }

  private void highlightFieldsWhenRequired() {
    if (ViewerStringUtils.isBlank(customViewName.getText())) {
      customViewName.getElement().setAttribute("required", "required");
      customViewName.addStyleName("wizard-connection-validator");
    }

    if (ViewerStringUtils.isBlank(customViewQuery.getText())) {
      customViewQuery.getElement().setAttribute("required", "required");
      customViewQuery.addStyleName("wizard-connection-validator");
    }

    if (ViewerStringUtils.isBlank(customViewSchemaName.getSelectedValue())) {
      customViewSchemaName.getElement().setAttribute("required", "required");
      customViewSchemaName.addStyleName("wizard-connection-validator");
    }
  }

  private boolean validateCustomViewQueryText() {
    if (ViewerStringUtils.isBlank(customViewQuery.getText())) {
      return false;
    }

    final String query = customViewQuery.getText();

    final RegExp compile = RegExp.compile("^(:>\\s+)?SELECT", "i");

    return compile.test(query);
  }

  public CustomViewsParameter getCustomViewParameter() {
    return new CustomViewsParameter(customViewSchemaName.getSelectedValue(), counter, customViewName.getText(),
      customViewDescription.getText(), customViewQuery.getText());
  }
}
