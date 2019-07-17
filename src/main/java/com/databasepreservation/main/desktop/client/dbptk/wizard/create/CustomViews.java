package com.databasepreservation.main.desktop.client.dbptk.wizard.create;

import java.util.ArrayList;
import java.util.Collection;
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
  TextBox customViewName, customViewDescription;

  @UiField
  TextArea customViewQuery;

  @UiField
  Label customViewNameLabel, customViewDescriptionLabel, customViewQueryLabel;

  private static CustomViews instance = null;
  private CustomViewsSidebar customViewsSidebar;
  private HashMap<String, CustomViewsParameter> customViewsParameters = new HashMap<>();
  private int counter = 0;
  private FlowPanel customViewsButtons;

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

    setRequired(customViewNameLabel, true);
    setRequired(customViewDescriptionLabel, false);
    setRequired(customViewQueryLabel, true);

    customViewName.addKeyUpHandler(event -> {
//      if (ViewerStringUtils.isBlank(customViewName.getText())) {
//        customViewNameLabel.addStyleName("form-label-spaced-error");
//        customViewName.addStyleName("textbox-error");
//      } else {
//        customViewNameLabel.removeStyleName("form-label-spaced-error");
//        customViewName.removeStyleName("textbox-error");
//      }
//    });
//
//    customViewQuery.addKeyUpHandler(event -> {
//      if (ViewerStringUtils.isBlank(customViewQuery.getText())) {
//        customViewQueryLabel.addStyleName("form-label-spaced-error");
//        customViewQuery.addStyleName("textarea-error");
//      } else {
//        customViewQueryLabel.removeStyleName("form-label-spaced-error");
//        customViewQuery.removeStyleName("textarea-error");
//      }
    });

    this.customViewsButtons.add(createCustomViewButton());

  }

  @Override
  public void clear() {

  }

  @Override
  public boolean validate() {
    return true;
  }

  @Override
  public CustomViewsParameters getValues() {
    CustomViewsParameters customViewsParameters = new CustomViewsParameters();
    ArrayList<CustomViewsParameter> parameters = new ArrayList<>(this.customViewsParameters.values());
    customViewsParameters.setCustomViewsParameter(parameters);
    return customViewsParameters;
  }

  @Override
  public void error() {

  }

  public void sideBarHighlighter(String customViewUUID) {
    customViewsButtons.clear();

    final CustomViewsParameter parameter = customViewsParameters.get(customViewUUID);

    setTextboxText(parameter.getCustomViewName(), parameter.getCustomViewDescription(), parameter.getCustomViewQuery());

    Button btnNew = new Button();
    btnNew.setText(messages.createCardButton());
    btnNew.addStyleName("btn btn-primary btn-new");
    btnNew.addClickHandler(event -> {
      setTextboxText("", "", "");
      customViewsSidebar.select("");
      customViewsButtons.clear();
      customViewsButtons.add(createCustomViewButton());
    });
    Button btnUpdate = new Button();
    btnUpdate.setText(messages.update());
    btnUpdate.addStyleName("btn btn-primary btn-save");
    btnUpdate.addClickHandler(event -> {
      final boolean valid = customViewFormValidator();
      if (valid) {
        updateCustomViewParameters(parameter.getCustomViewUUID(), customViewName.getText(),
          customViewDescription.getText(), customViewQuery.getText());
      }
    });
    Button btnDelete = new Button();
    btnDelete.setText(messages.delete());
    btnDelete.addStyleName("btn btn-primary btn-delete");
    btnDelete.addClickHandler(event -> {
      deleteCustomView(parameter.getCustomViewUUID());
      setTextboxText("", "", "");
    });

    SimplePanel simplePanelforbtnNew = new SimplePanel();
    simplePanelforbtnNew.addStyleName("btn-item");
    simplePanelforbtnNew.add(btnNew);

    SimplePanel simplePanelforbtnUpdate = new SimplePanel();
    simplePanelforbtnUpdate.addStyleName("btn-item");
    simplePanelforbtnUpdate.add(btnUpdate);

    SimplePanel simplePanelforbtnDelete = new SimplePanel();
    simplePanelforbtnDelete.addStyleName("btn-item");
    simplePanelforbtnDelete.add(btnDelete);

    customViewsButtons.add(simplePanelforbtnNew);
    customViewsButtons.add(simplePanelforbtnUpdate);
    customViewsButtons.add(simplePanelforbtnDelete);

    customViewsSidebar.select(customViewUUID);
  }

  public void refreshCustomButtons() {
    customViewsButtons.clear();
    customViewsButtons.add(createCustomViewButton());
  }

  private boolean customViewFormValidator() {
    String viewNameText = customViewName.getText();
    String viewQueryText = customViewQuery.getText();

    boolean value = !(ViewerStringUtils.isBlank(viewNameText) || ViewerStringUtils.isBlank(viewQueryText));

    boolean sameName = false;
    for (CustomViewsParameter p : customViewsParameters.values()) {
      if (p.getCustomViewName().toLowerCase().equals(viewNameText.toLowerCase())) {
        sameName = true;
      }
    }

    return value && sameName;
  }

  private void setRequired(Widget label, boolean required) {
    if (required)
      label.addStyleName("form-label-mandatory");
    else
      label.removeStyleName("form-label-mandatory");
  }

  private void updateCustomViewParameters(final int id, final String customViewNameText,
    final String customViewDescriptionText, final String customViewQueryText) {
    String customViewUUID = String.valueOf(id);
    final CustomViewsParameter parameter = customViewsParameters.get(customViewUUID);
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

  private void setTextboxText(final String customViewNameText, final String customViewDescriptionText,
    final String customViewQueryText) {
    customViewName.setText(customViewNameText);
    customViewDescription.setText(customViewDescriptionText);
    customViewQuery.setText(customViewQueryText);
  }

  private Button createCustomViewButton() {
    Button btnCreate = new Button();
    btnCreate.setText(messages.createCardButton());
    btnCreate.addStyleName("btn btn-primary");

    btnCreate.addClickHandler(event -> {
      final boolean valid = customViewFormValidator();
      if (valid) {
        customViewsSidebar.addSideBarHyperLink(HistoryManager.ROUTE_WIZARD_CUSTOM_VIEWS, customViewName.getText(),
          String.valueOf(counter));
        CustomViewsParameter parameter = new CustomViewsParameter(counter, customViewName.getText(),
          customViewDescription.getText(), customViewQuery.getText());
        customViewsParameters.put(String.valueOf(counter), parameter);
        counter++;
      } else {
        if (ViewerStringUtils.isBlank(customViewName.getText())) {
          customViewName.getElement().setAttribute("required", "required");
          customViewName.addStyleName("wizard-connection-validator");
        }

        if (ViewerStringUtils.isBlank(customViewQuery.getText())) {
          customViewQuery.getElement().setAttribute("required", "required");
          customViewQuery.addStyleName("wizard-connection-validator");
        }
      }
    });

    return btnCreate;
  }
}
