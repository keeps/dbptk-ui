package com.databasepreservation.common.client.common.visualization.browse.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.common.Progressbar;
import com.databasepreservation.common.client.common.visualization.validation.ValidatorPage;
import com.databasepreservation.common.client.models.DataTransformationProgressData;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.services.JobService;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DataTransformationProgressPanel extends Composite {

  interface DataTransformationProgressPanelBinder extends UiBinder<Widget, DataTransformationProgressPanel> {
  }

  private static DataTransformationProgressPanelBinder binder = GWT.create(DataTransformationProgressPanelBinder.class);
  private static Map<String, DataTransformationProgressPanel> instances = new HashMap<>();

  private List<String> timerMap = new ArrayList<>();

  @UiField
  public ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FlowPanel content;

  private ViewerDatabase database;
  List<DataTransformationProgressData> progressDataList;
  Map<String, Progressbar> progressbarMap = new HashMap<>();

  private Timer autoUpdateTimer = new Timer() {
    @Override
    public void run() {
      DataTransformationProgressPanel.this.update();
    }
  };

  public static DataTransformationProgressPanel getInstance(ViewerDatabase database) {
    return instances.computeIfAbsent(database.getUuid(), k -> new DataTransformationProgressPanel(database));
  }

  private DataTransformationProgressPanel(ViewerDatabase database) {
    this.database = database;
    initWidget(binder.createAndBindUi(this));
    init();
  }

  private void init() {
    Label title = new Label("Jobs");
    content.add(title);
  }

  public void initProgress() {
    JobService.Util.call((List<DataTransformationProgressData> response) -> {
      content.clear();
      autoUpdateTimer.scheduleRepeating(1000);
      autoUpdateTimer.run();
      progressDataList = response;
      getProgressData();
    }).progress(database.getUuid());
  }

  private void getProgressData() {
    for (DataTransformationProgressData progressData : progressDataList) {
      Progressbar progressbar = new Progressbar();
      ViewerTable table = database.getMetadata().getTable(progressData.getTableUUID());
      createProgressPanel(progressbar, table);
      timerMap.add(progressData.getTableUUID());
      progressbarMap.put(progressData.getTableUUID(), progressbar);
    }
  }

  private void createProgressPanel(Progressbar progressbar, ViewerTable table) {
    FlowPanel panel = new FlowPanel();
    Label tableTitle = new Label(table.getName());
    FlowPanel progress = new FlowPanel();
    progress.setStyleName("progress-report-panel");
    progress.add(progressbar);

    Button stop = new Button("Stop");

    stop.addClickHandler(event -> {
      JobService.Util.call((Boolean result) -> {
        stopUpdating(table.getUuid());
      }).stopDenormalizeJob(database.getUuid(), table.getUuid());
    });

    Button start = new Button("start");

    start.addClickHandler(event -> {
      JobService.Util.call((Boolean result) -> {
        stopUpdating(table.getUuid());
      }).startDenormalizeJob(database.getUuid(), table.getUuid());
    });

    panel.add(tableTitle);
    panel.add(progress);

    content.add(panel);
  }

  private void update() {
    JobService.Util.call((List<DataTransformationProgressData> response) -> {
      progressDataList = response;
      for (DataTransformationProgressData progressData : progressDataList) {
        GWT.log("-----------------------------");
        if (progressData.getFinished()) {
          timerMap.remove(progressData.getTableUUID());
          GWT.log("Finishing: " + progressData.getTableUUID());
          progressbarMap.get(progressData.getTableUUID()).setCurrent(100);
          stopUpdating(progressData.getTableUUID());
        } else {
          createProgressBar(progressData);
        }
        GWT.log("-----------------------------");
      }
    }).progress(database.getUuid());
  }

  private void stopUpdating(String tableUUID) {
    if(timerMap.isEmpty()){
      if (autoUpdateTimer != null) {
        autoUpdateTimer.cancel();
      }
    }
  }

  private void createProgressBar(DataTransformationProgressData progressData) {
    int currentGlobalPercent = new Double(
      ((progressData.getProcessedRows() * 1.0D) / progressData.getRowsToProcess()) * 100).intValue();
    GWT.log("updating: " + progressData.getTableUUID());
    if (progressbarMap.get(progressData.getTableUUID()) != null) {
      GWT.log("getRowsToProcess: " + progressData.getRowsToProcess());
      GWT.log("getProcessedRows: " + progressData.getProcessedRows());
      GWT.log("currentGlobalPercent: " + currentGlobalPercent);
      progressbarMap.get(progressData.getTableUUID()).setCurrent(currentGlobalPercent);
    }
  }
}
