package com.databasepreservation.common.shared.client.common.visualization.manager.SIARDPanel.navigation;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.common.client.BrowserService;
import com.databasepreservation.common.shared.ViewerConstants;
import com.databasepreservation.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.shared.client.common.NavigationPanel;
import com.databasepreservation.common.shared.client.common.dialogs.CommonDialogs;
import com.databasepreservation.common.shared.client.common.dialogs.Dialogs;
import com.databasepreservation.common.shared.client.common.fields.MetadataField;
import com.databasepreservation.common.shared.client.common.helpers.HelperValidator;
import com.databasepreservation.common.shared.client.common.utils.ApplicationType;
import com.databasepreservation.common.shared.client.common.utils.JavascriptUtils;
import com.databasepreservation.common.shared.client.common.visualization.manager.SIARDPanel.SIARDManagerPage;
import com.databasepreservation.common.shared.client.common.visualization.validation.ValidatorPage;
import com.databasepreservation.common.shared.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.shared.client.tools.HistoryManager;
import com.databasepreservation.common.shared.client.tools.SolrHumanizer;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

import config.i18n.client.ClientMessages;

public class ValidationNavigationPanel {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, ValidationNavigationPanel> instances = new HashMap<>();
  private ViewerDatabase database;
  private String validateAtHumanized = null;
  private Button btnRunValidator;
  private Button btnOpenValidator;
  private Button btnSeeReport;
  private Button btnDeleteReport;
  private HelperValidator validator = null;
  private MetadataField validatedAt = null;
  private MetadataField validationStatus = null;
  private FlowPanel validationIndicators = new FlowPanel();
  private MetadataField version = null;

  public static ValidationNavigationPanel getInstance(ViewerDatabase database, String validateAtHumanized) {
    String databaseUUID = database.getUUID();
    if (instances.get(databaseUUID) == null) {
      instances.put(databaseUUID, new ValidationNavigationPanel(database, validateAtHumanized));
    }
    return instances.get(databaseUUID);
  }

  private ValidationNavigationPanel(ViewerDatabase database, String validateAtHumanized) {
    this.database = database;
    this.validateAtHumanized = validateAtHumanized;
  }

