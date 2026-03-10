package com.databasepreservation.common.client.common.visualization.browse.configuration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.roda.core.data.v2.index.sublist.Sublist;

import com.databasepreservation.common.api.v1.utils.JobResponse;
import com.databasepreservation.common.client.ObserverManager;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultMethodCallback;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.utils.html.LabelUtils;
import com.databasepreservation.common.client.configuration.observer.ICollectionStatusObserver;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.index.facets.Facets;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.filter.FilterParameter;
import com.databasepreservation.common.client.index.filter.OrFiltersParameters;
import com.databasepreservation.common.client.index.filter.SimpleFilterParameter;
import com.databasepreservation.common.client.index.sort.Sorter;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerJob;
import com.databasepreservation.common.client.models.structure.ViewerJobStatus;
import com.databasepreservation.common.client.services.CollectionService;
import com.databasepreservation.common.client.services.JobService;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.widgets.Alert;
import com.databasepreservation.common.client.widgets.Toast;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
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
  HTML statusBadge;
  @UiField
  Label jobNameLabel;
  @UiField
  Label stepCountLabel;
  @UiField
  Label stepNameProgressLabel;
  @UiField
  Label elapsedTimeLabel;

  private Timer progressTimer;
  private boolean jobFinishedSuccessfully = false;
  private boolean isStoppingPolling = false;
  private CollectionStatus collectionStatus;
  private ViewerDatabase database;

  interface ConfigurationStatusPanelUiBinder extends UiBinder<Widget, ConfigurationStatusPanel> {
  }

  private static ConfigurationStatusPanelUiBinder binder = GWT.create(ConfigurationStatusPanelUiBinder.class);

  public ConfigurationStatusPanel() {
    alertPanel = new Alert(Alert.MessageAlertType.INFO, "");

    initWidget(binder.createAndBindUi(this));

    messageLabel.setText(messages.configurationStatusPanelLabelForTitle());

    alertPanel.setVisible(false);
    toggleProgressMode(false);

    ObserverManager.getCollectionObserver().addObserver(this);
    initHandlers();
  }

  public void setDatabase(ViewerDatabase database) {
    this.database = database;
    refreshStatusFromServer(true);
  }

  @Override
  protected void onDetach() {
    stopPolling();
    super.onDetach();
  }

  private void refreshStatusFromServer(boolean checkActiveJobs) {
    if (database == null)
      return;

    CollectionService.Util.call((List<CollectionStatus> statusList) -> {
      if (statusList != null && !statusList.isEmpty()) {
        updateCollection(statusList.get(0));
      }
    }).getCollectionConfiguration(database.getUuid(), database.getUuid());

    if (checkActiveJobs) {
      retrieveStatusFromLastJob();
    }
  }

  private void retrieveStatusFromLastJob() {
    ArrayList<FilterParameter> filterParameters = new ArrayList<>();
    filterParameters.add(new SimpleFilterParameter(ViewerConstants.SOLR_ROWS_DATABASE_UUID, database.getUuid()));
    ArrayList<FilterParameter> orFilterParameters = new ArrayList<>();

    orFilterParameters
      .add(new SimpleFilterParameter(ViewerConstants.SOLR_BATCH_JOB_STATUS, ViewerJobStatus.STARTING.name()));
    orFilterParameters
      .add(new SimpleFilterParameter(ViewerConstants.SOLR_BATCH_JOB_STATUS, ViewerJobStatus.STARTED.name()));

    filterParameters.add(new OrFiltersParameters(orFilterParameters));

    FindRequest findRequest = new FindRequest(ViewerJob.class.getName(), new Filter(filterParameters), new Sorter(),
      new Sublist(), new Facets());

    JobService.Util.call((IndexResult<ViewerJob> results) -> {
      if (results != null && results.getResults() != null && !results.getResults().isEmpty()) {
        ViewerJob latestJob = results.getResults().get(0);
        ViewerJobStatus status = latestJob.getStatus();

        if (status != ViewerJobStatus.COMPLETED && status != ViewerJobStatus.FAILED
          && status != ViewerJobStatus.STOPPED) {

          startPolling(latestJob.getUuid());
        }
      }
    }).find(findRequest, LocaleInfo.getCurrentLocale().getLocaleName());
  }

  private void initHandlers() {
    btnApplyConfiguration.addClickHandler(clickEvent -> {
      if (collectionStatus != null) {
        btnApplyConfiguration.setEnabled(false);
        collectionStatus.setNeedsToBeProcessed(false);

        CollectionService.Util.callDetailed((Boolean updateSuccess) -> {
          runJob();
        }, errorMessage -> {
          btnApplyConfiguration.setEnabled(true);
          Dialogs.showConfigurationDependencyErrors(errorMessage.get(DefaultMethodCallback.MESSAGE_KEY),
            errorMessage.get(DefaultMethodCallback.DETAILS_KEY), messages.basicActionClose());

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
      Dialogs.showErrors(messages.configurationStatusPanelDialogTitleForError(), errorMessage,
        messages.basicActionClose());
    }).run(database.getUuid(), database.getUuid(), null);
  }

  private void startPolling(String jobUUID) {
    isStoppingPolling = false;
    alertPanel.setVisible(true);
    toggleProgressMode(true);
    updateProgressVisuals(null); // Reset visual

    progressTimer = new Timer() {
      @Override
      public void run() {
        checkJobStatus(jobUUID);
      }
    };
    progressTimer.scheduleRepeating(1000);
  }

  private void stopPolling() {
    if (progressTimer != null) {
      progressTimer.cancel();
      progressTimer = null;
    }
  }

  private void checkJobStatus(String jobUUID) {
    JobService.Util.call((ViewerJob job) -> {
      if (job == null || isStoppingPolling)
        return;

      updateProgressVisuals(job);
      ViewerJobStatus status = job.getStatus();

      if (status == ViewerJobStatus.COMPLETED || status == ViewerJobStatus.FAILED
        || status == ViewerJobStatus.STOPPED) {

        isStoppingPolling = true;
        stopPolling();

        if (status == ViewerJobStatus.COMPLETED) {
          handleJobSuccess(job);
        } else {
          handleJobFailure(job);
        }
      }

    }, errorMessage -> {
      //
    }).retrieve(jobUUID);
  }

  private void handleJobSuccess(ViewerJob job) {
    jobFinishedSuccessfully = true;

    String stepName = (job != null && job.getCurrentStepName() != null) ? job.getCurrentStepName()
      : messages.configurationStatusPanelTextForInitializing();

    if (job != null && job.getSkipCount() != null && job.getSkipCount() > 0) {
      Toast.showInfo(messages.advancedConfigurationLabelForDataTransformation(),
        messages.configurationStatusPanelToastDescriptionForProcessingCompletedWithSkip(job.getSkipCount()));
      progressBar.removeStyleName("progress-bar-striped");
      progressBar.removeStyleName("active");
      progressBar.getElement().getStyle().setBackgroundColor("#f0ad4e");
      stepNameProgressLabel
        .setText(messages.configurationStatusPanelDescriptionForStepFinishedWithSkip(stepName, job.getSkipCount()));
      stepNameProgressLabel.getElement().getStyle().setColor("#c98114");
    } else {
      Toast.showInfo(messages.advancedConfigurationLabelForDataTransformation(),
        messages.configurationStatusPanelToastDescriptionForProcessingCompleted());
      progressBar.removeStyleName("active");
      stepNameProgressLabel.setText(messages.configurationStatusPanelDescriptionForStepFinished(stepName));
    }

    progressBar.getElement().getStyle().setWidth(100, com.google.gwt.dom.client.Style.Unit.PCT);
    statusBadge.setHTML(LabelUtils.getJobStatus(ViewerJobStatus.COMPLETED));

    if (job != null) {
      elapsedTimeLabel.setText(messages.configurationStatusPanelTextForElapsedTime(calculateTotalTime(job)));
    }

    refreshStatusFromServer(false);

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
    String errorDetails = job.getExitDescription() != null ? job.getExitDescription()
      : messages.configurationStatusPanelDialogTitleForError();
    Dialogs.showErrors(messages.configurationStatusPanelDialogTitleForError(), errorDetails,
      messages.basicActionClose());

    toggleProgressMode(false);
    btnApplyConfiguration.setEnabled(true);
  }

  private void updateProgressVisuals(ViewerJob job) {
    if (job == null) {
      if (!jobFinishedSuccessfully) {
        progressBar.getElement().getStyle().setWidth(0, com.google.gwt.dom.client.Style.Unit.PCT);
        statusBadge.setHTML("");
        jobNameLabel.setText(messages.configurationStatusPanelTextForInitializing());
        stepCountLabel.setText("");
        stepNameProgressLabel.setText(messages.configurationStatusPanelTextForInitializing());
        elapsedTimeLabel.setText("");
        progressBar.getElement().getStyle().clearBackgroundColor();
      }
      return;
    }

    int percent = 0;

    // Job Name
    if (job.getName() != null && !job.getName().isEmpty()) {
      jobNameLabel.setText(job.getName());
    } else {
      jobNameLabel.setText(messages.configurationStatusPanelTextForInitializing());
    }

    // Step Count
    if (job.getCurrentStepNumber() != null && job.getTotalSteps() != null) {
      stepCountLabel
        .setText(messages.configurationStatusPanelTextForStepCount(job.getCurrentStepNumber(), job.getTotalSteps()));
    } else {
      stepCountLabel.setText(messages.configurationStatusPanelTextForInitializing());
    }

    // Elapsed Time
    elapsedTimeLabel.setText(messages.configurationStatusPanelTextForElapsedTime(calculateTotalTime(job)));

    // Final Status Handling
    if (job.getStatus() == ViewerJobStatus.FAILED || job.getStatus() == ViewerJobStatus.STOPPED) {
      return;
    }

    if (job.getStatus() == ViewerJobStatus.COMPLETED) {
      progressBar.getElement().getStyle().setWidth(100, com.google.gwt.dom.client.Style.Unit.PCT);
      statusBadge.setHTML(LabelUtils.getJobStatus(job.getStatus()));
      return;
    }

    // Progress and Step Name Handling
    String stepName = job.getCurrentStepName() != null ? job.getCurrentStepName()
      : messages.configurationStatusPanelTextForProcessing();

    if (job.getStatus() == ViewerJobStatus.STARTING) {
      stepNameProgressLabel.setText(messages.configurationStatusPanelDescriptionForStepInitializing(stepName));
    } else if (job.getProcessRows() != null && job.getRowsToProcess() != null && job.getRowsToProcess() > 0) {
      percent = new Double((job.getProcessRows() * 1.0D / job.getRowsToProcess()) * 100).intValue();
      stepNameProgressLabel.setText(messages.configurationStatusPanelTextForStepProgress(stepName, percent,
        job.getProcessRows(), job.getRowsToProcess()));

    } else {
      stepNameProgressLabel.setText(messages.configurationStatusPanelTextForStepRunning(stepName));
    }

    progressBar.getElement().getStyle().setWidth(percent, com.google.gwt.dom.client.Style.Unit.PCT);

    if (job.getStatus() != null) {
      statusBadge.setHTML(LabelUtils.getJobStatus(job.getStatus()));
    }
  }

  private String calculateTotalTime(ViewerJob job) {
    if (job == null || job.getStartTime() == null)
      return "";
    Date end = job.getEndTime() != null ? job.getEndTime() : new Date();
    long millis = end.getTime() - job.getStartTime().getTime();
    return formatDuration(millis);
  }

  private String formatDuration(long millis) {
    long seconds = (millis / 1000) % 60;
    long minutes = (millis / (1000 * 60)) % 60;
    long hours = (millis / (1000 * 60 * 60));

    if (hours > 0)
      return hours + "h " + minutes + "m " + seconds + "s";
    if (minutes > 0)
      return minutes + "m " + seconds + "s";
    return seconds + "s";
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
    refreshStatusFromServer(true);
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
