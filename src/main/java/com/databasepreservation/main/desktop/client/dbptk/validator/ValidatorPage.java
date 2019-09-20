package com.databasepreservation.main.desktop.client.dbptk.validator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.ValidationProgressData;
import com.databasepreservation.main.common.shared.ViewerConstants;
import com.databasepreservation.main.common.shared.ViewerStructure.IsIndexed;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbItem;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.main.common.shared.client.common.LoadingDiv;
import com.databasepreservation.main.common.shared.client.common.utils.ApplicationType;
import com.databasepreservation.main.common.shared.client.common.utils.JavascriptUtils;
import com.databasepreservation.main.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.main.desktop.client.dbptk.SIARDMainPage;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ValidatorPage extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, ValidatorPage> instances = new HashMap<>();
  private String databaseUUID;
  private String reporterPath;
  private String udtPath = null;
  private ViewerDatabase database;
  private int lastPosition = 0;
  private Integer countErrors = 0;
  private Integer countPassed = 0;
  private Integer countSkipped = 0;
  private Boolean stickToBottom = true;

  private Timer autoUpdateTimer = new Timer() {
    @Override
    public void run() {
      ValidatorPage.this.update();
    }
  };

  interface ValidatorUiBinder extends UiBinder<Widget, ValidatorPage> {
  }

  private static ValidatorUiBinder binder = GWT.create(ValidatorUiBinder.class);

  public static ValidatorPage getInstance(String databaseUUID, String reporterPath) {
    return ValidatorPage.getInstance(databaseUUID, reporterPath, null);
  }

  public static ValidatorPage getInstance(String databaseUUID, String reporterPath, String udtPath) {
    if (instances.get(databaseUUID) == null) {
      GWT.log("NEW");
      ValidatorPage instance = new ValidatorPage(databaseUUID, reporterPath, udtPath);
      instances.put(databaseUUID, instance);
    }

    return instances.get(databaseUUID);
  }

  @UiField
  FlowPanel container, content, validationInformation, validationControl;

  @UiField
  Label title;

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField
  LoadingDiv loading;

  private ValidatorPage(String databaseUUID, String reporterPath, String udtPath) {
    this.databaseUUID = databaseUUID;
    this.reporterPath = URL.decodePathSegment(reporterPath);
    if (udtPath != null) {
      this.udtPath = URL.decodePathSegment(udtPath);
    }
    initWidget(binder.createAndBindUi(this));

    title.setText(messages.validatorPageTextForTitle());
    loading.setVisible(true);

    BrowserService.Util.getInstance().retrieve(databaseUUID, ViewerDatabase.class.getName(), databaseUUID,
      new DefaultAsyncCallback<IsIndexed>() {
        @Override
        public void onSuccess(IsIndexed result) {
          database = (ViewerDatabase) result;

          List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forSIARDValidatorPage(database.getUUID(),
            database.getMetadata().getName());
          BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);

          if (database.getValidationStatus() != ViewerDatabase.ValidationStatus.VALIDATION_RUNNING) {
            populateValidationInfo(false);
            BrowserService.Util.getInstance().clearValidationProgressData(databaseUUID,
              new DefaultAsyncCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                  content.clear();
                  initProgress();
                }
              });
          } else {
            populateValidationInfo(false);
          }
        }
      });
  }

  private void initProgress() {
    BrowserService.Util.getInstance().getValidationProgressData(databaseUUID,
      new DefaultAsyncCallback<ValidationProgressData>() {
        @Override
        public void onSuccess(ValidationProgressData result) {
          resetInfos();
          populateValidationInfo(false);
          loading.setVisible(true);
          autoUpdateTimer.scheduleRepeating(100);
          autoUpdateTimer.run();
          runValidator();
        }
      });
  }

  private void runValidator() {
    BrowserService.Util.getInstance().validateSIARD(database.getUUID(), database.getSIARDPath(), reporterPath, udtPath,
      new DefaultAsyncCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean result) {
          SIARDMainPage.getInstance(database.getUUID()).refreshInstance(database.getUUID());
          stopUpdating();
        }

        @Override
        public void onFailure(Throwable caught) {
          stopUpdating();
          super.onFailure(caught);
        }
      });
  }

  private void update() {
    BrowserService.Util.getInstance().getValidationProgressData(databaseUUID,
      new DefaultAsyncCallback<ValidationProgressData>() {
        @Override
        public void onSuccess(ValidationProgressData result) {
          update(result);
        }
      });
  }

  private void update(ValidationProgressData validationProgressData) {
    SIARDMainPage.getInstance(database.getUUID()).refreshInstance(database.getUUID());
    List<ValidationProgressData.Requirement> requirementList = validationProgressData.getRequirementsList(lastPosition);
    lastPosition += requirementList.size();
    for (ValidationProgressData.Requirement requirement : requirementList) {

      if (requirement.getType().equals(ValidationProgressData.Requirement.Type.REQUIREMENT)) {
        FlowPanel panel = new FlowPanel();
        panel.addStyleName("validation-panel title");
        panel.add(new Label(requirement.getID()));
        panel.add(new Label(requirement.getMessage()));
        content.add(panel);
      }

      if (requirement.getType().equals(ValidationProgressData.Requirement.Type.MESSAGE)) {
        FlowPanel panel = new FlowPanel();
        panel.addStyleName("validation-panel requirement");
        Label message = new Label(requirement.getMessage());
        panel.add(message);
        Label status = buildStatus(requirement.getStatus());
        panel.add(status);
        content.add(panel);
      }

      if (requirement.getType().equals(ValidationProgressData.Requirement.Type.PATH)) {
        Label pathLabel = new Label("Validation running on path: " + requirement.getMessage()); // TODO
        pathLabel.addStyleName("validation-panel requirement path text-muted");
        content.add(pathLabel);
      }

      if (requirement.getType().equals(ValidationProgressData.Requirement.Type.SUB_REQUIREMENT)) {
        FlowPanel panel = new FlowPanel();
        FlowPanel panelTitle = new FlowPanel();
        panel.addStyleName("validation-panel requirement");
        panelTitle.addStyleName("title");
        panelTitle.add(new Label(requirement.getID()));
        Label description = new Label(messages.validationRequirements(requirement.getID()));
        description.addStyleName("description text-muted");
        panelTitle.add(description);
        panel.add(panelTitle);
        Label status = buildStatus(requirement.getStatus());
        panel.add(status);
        content.add(panel);
      }

      if (stickToBottom) {
        content.getElement().setScrollTop(content.getElement().getScrollHeight());
      }

    }

    if (validationProgressData.isFinished()) {
      stopUpdating();
    }
  }

  private Label buildStatus(String status) {
    Label statusLabel = new Label(status);
    switch (status) {
      case "OK":
        countPassed++;
        statusLabel.addStyleName("label-success");
        break;
      case "ERROR":
        countErrors++;
        statusLabel.addStyleName("label-danger");
        break;
      case "SKIPPED":
        countSkipped++;
        statusLabel.addStyleName("label-default");
      case "PASSED":
      case "START":
      case "FINISH":
        statusLabel.addStyleName("label-info");
      default:
        statusLabel.addStyleName("label-danger");
    }
    statusLabel.addStyleName("validation-status");
    return statusLabel;
  }

  private String updateStatus(Label statusLabel) {
    String statusText = messages.humanizedTextForSIARDValidationRunning();
    statusLabel.setStyleName("label-info");
    if (countErrors != 0) {
      statusText = messages.humanizedTextForSIARDValidationFailed();
      statusLabel.setStyleName("label-danger");
    } else if (countPassed != 0 || countSkipped != 0) {
      statusText = messages.humanizedTextForSIARDValidationSuccess();
      statusLabel.setStyleName("label-success");
    }
    return statusText;
  }

  private void resetInfos() {
    lastPosition = 0;
    content.clear();
    countPassed = 0;
    countErrors = 0;
    countSkipped = 0;
    populateValidationInfo(false);

  }

  private void populateValidationInfo(Boolean enable) {
    validationInformation.clear();
    FlowPanel left = new FlowPanel();

    left.add(validationInfoBuilder(messages.managePageTableHeaderTextForDatabaseName(),
      database.getMetadata().getName(), new Label()));
    left.add(validationInfoBuilder(messages.numberOfValidationError(), countErrors.toString(), new Label()));
    left.add(validationInfoBuilder(messages.numberOfValidationsPassed(), countPassed.toString(), new Label()));
    left.add(validationInfoBuilder(messages.numberOfValidationsSkipped(), countSkipped.toString(), new Label()));
    Label statusLabel = new Label();
    left.add(validationInfoBuilder(messages.managePageTableHeaderTextForDatabaseStatus(), updateStatus(statusLabel),
      statusLabel));

    FlowPanel reportPanel = new FlowPanel();
    reportPanel.addStyleName("validation-info-panel");
    Label reportLabel = new Label(messages.reportFile());
    reportLabel.addStyleName("title");
    Button reportButton = new Button();
    reportButton.setText(messages.basicActionBrowse());
    reportButton.addStyleName("btn btn-link-info");
    reportButton.addClickHandler(event -> {
      if (ApplicationType.getType().equals(ViewerConstants.ELECTRON)) {
        JavascriptUtils.showItem(reporterPath);
      }
    });
    reportButton.setEnabled(enable);
    reportPanel.add(reportLabel);
    reportPanel.add(reportButton);

    left.add(reportPanel);

    validationInformation.add(left);

    validationControl.clear();
    Button stickToBottomBtn = new Button();
    stickToBottomBtn.setText(messages.validatorPageTextForStick());
    stickToBottomBtn.addStyleName("btn btn-link-info");
    stickToBottomBtn.addClickHandler(event -> {
      stickToBottom = !stickToBottom;
    });

    Button runAgainBtn = new Button();
    runAgainBtn.setText(messages.SIARDHomePageButtonTextRunValidationAgain());
    runAgainBtn.addStyleName("btn btn-link-info");
    runAgainBtn.setEnabled(enable);
    runAgainBtn.addClickHandler(event -> {
      BrowserService.Util.getInstance().clearValidationProgressData(databaseUUID, new DefaultAsyncCallback<Void>() {
        @Override
        public void onSuccess(Void result) {
          initProgress();
        }
      });
    });
    validationControl.add(stickToBottomBtn);
    validationControl.add(runAgainBtn);
  }

  private FlowPanel validationInfoBuilder(String key, String value, Label valueLabel) {
    FlowPanel panel = new FlowPanel();
    panel.addStyleName("validation-info-panel");
    Label keyLabel = new Label(key);
    keyLabel.addStyleName("title");
    valueLabel.setText(value);
    panel.add(keyLabel);
    panel.add(valueLabel);

    return panel;
  }

  private void stopUpdating() {
    instances.remove(databaseUUID);
    populateValidationInfo(true);
    loading.setVisible(false);
    if (autoUpdateTimer != null) {
      autoUpdateTimer.cancel();
    }
  }

  @Override
  protected void onDetach() {
    loading.setVisible(false);
    super.onDetach();
  }

  @Override
  protected void onAttach() {
    loading.setVisible(true);
    super.onAttach();
  }
}
