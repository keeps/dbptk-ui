package com.databasepreservation.main.desktop.client.dbptk.wizard.create;

import java.util.ArrayList;
import java.util.HashMap;

import com.databasepreservation.main.common.shared.client.tools.HistoryManager;
import com.databasepreservation.main.common.shared.client.tools.ViewerStringUtils;
import com.databasepreservation.main.common.shared.client.widgets.Toast;
import com.databasepreservation.main.desktop.client.common.sidebar.CustomViewsSidebar;
import com.databasepreservation.main.desktop.client.dbptk.wizard.WizardPanel;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.CustomViewsParameter;
import com.databasepreservation.main.desktop.shared.models.wizardParameters.CustomViewsParameters;
import com.google.gwt.core.client.GWT;
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
  FlowPanel customViewsList, rightSideContainer;

  @UiField
  TextBox customViewSchemaName, customViewName, customViewDescription;

  @UiField
  TextArea customViewQuery;

  @UiField
  Label customViewSchemaNameLabel, customViewNameLabel, customViewDescriptionLabel, customViewQueryLabel;

  private static CustomViews instance = null;
  private CustomViewsSidebar customViewsSidebar;
  private HashMap<String, CustomViewsParameter> customViewsParameters = new HashMap<>();
  private int counter = 0;
  private FlowPanel customViewsButtons;
  private boolean toSave;

  public static CustomViews getInstance(FlowPanel customViewButtons) {
    if (instance == null) {
      instance = new CustomViews(customViewButtons);
    }
    return instance;
  }

  private CustomViews(FlowPanel customViewsButtons) {
    initWidget(binder.createAndBindUi(this));

    this.customViewsButtons = customViewsButtons;
    customViewsSidebar = CustomViewsSidebar.getInstance();
    customViewsList.add(customViewsSidebar);

    setRequired(customViewSchemaNameLabel, true);
    setRequired(customViewNameLabel, true);
    setRequired(customViewDescriptionLabel, false);
    setRequired(customViewQueryLabel, true);

    this.customViewsButtons.add(createCustomViewButton());
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
    boolean empty = ViewerStringUtils.isBlank(customViewName.getText()) && ViewerStringUtils.isBlank(customViewQuery.getText())
        && ViewerStringUtils.isBlank(customViewSchemaName.getText());

    if (empty) {
      toSave = false;
      return true;
    } else {
      toSave = true;
    }

    CustomViewsParameter parameter = new CustomViewsParameter(customViewSchemaName.getText(), counter,
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
      CustomViewsParameter parameter = new CustomViewsParameter(customViewSchemaName.getText(), counter,
        customViewName.getText(), customViewDescription.getText(), customViewQuery.getText());
      parameters.add(parameter);
    }
    customViewsParameters.setCustomViewsParameter(parameters);

    return customViewsParameters;
  }

  @Override
  public void error() { }

  public void sideBarHighlighter(String customViewUUID, String action) {
    customViewsButtons.clear();

    final CustomViewsParameter parameter = customViewsParameters.get(customViewUUID);

    if (action != null && action.equals(HistoryManager.ACTION_DELETE)) {
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
      Button btnUpdate = new Button();
      btnUpdate.setText(messages.customViewsBtnSave());
      btnUpdate.addStyleName("btn btn-primary btn-save");
      btnUpdate.addClickHandler(event -> {
        final boolean valid = customViewFormValidatorUpdate(parameter.getCustomViewName());
        if (valid) {
          updateCustomViewParameters(parameter.getCustomViewID(), customViewSchemaName.getText(),
              customViewName.getText(),
              customViewDescription.getText(), customViewQuery.getText());
          Toast.showInfo(messages.customViewsTitle(), messages.customViewsUpdateMessage());
        } else {
          highlightFieldsWhenRequired();
        }
      });

      SimplePanel simplePanelforbtnNew = new SimplePanel();
      simplePanelforbtnNew.addStyleName("btn-item");
      simplePanelforbtnNew.add(btnNew);

      SimplePanel simplePanelforbtnUpdate = new SimplePanel();
      simplePanelforbtnUpdate.addStyleName("btn-item");
      simplePanelforbtnUpdate.add(btnUpdate);

      customViewsButtons.add(simplePanelforbtnUpdate);
      customViewsButtons.add(simplePanelforbtnNew);

      customViewsSidebar.select(customViewUUID);
    }
  }

  void refreshCustomButtons() {
    customViewsButtons.clear();
    customViewsButtons.add(createCustomViewButton());
  }

  private boolean customViewFormValidator() {
    String viewSchemaName = customViewSchemaName.getText();
    String viewNameText = customViewName.getText();
    String viewQueryText = customViewQuery.getText();

    boolean value = !(ViewerStringUtils.isBlank(viewNameText) || ViewerStringUtils.isBlank(viewQueryText)
      || ViewerStringUtils.isBlank(viewSchemaName));

    boolean sameName = false;
    for (CustomViewsParameter p : customViewsParameters.values()) {
      if (p.getCustomViewName().toLowerCase().equals(viewNameText.toLowerCase())) {
        sameName = true;
      }
    }

    return value && !sameName;
  }

  private boolean customViewFormValidatorUpdate(final String customViewName) {
    String viewSchemaName = customViewSchemaName.getText();
    String viewNameText = this.customViewName.getText();
    String viewQueryText = this.customViewQuery.getText();

    boolean value = !(ViewerStringUtils.isBlank(viewNameText) || ViewerStringUtils.isBlank(viewQueryText)
      || ViewerStringUtils.isBlank(viewSchemaName));

    boolean sameName = false;
    for (CustomViewsParameter p : customViewsParameters.values()) {
      if (p.getCustomViewName().toLowerCase().equals(viewNameText.toLowerCase())
        && !p.getCustomViewName().toLowerCase().equals(customViewName.toLowerCase())) {
        sameName = true;
      }
    }

    return value && !sameName;
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
  }

  private void setTextboxText(final String schemaName, final String customViewNameText,
    final String customViewDescriptionText,
    final String customViewQueryText) {
    customViewSchemaName.setText(schemaName);
    customViewName.setText(customViewNameText);
    customViewDescription.setText(customViewDescriptionText);
    customViewQuery.setText(customViewQueryText);
  }

  private SimplePanel createCustomViewButton() {
    Button btnSave = new Button();
    btnSave.setText(messages.customViewsBtnSave());
    btnSave.addStyleName("btn btn-primary btn-save");

    btnSave.addClickHandler(event -> {
      final boolean valid = customViewFormValidator();
      if (valid) {
        customViewsSidebar.addSideBarHyperLink(customViewName.getText(),
          String.valueOf(counter), HistoryManager.linkToCreateWizardCustomViewsDelete(String.valueOf(counter)));

        CustomViewsParameter parameter = new CustomViewsParameter(customViewSchemaName.getText(), counter,
          customViewName.getText(),
          customViewDescription.getText(), customViewQuery.getText());
        customViewsParameters.put(String.valueOf(counter), parameter);
        counter++;
        setTextboxText("", "", "", "");
        customViewName.getElement().removeAttribute("required");
        customViewQuery.getElement().removeAttribute("required");
        customViewSchemaName.getElement().removeAttribute("required");
        customViewsSidebar.selectNone();
      } else {
        highlightFieldsWhenRequired();
      }
    });

    SimplePanel simplePanelforbtnSave = new SimplePanel();
    simplePanelforbtnSave.addStyleName("btn-item");
    simplePanelforbtnSave.add(btnSave);

    return simplePanelforbtnSave;
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

    if (ViewerStringUtils.isBlank(customViewSchemaName.getText())) {
      customViewSchemaName.getElement().setAttribute("required", "required");
      customViewSchemaName.addStyleName("wizard-connection-validator");
    }
  }
}