  public NavigationPanel build() {

    // Run validator btn
    validator = new HelperValidator(database.getSIARDPath());
    btnRunValidator = new Button();
    btnRunValidator.setText(messages.SIARDHomePageButtonTextValidateNow());
    btnRunValidator.addStyleName("btn btn-link-info");
    btnRunValidator.addClickHandler(event -> {
      if (database.getSIARDVersion().equals(ViewerConstants.SIARD_V21)) {
        if (ApplicationType.getType().equals(ViewerConstants.DESKTOP)) {
          Dialogs.showValidatorSettings(messages.SIARDValidatorSettings(), messages.basicActionCancel(),
            messages.basicActionConfirm(), validator, new DefaultAsyncCallback<Boolean>() {
              @Override
              public void onSuccess(Boolean result) {
                if (result && validator.getReporterPathFile() != null) {
                  ValidatorPage.clear(database.getUUID());
                  if (validator.getUdtPathFile() == null) {
                    HistoryManager.gotoSIARDValidator(database.getUUID(), validator.getReporterPathFile());
                  } else {
                    HistoryManager.gotoSIARDValidator(database.getUUID(), validator.getReporterPathFile(),
                      validator.getUdtPathFile());
                  }
                }
              }
            });
        } else {
          ValidatorPage.clear(database.getUUID());
          HistoryManager.gotoSIARDValidator(database.getUUID());
        }
      } else {
        Dialogs.showInformationDialog(messages.SIARDValidatorDialogInformationTitle(),
          messages.SIARDValidatorTextForVersionCannotBeValidated(), messages.basicActionUnderstood(), "btn btn-link");
      }
    });

    // Open validator btn
    btnOpenValidator = new Button();
    btnOpenValidator.setText(messages.SIARDHomePageButtonTextOpenValidate());
    btnOpenValidator.addStyleName("btn btn-link-info");
    btnOpenValidator.addClickHandler(event -> {
      if (ApplicationType.getType().equals(ViewerConstants.DESKTOP)) {
        HistoryManager.gotoSIARDValidator(database.getUUID(), validator.getReporterPathFile());
      } else {
        HistoryManager.gotoSIARDValidator(database.getUUID());
      }
    });

    btnOpenValidator.setVisible(ValidatorPage.checkInstance(database.getUUID()));

    // See Report btn
    btnSeeReport = new Button();
    btnSeeReport.setText(messages.SIARDHomePageButtonTextSeeReport());
    btnSeeReport.addStyleName("btn btn-link-info");
    if (ApplicationType.getType().equals(ViewerConstants.DESKTOP)) {
      btnSeeReport.addClickHandler(clickEvent -> {
        JavascriptUtils.showItem(database.getValidatorReportPath());
      });
    }

    // Delete Report btn
    btnDeleteReport = new Button();
    btnDeleteReport.setText(messages.SIARDHomePageButtonTextForDeleteIngested());
    btnDeleteReport.addStyleName("btn btn-link-info");
    btnDeleteReport.addClickHandler(event -> {
      if (!database.getValidationStatus().equals(ViewerDatabase.ValidationStatus.VALIDATION_RUNNING)) {
        CommonDialogs.showConfirmDialog(messages.SIARDHomePageDialogTitleForDelete(),
          messages.SIARDHomePageTextForDeleteSIARDReportValidation(), messages.basicActionCancel(), messages.basicActionConfirm(),
          CommonDialogs.Level.DANGER, "500px", new DefaultAsyncCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
              if (result) {
                delete();
              }
            }
          });
      }
    });

    // buttons
    updateValidationButtons();

    // information
    validatedAt = MetadataField.createInstance(messages.SIARDHomePageLabelForValidatedAt(),
      messages.humanizedTextForSIARDNotValidated());
    version = MetadataField.createInstance(messages.SIARDHomePageLabelForValidationVersion(),
      messages.humanizedTextForSIARDNotValidated());
    updateValidationInformation();

    // Validation Status info
    validationStatus = MetadataField.createInstance(messages.SIARDHomePageLabelForValidationStatus(),
      SolrHumanizer.humanize(database.getValidationStatus()));
    validationStatus.setCSSMetadata(null, "label-field", "value-field");
    updateValidationStatus();

    validatedAt.setCSSMetadata(null, "label-field", "value-field");
    version.setCSSMetadata(null, "label-field", "value-field");

    NavigationPanel validation = NavigationPanel.createInstance(messages.SIARDHomePageOptionsHeaderForValidation());
    validation.addButton(btnRunValidator);
    validation.addButton(btnOpenValidator);
    validation.addButton(btnSeeReport);
    validation.addButton(btnDeleteReport);

    validation.addToInfoPanel(validationStatus);
    validation.addToInfoPanel(validatedAt);
    validation.addToInfoPanel(version);
    validation.addToInfoPanel(validationIndicators);

    return validation;
  }

  private void updateValidationInformation() {
    if (database.getValidationStatus().equals(ViewerDatabase.ValidationStatus.VALIDATION_SUCCESS)
      || database.getValidationStatus().equals(ViewerDatabase.ValidationStatus.VALIDATION_FAILED)) {
      validatedAt.updateText(validateAtHumanized);
      version.updateText(database.getValidatedVersion());
      validatedAt.setVisible(true);
      version.setVisible(true);
      // indicators
      updateValidationIndicators();
      validationIndicators.setVisible(true);
    } else { // NOT_VALIDATE, RUNNING, ERROR
      validationIndicators.setVisible(false);
      validatedAt.setVisible(false);
      version.setVisible(false);
    }
  }

  private void updateValidationStatus() {
    validationStatus.updateText(SolrHumanizer.humanize(database.getValidationStatus()));
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
        btnOpenValidator.setVisible(ValidatorPage.checkInstance(database.getUUID()));
        btnDeleteReport
          .setVisible(database.getValidatorReportPath() != null && !database.getValidatorReportPath().isEmpty());
        break;
      case VALIDATION_RUNNING:
        btnSeeReport.setVisible(false);
        btnRunValidator.setVisible(false);
        btnOpenValidator.setVisible(ValidatorPage.checkInstance(database.getUUID()));
        btnDeleteReport.setVisible(false);
        break;
      case NOT_VALIDATED:
        btnSeeReport.setVisible(false);
        updateRunValidatorButton(messages.SIARDHomePageButtonTextValidateNow());
        btnOpenValidator.setVisible(false);
        btnDeleteReport.setVisible(false);
        break;
      case ERROR:
        btnSeeReport.setVisible(false);
        updateRunValidatorButton(messages.SIARDHomePageButtonTextValidateNow());
        btnOpenValidator.setVisible(ValidatorPage.checkInstance(database.getUUID()));
        btnDeleteReport.setVisible(false);
        break;
    }
  }

  private void updateRunValidatorButton(String msg) {
    if(database.getSIARDPath() != null && !database.getSIARDPath().isEmpty()){
      btnRunValidator.setEnabled(true);
      btnRunValidator.setTitle(null);
    } else {
      btnRunValidator.setEnabled(false);
      btnRunValidator.setTitle(messages.SIARDHomePageTextForRequiredSIARDFile());
    }
    btnRunValidator.setText(msg);
  }

  private void updateValidationIndicators() {
    validationIndicators.clear();
    validationIndicators.addStyleName("validation-indicators");
    GWT.log("ValidationStatus::" + database.getValidationStatus());
    if (database.getValidationStatus().equals(ViewerDatabase.ValidationStatus.VALIDATION_SUCCESS)
      || database.getValidationStatus().equals(ViewerDatabase.ValidationStatus.VALIDATION_FAILED)) {
      Label label = new Label(messages.SIARDHomePageTextForValidationIndicators());
      label.addStyleName("label-field");
      validationIndicators.add(label);
      FlowPanel panel = new FlowPanel();
      panel.addStyleName("validation-indicators");
      if (database.getValidationPassed() != null) {
        panel.add(buildIndicators(database.getValidationPassed(), FontAwesomeIconManager.CHECK, "passed",
          messages.SIARDHomePageTextForValidationIndicatorsSuccess(Integer.parseInt(database.getValidationPassed()))));
      }
      if (database.getValidationErrors() != null) {
        panel.add(buildIndicators(database.getValidationErrors(), FontAwesomeIconManager.TIMES, "errors",
          messages.SIARDHomePageTextForValidationIndicatorsFailed(Integer.parseInt(database.getValidationErrors()))));
      }
      if (database.getValidationWarnings() != null) {
        panel.add(buildIndicators(database.getValidationWarnings(), FontAwesomeIconManager.WARNING, "warnings", messages
          .SIARDHomePageTextForValidationIndicatorsWarnings(Integer.parseInt(database.getValidationWarnings()))));
      }
      if (database.getValidationSkipped() != null) {
        panel.add(buildIndicators(database.getValidationSkipped(), FontAwesomeIconManager.SKIPPED, "skipped",
          messages.SIARDHomePageTextForValidationIndicatorsSkipped(Integer.parseInt(database.getValidationSkipped()))));
      }
      if (panel.getWidgetCount() > 0) {
        validationIndicators.add(panel);
      }
    }
  }

  private FlowPanel buildIndicators(String indicator, String icon, String style, String title) {
    FlowPanel panel = new FlowPanel();
    panel.setStyleName("indicator");
    panel.setTitle(title);
    Label label = new Label(indicator);
    HTML iconHTML = new HTML(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(icon)));
    iconHTML.addStyleName(style);
    panel.add(iconHTML);
    panel.add(label);

    return panel;
  }

  public void update(ViewerDatabase database) {
    this.database = database;

    BrowserService.Util.getInstance().getDateTimeHumanized(database.getValidatedAt(),
      new DefaultAsyncCallback<String>() {
        @Override
        public void onSuccess(String result) {
          validateAtHumanized = result;
          updateValidationStatus();
          updateValidationButtons();
          updateValidationInformation();
          updateValidationIndicators();
        }
      });

  }

  private void delete() {
    if (!database.getValidationStatus().equals(ViewerDatabase.ValidationStatus.VALIDATION_RUNNING)) {
      BrowserService.Util.getInstance().deleteSIARDValidatorReportFile(database.getValidatorReportPath(),
        database.getUUID(), new AsyncCallback<Void>() {
          @Override
          public void onFailure(Throwable caught) {

          }

          @Override
          public void onSuccess(Void result) {
            SIARDManagerPage.getInstance(database).refreshInstance(database.getUUID());
          }
        });
    }

  }
}
