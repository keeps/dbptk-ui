package com.databasepreservation.common.client.common.visualization.manager.SIARDPanel.navigation;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.client.common.NavigationPanel;
import com.databasepreservation.common.client.common.dialogs.CommonDialogs;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.helpers.HelperValidator;
import com.databasepreservation.common.client.common.utils.ApplicationType;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.common.utils.JavascriptUtils;
import com.databasepreservation.common.client.common.utils.html.LabelUtils;
import com.databasepreservation.common.client.common.visualization.manager.SIARDPanel.SIARDManagerPage;
import com.databasepreservation.common.client.common.visualization.validation.ValidatorPage;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseValidationStatus;
import com.databasepreservation.common.client.services.SiardService;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.tools.Humanize;
import com.databasepreservation.common.client.tools.RestUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

import config.i18n.client.ClientMessages;

public class ValidationNavigationPanel {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final String LABEL_FIELD = "label-field";
  private static final String VALUE_FIELD = "value-field";
  private static final String BTN_ITEM = "btn-item";
  private static Map<String, ValidationNavigationPanel> instances = new HashMap<>();
  private ViewerDatabase database;
  private Button btnValidate;
  private Button btnOpenValidator;
  private Button btnReport;
  private Button btnDeleteReport;
  private HelperValidator validator = null;
  private MetadataField validatedAt = null;
  private MetadataField validationStatus = null;
  private FlowPanel validationDetails = new FlowPanel();
  private MetadataField validationWarnings = null;
  private MetadataField version = null;
  private NavigationPanel navigationPanel;

  public static ValidationNavigationPanel getInstance(ViewerDatabase database) {
    return instances.computeIfAbsent(database.getUuid(), k -> new ValidationNavigationPanel(database));
  }

  private ValidationNavigationPanel(ViewerDatabase database) {
    this.database = database;
  }

  public NavigationPanel build() {
    buildValidateButton();
    buildReportButton();
    buildOpenButton();
    buildDeleteButton();

    // information
    validatedAt = MetadataField.createInstance(messages.SIARDHomePageLabelForValidatedAt(),
      messages.humanizedTextForSIARDNotValidated());
    version = MetadataField.createInstance(messages.SIARDHomePageLabelForValidateBy(),
      messages.humanizedTextForSIARDNotValidated());
    validationWarnings = MetadataField.createInstance(messages.SIARDHomePageLabelForValidationWarnings(),
      messages.humanizedTextForSIARDNotValidated());
    updateValidationInformation();

    // Validation Status info
    validationStatus = MetadataField.createInstance(messages.SIARDHomePageLabelForValidationStatus(),
      LabelUtils.getSIARDValidationStatus(database.getValidationStatus()));
    validationStatus.setCSS(null, LABEL_FIELD, VALUE_FIELD);

    validatedAt.setCSS(null, LABEL_FIELD, VALUE_FIELD);
    version.setCSS(null, LABEL_FIELD, VALUE_FIELD);
    validationWarnings.setCSS(null, LABEL_FIELD, VALUE_FIELD);

    navigationPanel = NavigationPanel.createInstance(messages.SIARDHomePageOptionsHeaderForValidation());
    navigationPanel.addToDescriptionPanel(messages.SIARDHomePageOptionsDescriptionForValidation());

    // buttons
    updateValidationButtons();

    navigationPanel.addToInfoPanel(validationStatus);
    navigationPanel.addToInfoPanel(validationDetails);
    navigationPanel.addToInfoPanel(validationWarnings);
    navigationPanel.addToInfoPanel(validatedAt);
    navigationPanel.addToInfoPanel(version);

    return navigationPanel;
  }

