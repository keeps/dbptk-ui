package com.databasepreservation.common.client.common.visualization.browse.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.common.Progressbar;
import com.databasepreservation.common.client.models.DenormalizeProgressData;
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

  private Map<String, Timer> timerMap = new HashMap<>();

  @UiField
  public ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FlowPanel content;

  private ViewerDatabase database;
  List<DenormalizeProgressData> progressDataList;
  Map<String, Progressbar> progressbarMap = new HashMap<>();

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
    JobService.Util.call((List<DenormalizeProgressData> response) -> {
      progressDataList = response;
      getProgressData();
    }).progress(database.getUuid());
  }

  private void getProgressData() {
    for (DenormalizeProgressData progressData : progressDataList) {
      Timer timer = new Timer() {
        @Override
        public void run() {
          DataTransformationProgressPanel.this.update();
        }
      };

      timer.scheduleRepeating(1000);
      timer.run();
      timerMap.put(progressData.getTableUUID(), timer);

      Progressbar progressbar = new Progressbar();
      progressbar.setCurrent(0);
      // progressData.reset();

      ViewerTable table = database.getMetadata().getTable(progressData.getTableUUID());
      createProgressPanel(progressbar, table);
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

    panel.add(stop);
    panel.add(start);
    panel.add(tableTitle);
    panel.add(progress);

    content.add(panel);
  }

  private void update() {
    JobService.Util.call((List<DenormalizeProgressData> response) -> {
      progressDataList = response;
      for (DenormalizeProgressData progressData : progressDataList) {
        if (progressData.getFinished()) {
          progressbarMap.get(progressData.getTableUUID()).setCurrent(100);
          stopUpdating(progressData.getTableUUID());
        } else {
          createProgressBar(progressData);
        }
      }
    }).progress(database.getUuid());
  }

  private void stopUpdating(String tableUUID) {
    Timer timer = timerMap.get(tableUUID);
    if (timer != null) {
      timer.cancel();
    }
  }

  private void createProgressBar(DenormalizeProgressData progressData) {
    int currentGlobalPercent = new Double(
      ((progressData.getProcessedRows() * 1.0D) / progressData.getRowsToProcess()) * 100).intValue();
    progressbarMap.get(progressData.getTableUUID()).setCurrent(currentGlobalPercent);
  }
}
