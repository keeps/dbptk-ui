/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.desktop.client.dbptk.wizard.upload;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.fields.ComboBoxField;
import com.databasepreservation.common.client.common.fields.FileUploadField;
import com.databasepreservation.common.client.common.fields.GenericField;
import com.databasepreservation.common.client.common.lists.widgets.MultipleSelectionTablePanel;
import com.databasepreservation.common.client.common.utils.ApplicationType;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.wizard.connection.ConnectionParameters;
import com.databasepreservation.common.client.models.wizard.customViews.CustomViewsParameter;
import com.databasepreservation.common.client.models.wizard.customViews.CustomViewsParameters;
import com.databasepreservation.common.client.models.wizard.table.ExternalLobParameter;
import com.databasepreservation.common.client.models.wizard.table.ExternalLobsDialogBoxResult;
import com.databasepreservation.common.client.services.MigrationService;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.common.client.widgets.ConfigurationCellTableResources;
import com.databasepreservation.common.client.widgets.Toast;
import com.databasepreservation.desktop.client.common.sidebar.CustomViewsSidebar;
import com.databasepreservation.desktop.client.dbptk.wizard.WizardPanel;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
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

  private static final CustomViewsUiBinder binder = GWT.create(CustomViewsUiBinder.class);

  @UiField
  FlowPanel customViewsList;

  @UiField
  FlowPanel rightSideContainer;

  @UiField
  FlowPanel schemasCombobox;

  @UiField
  FlowPanel customViewsButtons;

  @UiField
  FlowPanel content;

  @UiField
  TextBox customViewName;

  @UiField
  TextBox customViewDescription;

  @UiField
  TextArea customViewQuery;

  @UiField
  Label customViewNameLabel;

  @UiField
  Label customViewDescriptionLabel;

  @UiField
  Label customViewQueryLabel;

  @UiField
  FlowPanel queryColumnsContainer;

  private static CustomViews instance = null;
  private CustomViewsSidebar customViewsSidebar;
  private Map<String, CustomViewsParameter> customViewsParameters = new HashMap<>();
  private int counter = 0;
  private boolean toSave;
  private final ComboBoxField customViewSchemaName;
  private final Button btnNext;
  private final ConnectionParameters connectionParameters;
  private boolean isSelectionEmpty;
  private MultipleSelectionTablePanel<ViewerColumn> queryColumnsTable;
  private final List<ViewerColumn> currentQueryColumns = new ArrayList<>();
  private final Map<String, ExternalLobParameter> externalLOBsParameters = new HashMap<>();
  private String currentBasePath = null;
  private boolean externalLOBsDelete = false;
  private String externalLOBsBtnText = messages.basicActionAdd();

  public static CustomViews getInstance(List<String> schemas, Button btnNext, ConnectionParameters connectionParameters,
    boolean isSelectionEmpty) {
    if (instance == null) {
      instance = new CustomViews(schemas, btnNext, connectionParameters, isSelectionEmpty);
    }
    return instance;
  }

  private CustomViews(List<String> schemas, Button btnNext, ConnectionParameters connectionParameters,
    boolean isSelectionEmpty) {
    initWidget(binder.createAndBindUi(this));

    this.btnNext = btnNext;
    this.connectionParameters = connectionParameters;
    this.isSelectionEmpty = isSelectionEmpty;
    customViewsSidebar = CustomViewsSidebar.getInstance();
    customViewsList.add(customViewsSidebar);

    customViewSchemaName = ComboBoxField.createInstance(messages.customViewsPageLabelForSchemaName(), schemas);
    customViewSchemaName.setCSSMetadata("form-row", "form-label-spaced", "form-combobox");
    customViewSchemaName.setRequired();

    schemasCombobox.add(customViewSchemaName);

    setRequired(customViewNameLabel, true);
    setRequired(customViewDescriptionLabel, false);
    setRequired(customViewQueryLabel, true);

    customViewsButtons.add(createCustomViewButton());
  }

  void checkIfHaveCustomViews() {
    if (customViewsParameters.isEmpty()) {
      if (isSelectionEmpty) {
        btnNext.setText(messages.basicActionNext());
        btnNext.setEnabled(false);
        btnNext.setTitle(messages.customViewsPageHintForDisableNext());
      } else {
        btnNext.setText(messages.basicActionSkip());
        btnNext.setEnabled(true);
        btnNext.setTitle("");
      }
    } else {
      btnNext.setText(messages.basicActionNext());
      btnNext.setEnabled(true);
      btnNext.setTitle("");
    }
  }

  @Override
  public void clear() {
    if (customViewsParameters != null && !customViewsParameters.isEmpty()) {
      customViewsParameters.clear();
    }

    if (queryColumnsContainer != null) {
      queryColumnsContainer.clear();
    }
    if (currentQueryColumns != null) {
      currentQueryColumns.clear();
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
    boolean empty = ViewerStringUtils.isBlank(customViewName.getText())
      && ViewerStringUtils.isBlank(customViewQuery.getText());

    if (empty) {
      toSave = false;
      return true;
    } else {
      toSave = true;
    }

    CustomViewsParameter parameter = new CustomViewsParameter(customViewSchemaName.getSelectedValue(), counter,
      customViewName.getText(), customViewDescription.getText(), customViewQuery.getText());
    parameter.setColumnParametersFromExternalLobs(externalLOBsParameters);

    toSave = !customViewsParameters.containsValue(parameter);

    return customViewsParameters.containsValue(parameter);
  }

  @Override
  public CustomViewsParameters getValues() {
    CustomViewsParameters parameters = new CustomViewsParameters();
    if (toSave) {
      CustomViewsParameter parameter = new CustomViewsParameter(customViewSchemaName.getSelectedValue(), counter,
        customViewName.getText(), customViewDescription.getText(), customViewQuery.getText());
      parameter.setColumnParametersFromExternalLobs(externalLOBsParameters);
      customViewsParameters.put(parameter.getSchemaName(), parameter);
    }
    parameters.setCustomViewsParameter(customViewsParameters);
    parameters.setExternalLobConfigurationSet(!externalLOBsParameters.isEmpty());

    return parameters;
  }

  @Override
  public void error() {
  }

  public void sideBarHighlighter(String customViewUUID, String action) {

    final CustomViewsParameter parameter = customViewsParameters.get(customViewUUID);

    if (action != null && action.equals(HistoryManager.ACTION_DELETE)) {
      Dialogs.showConfirmDialog(messages.customViewsPageTitle(), messages.customViewsPageTextForDialogConfirmDelete(),
        messages.basicActionCancel(), messages.basicActionConfirm(), new DefaultAsyncCallback<Boolean>() {
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
      setTextboxText(parameter.getSchemaName(), parameter.getCustomViewName(), parameter.getCustomViewDescription(),
        parameter.getCustomViewQuery());

      if (validateCustomViewQueryText()) {

        MigrationService.Util.call(this::populateQueryColumnsTable, (String errorMessage) -> {
          Dialogs.showErrors(messages.customViewsPageTitle(), errorMessage, messages.basicActionClose());
        }).testQuery(connectionParameters, parameter.getCustomViewQuery());
      }

      Button btnNew = new Button();
      btnNew.setText(messages.basicActionNew());
      btnNew.addStyleName("btn btn-primary btn-plus");
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
      btnUpdate.setText(messages.basicActionSave());
      btnUpdate.addStyleName("btn btn-primary btn-save");
      btnUpdate.addClickHandler(event -> {
        final int valid = customViewFormValidatorUpdate(parameter.getCustomViewName());
        if (valid == -1) {
          MigrationService.Util.call((List<List<String>> result) -> {
            updateCustomViewParameters(parameter.getCustomViewID(), customViewSchemaName.getSelectedValue(),
              customViewName.getText(), customViewDescription.getText(), customViewQuery.getText());
            Toast.showInfo(messages.customViewsPageTitle(), messages.customViewsUpdateMessage());
          }, (String errorMessage) -> {
            Dialogs.showErrors(messages.customViewsPageTitle(), errorMessage, messages.basicActionClose());
          }).testQuery(connectionParameters, customViewQuery.getText());
        } else {
          Toast.showError(messages.customViewsPageTitle(), messages.customViewsPageErrorMessagesFor(valid));
          highlightFieldsWhenRequired();
        }
      });

      SimplePanel simplePanelForUpdateButton = new SimplePanel();
      simplePanelForUpdateButton.addStyleName("btn-item");
      simplePanelForUpdateButton.add(btnUpdate);
      customViewsButtons.add(simplePanelForUpdateButton);

      SimplePanel simplePanelForTestButton = new SimplePanel();
      simplePanelForTestButton.addStyleName("btn-item");
      simplePanelForTestButton.add(getButtonTestQuery(btnUpdate));
      customViewsButtons.add(simplePanelForTestButton);

      SimplePanel simplePanelForNewButton = new SimplePanel();
      simplePanelForNewButton.addStyleName("btn-item");
      simplePanelForNewButton.add(btnNew);
      customViewsButtons.add(simplePanelForNewButton);

      customViewsSidebar.select(customViewUUID);
    }
  }

  private void populateQueryColumnsTable(List<List<String>> result) {
    if (result == null || result.isEmpty()) {
      return;
    }

    if (queryColumnsContainer != null) {
      queryColumnsContainer.clear();
    }
    if (currentQueryColumns != null) {
      currentQueryColumns.clear();
    }

    List<String> headerRow = result.get(0);
    for (String columnName : headerRow) {
      ViewerColumn column = new ViewerColumn();
      column.setDisplayName(columnName);
      currentQueryColumns.add(column);
    }

    queryColumnsTable = new MultipleSelectionTablePanel<>(GWT.create(ConfigurationCellTableResources.class));
    queryColumnsTable.setHeight("35vh");

    Button btnSelectToggle = new Button(messages.basicActionSelectNone());
    btnSelectToggle.addStyleName("btn btn-primary btn-select-none");
    final boolean[] isAllSelected = {true};

    btnSelectToggle.addClickHandler(event -> {
      isAllSelected[0] = !isAllSelected[0];
      if (isAllSelected[0]) {
        btnSelectToggle.setText(messages.basicActionSelectNone());
        btnSelectToggle.removeStyleName("btn-select-all");
        btnSelectToggle.addStyleName("btn-select-none");
      } else {
        btnSelectToggle.setText(messages.basicActionSelectAll());
        btnSelectToggle.removeStyleName("btn-select-none");
        btnSelectToggle.addStyleName("btn-select-all");
      }
      for (ViewerColumn col : currentQueryColumns) {
        queryColumnsTable.getSelectionModel().setSelected(col, isAllSelected[0]);
      }
    });

    FlowPanel selectPanel = new FlowPanel();
    selectPanel.addStyleName("select-buttons-container");
    selectPanel.add(btnSelectToggle);

    final ButtonDatabaseColumn buttonDatabaseColumn = new ButtonDatabaseColumn() {
      @Override
      public String getValue(ViewerColumn viewerColumn) {
        return messages.tableAndColumnsPageTextForExternalLOBConfigure();
      }
    };

    buttonDatabaseColumn.setFieldUpdater((index, object, value) -> {
      try {
        String id = object.getDisplayName();

        externalLOBsDelete = false;
        externalLOBsBtnText = messages.basicActionAdd();

        final ComboBoxField referenceType = ComboBoxField
          .createInstance(messages.tableAndColumnsPageLabelForReferenceType());
        referenceType.setComboBoxValue(messages.tableAndColumnsFileSystemOption(), "file-system");

        if (connectionParameters != null && connectionParameters.doSSH()) {
          referenceType.setComboBoxValue(messages.tableAndColumnsRemoteFileSystem(), "remote-file-system");
        }
        referenceType.setCSSMetadata("form-row", "form-label-spaced", "form-combobox");

        FlowPanel helperReferenceType = new FlowPanel();
        helperReferenceType.addStyleName("form-helper");
        InlineHTML spanReferenceType = new InlineHTML();
        spanReferenceType.addStyleName("form-text-helper text-muted");
        spanReferenceType.setText(messages.tableAndColumnsPageDescriptionForExternalLOBReferenceType());
        referenceType.addHelperText(spanReferenceType);
        helperReferenceType.add(referenceType);
        helperReferenceType.add(spanReferenceType);

        if (ApplicationType.getType().equals(ViewerConstants.APPLICATION_ENV_DESKTOP)) {
          FileUploadField fileUploadField = FileUploadField.createInstance(
            messages.tableAndColumnsPageLabelForBasePath(), messages.tableAndColumnsPageTableHeaderTextForSelect());
          fileUploadField.setParentCSS("form-row");
          fileUploadField.setLabelCSS("form-label-spaced");
          fileUploadField.setButtonCSS("btn btn-link form-button");

          FlowPanel helperFileUploadField = new FlowPanel();
          helperFileUploadField.addStyleName("form-helper");
          InlineHTML spanFileUploadField = new InlineHTML();
          spanFileUploadField.addStyleName("form-text-helper text-muted");
          spanFileUploadField.setText(messages.tableAndColumnsPageDescriptionForExternalLOBBasePath());
          fileUploadField.addHelperText(spanFileUploadField);
          helperFileUploadField.add(fileUploadField);
          helperFileUploadField.add(spanFileUploadField);

          if (externalLOBsParameters.get(id) != null) {
            if (externalLOBsParameters.get(id).getBasePath() != null) {
              String displayPath = com.databasepreservation.common.client.tools.PathUtils
                .getFileName(externalLOBsParameters.get(id).getBasePath());
              fileUploadField.setPathLocation(displayPath, externalLOBsParameters.get(id).getBasePath());
            }
            externalLOBsDelete = true;
            externalLOBsBtnText = messages.basicActionUpdate();
          }

          fileUploadField.buttonAction(() -> {
            JavaScriptObject options = com.databasepreservation.common.client.tools.JSOUtils
              .getOpenDialogOptions(Collections.singletonList("openDirectory"), Collections.emptyList());
            String path = com.databasepreservation.common.client.common.utils.JavascriptUtils.openFileDialog(options);
            if (path != null) {
              currentBasePath = path;
              String displayPath = com.databasepreservation.common.client.tools.PathUtils.getFileName(path);
              fileUploadField.setPathLocation(displayPath, path);
              fileUploadField.setInformationPathCSS("gwt-Label-disabled information-path");
            }
          });

          Dialogs.showExternalLobsSetupDialog(messages.tableAndColumnsPageDialogTitleForExternalLOBDialog(),
            helperReferenceType, helperFileUploadField, messages.basicActionCancel(), externalLOBsBtnText,
            externalLOBsDelete, new DefaultAsyncCallback<ExternalLobsDialogBoxResult>() {
              @Override
              public void onSuccess(ExternalLobsDialogBoxResult result) {
                if (result.getOption().equals("add") && result.isResult()) {
                  ExternalLobParameter externalLobParameter = new ExternalLobParameter();
                  externalLobParameter.setBasePath(currentBasePath);
                  externalLobParameter.setReferenceType(referenceType.getSelectedValue());
                  externalLOBsParameters.put(id, externalLobParameter);
                }
                if (result.getOption().equals("delete") && result.isResult()) {
                  externalLOBsDelete = false;
                  externalLOBsParameters.remove(id);
                  externalLOBsBtnText = messages.basicActionAdd();
                }
                queryColumnsTable.getDisplay().redrawRow(index);
              }
            });
        } else {
          TextBox textBox = new TextBox();
          textBox.addStyleName("form-textbox");
          if (externalLOBsParameters.get(id) != null) {
            textBox.setText(externalLOBsParameters.get(id).getBasePath());
            externalLOBsDelete = true;
            externalLOBsBtnText = messages.basicActionUpdate();
          }
          GenericField genericField = GenericField.createInstance(messages.tableAndColumnsPageLabelForBasePath(),
            textBox);
          genericField.setCSSMetadata("form-row", "form-label-spaced");

          Dialogs.showExternalLobsSetupDialog(messages.tableAndColumnsPageDialogTitleForExternalLOBDialog(),
            helperReferenceType, genericField, messages.basicActionCancel(), externalLOBsBtnText, externalLOBsDelete,
            new DefaultAsyncCallback<ExternalLobsDialogBoxResult>() {
              @Override
              public void onSuccess(ExternalLobsDialogBoxResult result) {
                if (result.getOption().equals("add") && result.isResult()) {
                  ExternalLobParameter externalLOBsParameter = new ExternalLobParameter();
                  externalLOBsParameter.setBasePath(textBox.getText());
                  externalLOBsParameter.setReferenceType(referenceType.getSelectedValue());
                  externalLOBsParameters.put(id, externalLOBsParameter);
                }
                if (result.getOption().equals("delete") && result.isResult()) {
                  externalLOBsParameters.remove(id);
                }
                queryColumnsTable.getDisplay().redrawRow(index);
              }
            });
        }
      } catch (Exception e) {
        Dialogs.showErrors(messages.customViewsPageTitle(), e.getMessage(), messages.basicActionClose());
      }
    });

    queryColumnsTable.createTable(selectPanel, Arrays.asList(1, 2), currentQueryColumns.iterator(),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableAndColumnsPageTableHeaderTextForSelect(), 4,
        new Column<ViewerColumn, Boolean>(new CheckboxCell(true, true)) {
          @Override
          public Boolean getValue(ViewerColumn viewerColumn) {
            return queryColumnsTable.getSelectionModel().isSelected(viewerColumn);
          }
        }),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableAndColumnsPageTableHeaderTextForColumnName(), 15,
        new TextColumn<ViewerColumn>() {
          @Override
          public String getValue(ViewerColumn column) {
            return column.getDisplayName();
          }
        }),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableAndColumnsPageTableHeaderTextForOptions(), 10,
        buttonDatabaseColumn));

    for (ViewerColumn col : currentQueryColumns) {
      queryColumnsTable.getSelectionModel().setSelected(col, true);
    }

    queryColumnsContainer.add(queryColumnsTable);
  }

  private abstract static class ButtonDatabaseColumn extends Column<ViewerColumn, String> {
    public ButtonDatabaseColumn() {
      super(new ButtonCell());
    }

    @Override
    public void render(Cell.Context context, ViewerColumn object, SafeHtmlBuilder sb) {
      String value = getValue(object);
      sb.appendHtmlConstant("<button class=\"btn btn-link-info\" type=\"button\" tabindex=\"-1\">");
      if (value != null) {
        sb.append(com.google.gwt.safehtml.shared.SafeHtmlUtils.fromString(value));
      }
      sb.appendHtmlConstant("</button>");
    }
  }

  void refreshCustomButtons(boolean isSelectionEmpty) {
    customViewsButtons.clear();
    customViewsButtons.add(createCustomViewButton());
    this.isSelectionEmpty = isSelectionEmpty;
    checkIfHaveCustomViews();
  }

  private int customViewFormValidator() {
    String viewNameText = customViewName.getText();
    String viewQueryText = customViewQuery.getText();

    boolean value = ViewerStringUtils.isBlank(viewNameText) || ViewerStringUtils.isBlank(viewQueryText);

    if (value) {
      return 1;
    }

    boolean sameName = false;
    for (CustomViewsParameter p : customViewsParameters.values()) {
      if (p.getCustomViewName().equalsIgnoreCase(viewNameText)) {
        sameName = true;
      }
    }

    if (sameName) {
      return 2;
    }

    return -1;
  }

  private int customViewFormValidatorUpdate(final String customViewName) {
    String viewNameText = this.customViewName.getText();
    String viewQueryText = this.customViewQuery.getText();

    boolean value = ViewerStringUtils.isBlank(viewNameText) || ViewerStringUtils.isBlank(viewQueryText);

    if (value) {
      return 1;
    }

    boolean sameName = false;
    for (CustomViewsParameter p : customViewsParameters.values()) {
      if (p.getCustomViewName().equalsIgnoreCase(viewNameText.toLowerCase())
        && !p.getCustomViewName().equalsIgnoreCase(customViewName.toLowerCase())) {
        sameName = true;
      }
    }

    if (sameName) {
      return 2;
    }

    return -1;
  }

  private void setRequired(Widget label, boolean required) {
    if (required)
      label.addStyleName("form-label-mandatory");
    else
      label.removeStyleName("form-label-mandatory");
  }

  private void updateCustomViewParameters(final int id, final String customViewSchemaName,
    final String customViewNameText, final String customViewDescriptionText, final String customViewQueryText) {
    String customViewUUID = String.valueOf(id);
    final CustomViewsParameter parameter = customViewsParameters.get(customViewUUID);
    parameter.setSchemaName(customViewSchemaName);
    parameter.setCustomViewName(customViewNameText);
    parameter.setCustomViewDescription(customViewDescriptionText);
    parameter.setCustomViewQuery(customViewQueryText);
    parameter.setColumnParametersFromExternalLobs(externalLOBsParameters);

    customViewsParameters.put(customViewUUID, parameter);
    customViewsSidebar.updateSidebarHyperLink(customViewUUID, customViewNameText);
  }

  private void deleteCustomView(Integer id) {
    String customViewUUID = String.valueOf(id);
    customViewsParameters.remove(customViewUUID);
    customViewsSidebar.removeSideBarHyperLink(customViewUUID);
    checkIfHaveCustomViews();
  }

  private void setTextboxText(final String schemaName, final String customViewNameText,
    final String customViewDescriptionText, final String customViewQueryText) {
    customViewName.setText(customViewNameText);
    customViewDescription.setText(customViewDescriptionText);
    customViewQuery.setText(customViewQueryText);
  }

  private FlowPanel createCustomViewButton() {
    Button btnSave = new Button();
    btnSave.setText(messages.basicActionSave());
    btnSave.addStyleName("btn btn-primary btn-save");
    btnSave.setEnabled(false);

    btnSave.addClickHandler(event -> {
      final int valid = customViewFormValidator();
      if (valid == -1) {
        MigrationService.Util.call((List<List<String>> result) -> {
          customViewsSidebar.addSideBarHyperLink(customViewName.getText(), String.valueOf(counter),
            HistoryManager.linkToCreateWizardCustomViewsDelete(String.valueOf(counter)));

          CustomViewsParameter parameter = new CustomViewsParameter(customViewSchemaName.getSelectedValue(), counter,
            customViewName.getText(), customViewDescription.getText(), customViewQuery.getText());
          parameter.setColumnParametersFromExternalLobs(externalLOBsParameters);
          customViewsParameters.put(String.valueOf(counter), parameter);
          counter++;
          setTextboxText("", "", "", "");
          customViewName.getElement().removeAttribute("required");
          customViewQuery.getElement().removeAttribute("required");
          customViewSchemaName.getElement().removeAttribute("required");
          customViewsSidebar.selectNone();
          queryColumnsContainer.clear();

          checkIfHaveCustomViews();
        }, (String errorMessage) -> {
          Dialogs.showErrors(messages.customViewsPageTitle(), errorMessage, messages.basicActionClose());
          checkIfHaveCustomViews();
        }).testQuery(connectionParameters, customViewQuery.getText());
      } else {
        Toast.showError(messages.customViewsPageTitle(), messages.customViewsPageErrorMessagesFor(valid));
        highlightFieldsWhenRequired();
        checkIfHaveCustomViews();
      }
    });

    FlowPanel FlowPanelForSaveButton = new FlowPanel();
    FlowPanelForSaveButton.addStyleName("btn-item");
    FlowPanelForSaveButton.add(btnSave);

    FlowPanel FlowPanelForTestButton = new FlowPanel();
    FlowPanelForTestButton.addStyleName("btn-item");
    FlowPanelForTestButton.add(getButtonTestQuery(btnSave));

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

  private Button getButtonTestQuery(Button btnSave) {
    Button btnTest = new Button();
    btnTest.setText(messages.basicActionTest());
    btnTest.addStyleName("btn btn-primary btn-run");

    btnTest.addClickHandler(event -> {
      if (validateCustomViewQueryText()) {
        Widget spinner = new HTML(SafeHtmlUtils.fromSafeConstant(
          "<div class='spinner'><div class='double-bounce1'></div><div class='double-bounce2'></div></div>"));
        content.add(spinner);

        MigrationService.Util.call((List<List<String>> result) -> {
          content.remove(spinner);
          Dialogs.showQueryResult(messages.customViewsPageTextForQueryResultsDialogTitle(), messages.basicActionClose(),
            result);

          populateQueryColumnsTable(result);
          btnSave.setEnabled(true);
        }, (String errorMessage) -> {
          content.remove(spinner);
          Dialogs.showErrors(messages.customViewsPageTitle(), errorMessage, messages.basicActionClose());
        }).testQuery(connectionParameters, customViewQuery.getText());
      } else {
        Toast.showError(messages.customViewsPageTitle(), messages.customViewsPageErrorMessageForQueryError());
      }
    });

    return btnTest;
  }
}