  private void buildValidateButton() {
    validator = new HelperValidator(database.getPath());
    btnValidate = new Button();
    btnValidate.setText(messages.SIARDHomePageButtonTextValidateNow());
    btnValidate.addStyleName("btn btn-outline-primary btn-play");

    btnValidate.addClickHandler(event -> {
      if (database.getVersion().equals(ViewerConstants.SIARD_V21)) {
        if (database.getPath() != null && !database.getPath().isEmpty()) {
          if (ApplicationType.getType().equals(ViewerConstants.APPLICATION_ENV_DESKTOP)) {
            Dialogs.showValidatorSettings(messages.SIARDValidatorSettings(), messages.basicActionCancel(),
              messages.basicActionConfirm(), validator, new DefaultAsyncCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                  if (result && validator.getReporterPathFile() != null) {
                    runValidation();
                  }
                }
              });
          } else {
            runValidation();
          }
        }
      } else {
        Dialogs.showInformationDialog(messages.SIARDValidatorDialogInformationTitle(),
          messages.SIARDValidatorTextForVersionCannotBeValidated(), messages.basicActionUnderstood(), "btn btn-link");
      }
    });
  }

  private void runValidation() {
    ValidatorPage.clear(database.getUuid());
    HistoryManager.gotoSIARDValidator(database.getUuid());
    SiardService.Util.call((Boolean result) -> {
      // Do nothing, wait for update finish
    }, (String errorMessage) -> {
      ValidatorPage.getInstance(database).error();
      Dialogs.showErrors(messages.validatorPageTextForTitle(), errorMessage, messages.basicActionClose());
    }).validateSiard(database.getUuid(), database.getUuid(), validator.getReporterPathFile(),
      validator.getUdtPathFile(), Boolean.parseBoolean(validator.skipAdditionalChecks()));
  }

  private void buildReportButton() {
    // See Report btn
    btnReport = new Button();
    btnReport.addStyleName("btn btn-outline-primary");
    if (ApplicationType.getType().equals(ViewerConstants.APPLICATION_ENV_DESKTOP)) {
      btnReport.setText(messages.SIARDHomePageButtonTextForOpenReport());
      btnReport.addStyleName("btn-open");
      btnReport.addClickHandler(clickEvent -> JavascriptUtils.showItem(database.getValidatorReportPath()));
    } else {
      btnReport.setText(messages.SIARDHomePageButtonTextForDownloadReport());
      btnReport.addStyleName("btn-download");
      btnReport.addClickHandler(clickEvent -> {
        SafeUri downloadUri = RestUtils.createFileResourceDownloadValidationReportUri(database.getUuid());
        Window.Location.assign(downloadUri.asString());
      });
    }
  }

  private void buildOpenButton() {
    // Open validator btn
    btnOpenValidator = new Button();
    btnOpenValidator.setText(messages.SIARDHomePageButtonTextOpenValidate());
    btnOpenValidator.addStyleName("btn btn-outline-primary btn-validate");
    btnOpenValidator.addClickHandler(event -> HistoryManager.gotoSIARDValidator(database.getUuid()));
  }

  private void buildDeleteButton() {
    // Delete Report btn
    btnDeleteReport = new Button();
    btnDeleteReport.setText(messages.SIARDHomePageButtonTextForDeleteIngested());
    btnDeleteReport.addStyleName("btn btn-outline-danger btn-delete");
    btnDeleteReport.addClickHandler(event -> {
      if (!database.getValidationStatus().equals(ViewerDatabaseValidationStatus.VALIDATION_RUNNING)) {
        CommonDialogs.showConfirmDialog(messages.SIARDHomePageDialogTitleForDeleteValidationReport(),
          messages.SIARDHomePageTextForDeleteSIARDReportValidation(), messages.basicActionCancel(),
          messages.basicActionConfirm(), CommonDialogs.Level.DANGER, "500px", new DefaultAsyncCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
              if (result) {
                delete();
              }
            }
          });
      }
    });
  }

  private void updateValidationInformation() {
    if (database.getValidationStatus().equals(ViewerDatabaseValidationStatus.VALIDATION_SUCCESS)
      || database.getValidationStatus().equals(ViewerDatabaseValidationStatus.VALIDATION_FAILED)) {
      validatedAt.updateText(Humanize.formatDateTime(database.getValidatedAt()));
      version.updateText(
        messages.SIARDHomePageLabelDBPTKVersion(database.getValidatedVersion(), ViewerConstants.DBPTK_RELEASE_LINK));
      validatedAt.setVisible(true);
      version.setVisible(true);
      // indicators
      updateValidationIndicators();

      validationDetails.setVisible(true);
    } else { // NOT_VALIDATE, RUNNING, ERROR
      validationDetails.setVisible(false);
      validatedAt.setVisible(false);
      version.setVisible(false);
      validationWarnings.setVisible(false);
    }
  }

  private void updateValidationStatus() {
    validationStatus.updateText(Humanize.validationStatus(database.getValidationStatus()));
    switch (database.getValidationStatus()) {
      case VALIDATION_SUCCESS:
        validationStatus.getMetadataValue().setStyleName("label-success");
        break;
      case VALIDATION_FAILED:
        validationStatus.getMetadataValue().setStyleName("label-danger");
        break;
      case ERROR:
        validationStatus.getMetadataValue().setStyleName("label-danger label-error");
        break;
      case NOT_VALIDATED:
        validationStatus.getMetadataValue().setStyleName("label-default");
        break;
      case VALIDATION_RUNNING:
      default:
        validationStatus.getMetadataValue().setStyleName("label-info");
        break;
    }
  }

  private void updateValidationButtons() {
    navigationPanel.clearButtonsPanel();

    switch (database.getValidationStatus()) {
      case VALIDATION_SUCCESS:
      case VALIDATION_FAILED:
        btnValidate.setText(messages.SIARDHomePageButtonTextRunValidationAgain());
        navigationPanel.addButton(CommonClientUtils.wrapOnDiv(BTN_ITEM, btnValidate));
        if (ValidatorPage.checkInstance(database.getUuid())) {
          navigationPanel.addButton(CommonClientUtils.wrapOnDiv(BTN_ITEM, btnOpenValidator));
        }
        navigationPanel.addButton(CommonClientUtils.wrapOnDiv(BTN_ITEM, btnReport));
        if (database.getValidatorReportPath() != null && !database.getValidatorReportPath().isEmpty()) {
          navigationPanel.addButton(CommonClientUtils.wrapOnDiv(BTN_ITEM, btnDeleteReport));
        }
        break;
      case VALIDATION_RUNNING:
        if (ValidatorPage.checkInstance(database.getUuid())) {
          navigationPanel.addButton(CommonClientUtils.wrapOnDiv(BTN_ITEM, btnOpenValidator));
        }
        break;
      case NOT_VALIDATED:
        btnValidate.setText(messages.SIARDHomePageButtonTextValidateNow());
        navigationPanel.addButton(CommonClientUtils.wrapOnDiv(BTN_ITEM, btnValidate));
        break;
      case ERROR:
        if (database.getPath() != null && !database.getPath().isEmpty()) {
          btnValidate.setEnabled(true);
          btnValidate.setTitle("");
        } else {
          btnValidate.setEnabled(false);
          btnValidate.setTitle(messages.SIARDHomePageTextForRequiredSIARDFile());
        }
        btnValidate.setText(messages.SIARDHomePageButtonTextValidateNow());
        navigationPanel.addButton(CommonClientUtils.wrapOnDiv(BTN_ITEM, btnValidate));
        break;
    }

    if (database.getPath() == null || database.getPath().isEmpty()) {
      btnValidate.setEnabled(false);
      btnValidate.setTitle(messages.SIARDHomePageTextForRequiredSIARDFile());
    }
  }

  private void updateValidationIndicators() {
    validationDetails.clear();
    validationDetails.addStyleName("validation-indicators");
    GWT.log("ValidationStatus::" + database.getValidationStatus());
    if (database.getValidationStatus().equals(ViewerDatabaseValidationStatus.VALIDATION_SUCCESS)
      || database.getValidationStatus().equals(ViewerDatabaseValidationStatus.VALIDATION_FAILED)) {
      Label label = new Label(messages.SIARDHomePageLabelForValidationDetails());
      label.addStyleName(LABEL_FIELD);
      validationDetails.add(label);
      FlowPanel panel = new FlowPanel();
      panel.addStyleName("validation-indicators");
      if (database.getValidationPassed() != null) {
        panel.add(buildIndicators(FontAwesomeIconManager.CHECK, "passed",
          messages.SIARDHomePageTextForValidationIndicatorsSuccess(Integer.parseInt(database.getValidationPassed()))));
      }
      if (database.getValidationErrors() != null) {
        panel.add(buildIndicators(FontAwesomeIconManager.TIMES, "errors",
          messages.SIARDHomePageTextForValidationIndicatorsFailed(Integer.parseInt(database.getValidationErrors()))));
      }

      if (database.getValidationSkipped() != null) {
        panel.add(buildIndicators(FontAwesomeIconManager.SKIPPED, "skipped",
          messages.SIARDHomePageTextForValidationIndicatorsSkipped(Integer.parseInt(database.getValidationSkipped()))));
      }
      if (panel.getWidgetCount() > 0) {
        validationDetails.add(panel);
      }
      if (database.getValidationWarnings() != null) {
        validationWarnings.setVisible(true);
        validationWarnings.updateText(messages
          .SIARDHomePageTextForValidationIndicatorsWarnings(Integer.parseInt(database.getValidationWarnings())));
      } else {
        validationWarnings.setVisible(false);
      }
    }
  }

  private FlowPanel buildIndicators(String icon, String style, String title) {
    FlowPanel panel = new FlowPanel();
    panel.setStyleName("indicator");
    panel.setTitle(title);
    HTML iconHTML = new HTML(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(icon)));
    iconHTML.addStyleName(style);
    panel.add(iconHTML);
    panel.add(new Label(title));

    return panel;
  }

  public void update(ViewerDatabase database) {
    this.database = database;
    updateValidationStatus();
    updateValidationButtons();
    updateValidationInformation();
    updateValidationIndicators();
  }

  private void delete() {
    if (!database.getValidationStatus().equals(ViewerDatabaseValidationStatus.VALIDATION_RUNNING)) {
      SiardService.Util
        .call((Void result) -> SIARDManagerPage.getInstance(database).refreshInstance(database.getUuid()))
        .deleteValidationReport(database.getUuid(), database.getValidatorReportPath());
    }
  }
}
