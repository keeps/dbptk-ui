package com.databasepreservation.main.desktop.client.dbptk.validator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.ValidationProgressData;
import com.databasepreservation.main.common.shared.ViewerConstants;
import com.databasepreservation.main.common.shared.ViewerStructure.IsIndexed;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerValidator;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbItem;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.main.common.shared.client.common.LoadingDiv;
import com.databasepreservation.main.common.shared.client.common.utils.ApplicationType;
import com.databasepreservation.main.common.shared.client.common.utils.JavascriptUtils;
import com.databasepreservation.main.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.main.common.shared.client.tools.SolrHumanizer;
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
  private boolean isRenderer = false;
  private Integer countErrors = 0;
  private Integer countPassed = 0;
  private Integer countSkipped = 0;

  private Timer autoUpdateTimer = new Timer() {
    @Override
    public void run() {
      if (!isRenderer) {
        ValidatorPage.this.update();
      }
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

    title.setText("messages.SIARDValidator()");
    loading.setVisible(true);

    BrowserService.Util.getInstance().retrieve(databaseUUID, ViewerDatabase.class.getName(), databaseUUID,
      new DefaultAsyncCallback<IsIndexed>() {
        @Override
        public void onSuccess(IsIndexed result) {
          database = (ViewerDatabase) result;

          List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forSIARDValidatorPage(database.getUUID(),
            database.getMetadata().getName());
          BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
          content.clear();

          initProgress();
        }
      });
  }

  private void initProgress() {
    BrowserService.Util.getInstance().getValidationProgressData(databaseUUID,
      new DefaultAsyncCallback<ValidationProgressData>() {
        @Override
        public void onSuccess(ValidationProgressData result) {
          result.reset();
          stopUpdating();
          loading.setVisible(true);
          populateValidationInfo(false);
          autoUpdateTimer.scheduleRepeating(1000);
          runValidator();
        }
      });
  }

  private void runValidator() {
    BrowserService.Util.getInstance().validateSIARD(database.getUUID(), database.getSIARDPath(), reporterPath, udtPath,
      new DefaultAsyncCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean result) {
          GWT.log("Result: " + result);
          buildValidationControl(true);
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
    isRenderer = true;
//    content.clear();
//    resetCount();

    List<ViewerValidator> componentList = validationProgressData.getComponent();
    for (ViewerValidator component : componentList) {
      FlowPanel componentPanel = new FlowPanel();
      componentPanel.addStyleName("validation-panel title");
      componentPanel.add(new Label(component.getComponentID()));
      componentPanel.add(new Label(component.getComponentName()));
      componentPanel.add(new Label(component.getComponentStatus()));
      content.add(componentPanel);

      for(Map.Entry<String, List<String>> entry : component.getPathListBeingValidated().entrySet()){
        Label messageLabel = new Label(entry.getKey());
        messageLabel.addStyleName("validation-panel title");
        content.add(messageLabel);
        for (String path : entry.getValue()) {
          Label pathLabel = new Label(path);
          pathLabel.addStyleName("validation-panel requirement");
          content.add(pathLabel);
        }
      }

      for (ViewerValidator.Requirement requirement : component.getRequirementList()) {
        FlowPanel requirementPanel = new FlowPanel();
        FlowPanel requirementTitlePanel = new FlowPanel();
        requirementPanel.addStyleName("validation-panel requirement");
        requirementTitlePanel.addStyleName("title");
        requirementTitlePanel.add(new Label(requirement.getRequirementID()));
        requirementTitlePanel.add(new Label(messages.validationRequirements(requirement.getRequirementID())));
        requirementPanel.add(requirementTitlePanel);
        Label status = new Label(requirement.getRequirementStatus());
        switch (requirement.getRequirementStatus()) {
          case "OK":
            countPassed++;
            status.addStyleName("label-success");
            break;
          case "ERROR":
            countErrors++;
            status.addStyleName("label-danger");
            break;
          default:
            countSkipped++;
            status.addStyleName("label-default");
        }
        requirementPanel.add(status);
        content.add(requirementPanel);

        for(Map.Entry<String, List<String>> entry : requirement.getPathListBeingValidated().entrySet()){
          for (String path : entry.getValue()) {
            Label pathLabel = new Label(path);
            pathLabel.addStyleName("validation-panel requirement");
            content.add(pathLabel);
          }
        }
//        content.getElement().setScrollTop(content.getElement().getScrollHeight());
      }
    }

    isRenderer = false;
    if (validationProgressData.isFinished()) {
      stopUpdating();
    }
  }

  private String updateStatus(Label statusLabel) {
    String statusText = messages.humanizedTextForSIARDNotValidated();
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

  private void resetCount() {
    countPassed = 0;
    countErrors = 0;
    countSkipped = 0;
  }

  private void populateValidationInfo(Boolean enable) {
    validationInformation.clear();
    FlowPanel left = new FlowPanel();

    left.add(validationInfoBuilder(messages.managePageTableHeaderTextForDatabaseName(), database.getMetadata().getName(), new Label()));
    left.add(validationInfoBuilder(messages.numberOfValidationError(), countErrors.toString(), new Label()));
    left.add(validationInfoBuilder(messages.numberOfValidationsPassed(), countPassed.toString(), new Label()));
    left.add(validationInfoBuilder(messages.numberOfValidationsSkipped(), countSkipped.toString(), new Label()));
    Label statusLabel = new Label();
    left.add(validationInfoBuilder(messages.managePageTableHeaderTextForDatabaseStatus(), updateStatus(statusLabel), statusLabel)); //TODO

    FlowPanel reportPanel = new FlowPanel();
    reportPanel.addStyleName("validation-info-panel");
    Label reportLabel = new Label(messages.reporterFile());
    reportLabel.addStyleName("title");
    Button reporterButton = new Button();
    reporterButton.setText(messages.basicActionBrowse());
    reporterButton.addStyleName("btn btn-link-info");
    reporterButton.addClickHandler(event -> {
      if (ApplicationType.getType().equals(ViewerConstants.ELECTRON)) {
        JavascriptUtils.showItem(reporterPath);
      }
    });
    reporterButton.setEnabled(enable);
    reportPanel.add(reportLabel);
    reportPanel.add(reporterButton);

    left.add(reportPanel);

    validationInformation.add(left);
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

  private void buildValidationControl(boolean enable){
    validationControl.clear();
    Button runAgainBtn = new Button();
    runAgainBtn.setText(messages.runAgain());
    runAgainBtn.addStyleName("btn btn-link-info");
    runAgainBtn.addClickHandler(event ->{
      clear(databaseUUID);
      initProgress();
    });
    validationControl.add(runAgainBtn);
  }

  public void clear(String uuid) {
    content.clear();
    instances.put(uuid, null);
  }

  @Override
  protected void onDetach() {
    stopUpdating();
    super.onDetach();
  }

  private void stopUpdating() {
    populateValidationInfo(true);
    loading.setVisible(false);
    if (autoUpdateTimer != null) {
      autoUpdateTimer.cancel();
    }
  }

}
