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
  private static Map<String, ValidationNavigationPanel> instances = new HashMap<>();
  private ViewerDatabase database;
  private Button btnRunValidator;
  private Button btnOpenValidator;
  private Button btnSeeReport;
  private Button btnDeleteReport;
  private HelperValidator validator = null;
  private MetadataField validatedAt = null;
  private MetadataField validationStatus = null;
  private FlowPanel validationDetails = new FlowPanel();
  private MetadataField validationWarnings = null;
  private MetadataField version = null;

  public static ValidationNavigationPanel getInstance(ViewerDatabase database) {
    String databaseUUID = database.getUuid();
    if (instances.get(databaseUUID) == null) {
      instances.put(databaseUUID, new ValidationNavigationPanel(database));
    }
    return instances.get(databaseUUID);
  }

  private ValidationNavigationPanel(ViewerDatabase database) {
    this.database = database;
  }

  public NavigationPanel build() {

    // Run validator btn
    validator = new HelperValidator(database.getPath());
    btnRunValidator = new Button();
    btnRunValidator.setText(messages.SIARDHomePageButtonTextValidateNow());
    btnRunValidator.addStyleName("btn btn-outline-primary btn-play");
    btnRunValidator.addClickHandler(event -> {
      if (database.getVersion().equals(ViewerConstants.SIARD_V21)) {
        if (ApplicationType.getType().equals(ViewerConstants.DESKTOP)) {
          Dialogs.showValidatorSettings(messages.SIARDValidatorSettings(), messages.basicActionCancel(),
            messages.basicActionConfirm(), validator, new DefaultAsyncCallback<Boolean>() {
              @Override
              public void onSuccess(Boolean result) {
                if (result && validator.getReporterPathFile() != null) {
                  ValidatorPage.clear(database.getUuid());
                  if (validator.getUdtPathFile() == null) {
                    HistoryManager.gotoSIARDValidator(database.getUuid(), validator.getReporterPathFile(),
                      validator.skipAdditionalChecks());
                  } else {
                    HistoryManager.gotoSIARDValidator(database.getUuid(), validator.getReporterPathFile(),
                      validator.getUdtPathFile(), validator.skipAdditionalChecks());
                  }
                }
              }
            });
        } else {
          ValidatorPage.clear(database.getUuid());
          HistoryManager.gotoSIARDValidator(database.getUuid(), validator.skipAdditionalChecks());
        }
      } else {
        Dialogs.showInformationDialog(messages.SIARDValidatorDialogInformationTitle(),
          messages.SIARDValidatorTextForVersionCannotBeValidated(), messages.basicActionUnderstood(), "btn btn-link");
      }
    });

    // Open validator btn
    btnOpenValidator = new Button();
    btnOpenValidator.setText(messages.SIARDHomePageButtonTextOpenValidate());
    btnOpenValidator.addStyleName("btn btn-outline-primary btn-validate");
    btnOpenValidator.addClickHandler(event -> {
      if (ApplicationType.getType().equals(ViewerConstants.DESKTOP)) {
        HistoryManager.gotoSIARDValidator(database.getUuid(), validator.getReporterPathFile());
      } else {
        HistoryManager.gotoSIARDValidator(database.getUuid(), validator.skipAdditionalChecks());
      }
    });

    btnOpenValidator.setVisible(ValidatorPage.checkInstance(database.getUuid()));

    // See Report btn
    btnSeeReport = new Button();
    btnSeeReport.addStyleName("btn btn-outline-primary");
    if (ApplicationType.getType().equals(ViewerConstants.DESKTOP)) {
      btnSeeReport.setText(messages.SIARDHomePageButtonTextForOpenReport());
      btnSeeReport.addStyleName("btn-open");
      btnSeeReport.addClickHandler(clickEvent -> {
        JavascriptUtils.showItem(database.getValidatorReportPath());
      });
    } else {
      btnSeeReport.setText(messages.SIARDHomePageButtonTextForDownloadReport());
      btnSeeReport.addStyleName("btn-download");
      btnSeeReport.addClickHandler(clickEvent -> {
        SafeUri downloadUri = RestUtils.createFileResourceDownloadValidationReportUri(database.getUuid());
        Window.Location.assign(downloadUri.asString());
      });
    }

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

    // information
    validatedAt = MetadataField.createInstance(messages.SIARDHomePageLabelForValidatedAt(),
      messages.humanizedTextForSIARDNotValidated());
    version = MetadataField.createInstance(messages.SIARDHomePageLabelForValidateBy(),
      messages.humanizedTextForSIARDNotValidated());
    validationWarnings = MetadataField.createInstance(messages.SIARDHomePageLabelForValidationWarnings(),
      messages.humanizedTextForSIARDNotValidated());
    updateValidationInformation();
    validationWarnings.setVisible(false);

    // Validation Status info
    validationStatus = MetadataField.createInstance(messages.SIARDHomePageLabelForValidationStatus(),
      LabelUtils.getSIARDValidationStatus(database.getValidationStatus()));
    validationStatus.setCSS(null, "label-field", "value-field");

    // updateValidationStatus();

    validatedAt.setCSS(null, "label-field", "value-field");
    version.setCSS(null, "label-field", "value-field");
    validationWarnings.setCSS(null, "label-field", "value-field");

    NavigationPanel validation = NavigationPanel.createInstance(messages.SIARDHomePageOptionsHeaderForValidation());
    validation.addToDescriptionPanel(messages.SIARDHomePageOptionsDescriptionForSIARD());
    validation.addButton(CommonClientUtils.wrapOnDiv("btn-item", btnRunValidator));
    validation.addButton(CommonClientUtils.wrapOnDiv("btn-item", btnOpenValidator));
    validation.addButton(CommonClientUtils.wrapOnDiv("btn-item", btnSeeReport));
    validation.addButton(CommonClientUtils.wrapOnDiv("btn-item", btnDeleteReport));

    // buttons
    updateValidationButtons();

    validation.addToInfoPanel(validationStatus);
    validation.addToInfoPanel(validationDetails);
    validation.addToInfoPanel(validationWarnings);
    validation.addToInfoPanel(validatedAt);
    validation.addToInfoPanel(version);

    return validation;
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
      case VALIDATION_RUNNING:
        validationStatus.getMetadataValue().setStyleName("label-info");
        break;
      case ERROR:
        validationStatus.getMetadataValue().setStyleName("label-danger label-error");
        break;
      default:
        validationStatus.getMetadataValue().setStyleName("label-info");
        break;
    }
  }

  private void updateValidationButtons() {
    switch (database.getValidationStatus()) {
      case VALIDATION_SUCCESS:
      case VALIDATION_FAILED:
        btnSeeReport.setVisible(true);
        updateRunValidatorButton(messages.SIARDHomePageButtonTextRunValidationAgain());
        handleBtnOpenVisibility();
        btnDeleteReport
          .setVisible(database.getValidatorReportPath() != null && !database.getValidatorReportPath().isEmpty());
        break;
      case VALIDATION_RUNNING:
        btnSeeReport.setVisible(false);
        btnRunValidator.setVisible(false);
        handleBtnOpenVisibility();
        btnDeleteReport.setVisible(false);
        break;
      case NOT_VALIDATED:
        btnSeeReport.setVisible(false);
        updateRunValidatorButton(messages.SIARDHomePageButtonTextValidateNow());
        btnOpenValidator.setVisible(false);
        btnOpenValidator.getElement().getParentElement().addClassName("btn-item-hidden");
        btnDeleteReport.setVisible(false);
        break;
      case ERROR:
        btnSeeReport.setVisible(false);
        updateRunValidatorButton(messages.SIARDHomePageButtonTextValidateNow());
        handleBtnOpenVisibility();
        btnDeleteReport.setVisible(false);
        break;
    }
  }

  private void handleBtnOpenVisibility() {
    if(ValidatorPage.checkInstance(database.getUuid())){
      btnOpenValidator.setVisible(true);
      btnOpenValidator.getParent().removeStyleName("btn-item-hidden");
    } else {
      btnOpenValidator.setVisible(false);
      btnOpenValidator.getParent().addStyleName("btn-item-hidden");
    }
  }

  private void updateRunValidatorButton(String msg) {
    if (database.getPath() != null && !database.getPath().isEmpty()) {
      btnRunValidator.setEnabled(true);
      btnRunValidator.setTitle(null);
    } else {
      btnRunValidator.setEnabled(false);
      btnRunValidator.setTitle(messages.SIARDHomePageTextForRequiredSIARDFile());
    }
    btnRunValidator.setText(msg);
  }

  private void updateValidationIndicators() {
    validationDetails.clear();
    validationDetails.addStyleName("validation-indicators");
    GWT.log("ValidationStatus::" + database.getValidationStatus());
    if (database.getValidationStatus().equals(ViewerDatabaseValidationStatus.VALIDATION_SUCCESS)
      || database.getValidationStatus().equals(ViewerDatabaseValidationStatus.VALIDATION_FAILED)) {
      Label label = new Label(messages.SIARDHomePageLabelForValidationDetails());
      label.addStyleName("label-field");
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
      SiardService.Util.call((Void result) -> {
        SIARDManagerPage.getInstance(database).refreshInstance(database.getUuid());
      }).deleteValidationReport(database.getUuid(), database.getValidatorReportPath());
    }
  }
}
