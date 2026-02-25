package com.databasepreservation.common.client.common.visualization.browse.configuration;

import java.util.List;

import com.databasepreservation.common.api.v1.utils.JobResponse;
import com.databasepreservation.common.client.ObserverManager;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.utils.html.LabelUtils;
import com.databasepreservation.common.client.configuration.observer.ICollectionStatusObserver;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerJob;
import com.databasepreservation.common.client.models.structure.ViewerJobStatus;
import com.databasepreservation.common.client.services.CollectionService;
import com.databasepreservation.common.client.services.JobService;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.widgets.Alert;
import com.databasepreservation.common.client.widgets.Toast;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ConfigurationStatusPanel extends Composite implements ICollectionStatusObserver {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField(provided = true)
  Alert alertPanel;

  @UiField
  FlowPanel initialStatePanel;
  @UiField
  Label messageLabel;
  @UiField
  Button btnApplyConfiguration;
  @UiField
  Button btnGoToJobs;

  @UiField
  FlowPanel progressPanel;
  @UiField
  HTMLPanel progressBar;
  @UiField
  Label progressText;
  @UiField
  HTML statusBadge;

  private Timer progressTimer;
  private boolean jobFinishedSuccessfully = false;
  private CollectionStatus collectionStatus;
  private ViewerDatabase database;

  interface ConfigurationStatusPanelUiBinder extends UiBinder<Widget, ConfigurationStatusPanel> {
  }

  private static ConfigurationStatusPanelUiBinder binder = GWT.create(ConfigurationStatusPanelUiBinder.class);

  public ConfigurationStatusPanel() {
    alertPanel = new Alert(Alert.MessageAlertType.INFO, "", FontAwesomeIconManager.DATABASE_INFORMATION);

    initWidget(binder.createAndBindUi(this));

    messageLabel.setText(messages.configurationStatusPanelLabelForTitle());

    alertPanel.setVisible(false);
    toggleProgressMode(false);

    ObserverManager.getCollectionObserver().addObserver(this);
    initHandlers();
  }

  public void setDatabase(ViewerDatabase database) {
    this.database = database;
    refreshStatusFromServer();
  }

  @Override
  protected void onDetach() {
    stopPolling();
    super.onDetach();
  }

  private void refreshStatusFromServer() {
    if (database == null)
      return;

    CollectionService.Util.call((List<CollectionStatus> statusList) -> {
      if (statusList != null && !statusList.isEmpty()) {
        updateCollection(statusList.get(0));
      }
    }).getCollectionConfiguration(database.getUuid(), database.getUuid());
  }

  private void initHandlers() {
    btnApplyConfiguration.addClickHandler(clickEvent -> {
      if (collectionStatus != null) {
        btnApplyConfiguration.setEnabled(false);
        collectionStatus.setNeedsToBeProcessed(false);

        CollectionService.Util.call((Boolean updateSuccess) -> {
          runJob();
        }, errorMessage -> {
          btnApplyConfiguration.setEnabled(true);
          Dialogs.showErrors("Error", errorMessage, messages.basicActionClose());
        }).updateCollectionConfiguration(database.getUuid(), database.getUuid(), collectionStatus);
      }
    });

    btnGoToJobs.addClickHandler(clickEvent -> {
      HistoryManager.gotoJobs();
    });
  }

  private void runJob() {
    CollectionService.Util.call((JobResponse response) -> {
      jobFinishedSuccessfully = false;
      startPolling(response.getJobId());
      btnGoToJobs.setVisible(true);
    }, errorMessage -> {
      btnApplyConfiguration.setEnabled(true);
      Dialogs.showErrors("Error", errorMessage, messages.basicActionClose());
    }).run(database.getUuid(), database.getUuid(), null);
  }

  private void startPolling(String jobUUID) {
    toggleProgressMode(true);
    updateProgressVisuals(null); // Reset visual

    progressTimer = new Timer() {
      @Override
      public void run() {
        checkJobStatus(jobUUID);
      }
    };
    progressTimer.scheduleRepeating(100);
  }

  private void stopPolling() {
    if (progressTimer != null) {
      progressTimer.cancel();
      progressTimer = null;
    }
  }

  private void checkJobStatus(String jobUUID) {
    JobService.Util.call((ViewerJob job) -> {
      if (job == null)
        return;

      updateProgressVisuals(job);
      ViewerJobStatus status = job.getStatus();

      if (status == ViewerJobStatus.COMPLETED || status == ViewerJobStatus.FAILED
        || status == ViewerJobStatus.STOPPED) {
        stopPolling();

        if (status == ViewerJobStatus.COMPLETED) {
          handleJobSuccess();
        } else {
          handleJobFailure(job);
        }
      }

    }, errorMessage -> {
      //
    }).retrieve(jobUUID);
  }

  private void handleJobSuccess() {
    jobFinishedSuccessfully = true;
    Toast.showInfo(messages.advancedConfigurationLabelForDataTransformation(), "Processing Completed");

    progressBar.removeStyleName("active");
    progressText.setText("Completed (100%)");
    statusBadge.setHTML(LabelUtils.getJobStatus(ViewerJobStatus.COMPLETED));

    refreshStatusFromServer();

    Timer resetTimer = new Timer() {
      @Override
      public void run() {
        jobFinishedSuccessfully = false;
        updateVisualState();
      }
    };
    resetTimer.schedule(5000);
  }

  private void handleJobFailure(ViewerJob job) {
    jobFinishedSuccessfully = false;
    String errorDetails = job.getExitDescription() != null ? job.getExitDescription() : "Job Failed";
    Dialogs.showErrors("Job Failed", errorDetails, messages.basicActionClose());

    toggleProgressMode(false);
    btnApplyConfiguration.setEnabled(true);
  }

  private void updateProgressVisuals(ViewerJob job) {
    if (job == null) {
      progressBar.getElement().getStyle().setWidth(0, com.google.gwt.dom.client.Style.Unit.PCT);
      progressText.setText("Initializing...");
      statusBadge.setHTML("");
      return;
    }

    int percent = 0;
    String textInfo = "Processing...";
    GWT.log("Processing..." + job.getProcessRows() + " of " + job.getRowsToProcess() + " - Status: " + job.getStatus());

    if (job.getProcessRows() != null && job.getRowsToProcess() != null && job.getRowsToProcess() > 0) {
      percent = new Double((job.getProcessRows() * 1.0D / job.getRowsToProcess()) * 100).intValue();
      textInfo = percent + "% (" + job.getProcessRows() + " of " + job.getRowsToProcess() + ")";
    } else if (job.getStatus() == ViewerJobStatus.COMPLETED) {
      percent = 100;
      textInfo = "100%";
    }

    progressBar.getElement().getStyle().setWidth(percent, com.google.gwt.dom.client.Style.Unit.PCT);
    progressText.setText(textInfo);

    if (job.getStatus() != null) {
      statusBadge.setHTML(LabelUtils.getJobStatus(job.getStatus()));
    }
  }

  private void toggleProgressMode(boolean showProgress) {
    initialStatePanel.setVisible(!showProgress);
    progressPanel.setVisible(showProgress);
  }

  @Override
  public void updateCollection(CollectionStatus newStatus) {
    if (database != null && newStatus != null && newStatus.getDatabaseUUID().equals(database.getUuid())) {
      this.collectionStatus = newStatus;

      if (newStatus.isNeedsToBeProcessed()) {
        jobFinishedSuccessfully = false;
      }

      if (progressTimer == null && !jobFinishedSuccessfully) {
        updateVisualState();
      }
    }
  }

  @Override
  protected void onAttach() {
    super.onAttach();
    refreshStatusFromServer();
  }

  private void updateVisualState() {
    if (collectionStatus != null) {
      boolean needsProcess = collectionStatus.isNeedsToBeProcessed();

      if (jobFinishedSuccessfully) {
        alertPanel.setVisible(true);
        return;
      }

      alertPanel.setVisible(needsProcess);

      if (needsProcess) {
        toggleProgressMode(false);
        btnApplyConfiguration.setEnabled(true);
        statusBadge.setHTML("");
      }
    }
  }
}
