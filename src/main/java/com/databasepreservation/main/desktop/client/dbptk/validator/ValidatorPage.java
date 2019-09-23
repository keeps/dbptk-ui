package com.databasepreservation.main.desktop.client.dbptk.validator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.Constants;
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
import com.databasepreservation.main.common.shared.client.tools.FontAwesomeIconManager;
import com.databasepreservation.main.common.shared.client.tools.HistoryManager;
import com.databasepreservation.main.common.shared.client.widgets.Toast;
import com.databasepreservation.main.desktop.client.dbptk.SIARDMainPage;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
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
  private FlowPanel tailIndicator = new FlowPanel();

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
  FocusPanel contentScroll;

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField
  LoadingDiv loading;

  @UiField
  Button btnCancel, btnRunAgain;

  private ValidatorPage(String databaseUUID, String reporterPath, String udtPath) {
    this.databaseUUID = databaseUUID;
    this.reporterPath = URL.decodePathSegment(reporterPath);
    if (udtPath != null) {
      this.udtPath = URL.decodePathSegment(udtPath);
    }
    initWidget(binder.createAndBindUi(this));

    title.setText(messages.validatorPageTextForTitle());
    loading.setVisible(true);
    tailIndicator.setStyleName("tail tail-on");

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
          autoUpdateTimer.scheduleRepeating(250);
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
          //TODO
        }

        @Override
        public void onFailure(Throwable caught) {
          stopUpdating(caught.getMessage());
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
          if (result.isFinished() && result.getRequirementsList().size() <= lastPosition) {
            stopUpdating(messages.validatorPageTextForToast());
          }
        }
      });
  }

  private void update(ValidationProgressData validationProgressData) {
    List<ValidationProgressData.Requirement> requirementList = validationProgressData.getRequirementsList(lastPosition);
    lastPosition += requirementList.size();
    for (ValidationProgressData.Requirement requirement : requirementList) {

      if (requirement.getType().equals(ValidationProgressData.Requirement.Type.REQUIREMENT)) {
        FlowPanel panel = new FlowPanel();
        panel.addStyleName("validation-panel title");
        panel.add(new Label(requirement.getID()));
        panel.add(new Label(requirement.getMessage()));
        panel.setTitle(requirement.getType().name());
        content.add(panel);
      }

      if (requirement.getType().equals(ValidationProgressData.Requirement.Type.MESSAGE)) {
        FlowPanel panel = new FlowPanel();
        panel.addStyleName("validation-panel requirement");
        Label message = new Label(requirement.getMessage());
        panel.add(message);
        Label status = buildStatus(requirement.getStatus());
        panel.add(status);
        panel.setTitle(requirement.getType().name());
        content.add(panel);
      }

      if (requirement.getType().equals(ValidationProgressData.Requirement.Type.PATH)) {
        Label pathLabel = new Label("Validation running on path: " + requirement.getMessage()); // TODO
        pathLabel.addStyleName("validation-panel requirement path text-muted");
        pathLabel.setTitle(requirement.getType().name());
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
        panel.setTitle(requirement.getType().name());
        content.add(panel);
      }

      if (stickToBottom) {
        contentScroll.getElement().setScrollTop(contentScroll.getElement().getScrollHeight());
      }
    }
  }

  private Label buildStatus(String status) {
    Label statusLabel = new Label(status);
    switch (status) {
      case "OK":
        countPassed++;
        statusLabel.setStyleName("label-success");
        break;
      case "ERROR":
        countErrors++;
        statusLabel.setStyleName("label-danger");
        statusLabel.setTitle(messages.validatorPageTextForErrorDetails());
        break;
      case "SKIPPED":
        countSkipped++;
        statusLabel.setStyleName("label-default");
        break;
      default:
        statusLabel.setStyleName("label-info");
    }
    statusLabel.addStyleName("gwt-Label validation-status");
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
    btnRunAgain.setEnabled(enable);
    FlowPanel left = new FlowPanel();

    left.add(validationInfoBuilder(messages.managePageTableHeaderTextForDatabaseName(),database.getMetadata().getName(), new Label(), true));
    left.add(validationInfoBuilder(messages.numberOfValidationError(), countErrors.toString(), new Label(), enable));
    left.add(validationInfoBuilder(messages.numberOfValidationsPassed(), countPassed.toString(), new Label(), enable));
    left.add(validationInfoBuilder(messages.numberOfValidationsSkipped(), countSkipped.toString(), new Label(), enable));
    Label statusLabel = new Label();
    left.add(validationInfoBuilder(messages.managePageTableHeaderTextForDatabaseStatus(), updateStatus(statusLabel),
      statusLabel, true));

    FlowPanel reportPanel = new FlowPanel();
    reportPanel.addStyleName("validation-info-panel");
    Label reportLabel = new Label(messages.reportFile());
    reportLabel.addStyleName("title");
    Button reportButton = new Button();
    reportButton.setText(messages.basicActionOpen());
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

    FlowPanel stickToBottomPannel = new FlowPanel();
    FocusPanel stickFocus = new FocusPanel();
    FlowPanel stickToBottomInner = new FlowPanel();
    Label tailLabel = new Label(messages.validatorPageTextForStick());
    stickToBottomInner.add(tailLabel);
    stickToBottomInner.add(tailIndicator);
    stickToBottomInner.addStyleName("stick-bottom-log");
    stickFocus.addClickHandler(event -> {
      stickToBottom = !stickToBottom;
      if(stickToBottom){
        contentScroll.getElement().setScrollTop(contentScroll.getElement().getScrollHeight());
        tailIndicator.setStyleName("tail tail-on");
      } else {
        tailIndicator.setStyleName("tail tail-off");
      }
    });
    stickFocus.add(stickToBottomInner);
    stickToBottomPannel.add(stickFocus);
    validationInformation.add(stickToBottomPannel);
  }

  private FlowPanel validationInfoBuilder(String key, String value, Label valueLabel, Boolean loading) {
    FlowPanel panel = new FlowPanel();
    panel.addStyleName("validation-info-panel");
    Label keyLabel = new Label(key);
    keyLabel.addStyleName("title");
    panel.add(keyLabel);
    if(!loading){
      String iconTag = FontAwesomeIconManager.getTag(FontAwesomeIconManager.COG);
      HTML html = new HTML(SafeHtmlUtils.fromSafeConstant(iconTag));
      html.getElement().getFirstChildElement().addClassName("fa-"+FontAwesomeIconManager.SPIN);
      html.addStyleName("text-muted");
      panel.add(html);
    } else {
      valueLabel.setText(value);
      panel.add(valueLabel);
    }

    return panel;
  }

  private void stopUpdating(String message) {
    Toast.showInfo(messages.validatorPageTextForTitle(), message);
    SIARDMainPage.getInstance(database.getUUID()).refreshInstance(database.getUUID());
    instances.remove(databaseUUID);
    populateValidationInfo(true);
    loading.setVisible(false);
    if (autoUpdateTimer != null) {
      autoUpdateTimer.cancel();
    }
  }

  @Override
  protected void onDetach() {
    SIARDMainPage.getInstance(database.getUUID()).refreshInstance(database.getUUID());
    loading.setVisible(false);
    super.onDetach();
  }

  @Override
  protected void onAttach() {
    loading.setVisible(true);
    super.onAttach();
  }

  @UiHandler("btnCancel")
  void setBtnCancelHandler(ClickEvent e){
    HistoryManager.gotoSIARDInfo(databaseUUID);
  }

  @UiHandler("btnRunAgain")
  void setBtnRunAgainHandler(ClickEvent e){
    BrowserService.Util.getInstance().clearValidationProgressData(databaseUUID, new DefaultAsyncCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        initProgress();
      }
    });
  }

  @UiHandler("contentScroll")
  void setContentScrollHandler(MouseWheelEvent e){
    if(e.isNorth()){
      stickToBottom = false;
      tailIndicator.setStyleName("tail tail-off");
    }
  }
}
