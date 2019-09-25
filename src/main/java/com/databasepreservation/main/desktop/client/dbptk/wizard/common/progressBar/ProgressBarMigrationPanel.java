package com.databasepreservation.main.desktop.client.dbptk.wizard.common.progressBar;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.ProgressData;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.google.gwt.user.client.Timer;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ProgressBarMigrationPanel extends ProgressBarPanel {
  private Timer autoUpdateTimer = new Timer() {
    @Override
    public void run() {
      ProgressBarMigrationPanel.this.update();
    }
  };

  public static ProgressBarPanel getInstance(String uuid) {
    if (instances.get(uuid) == null) {
      instances.put(uuid, new ProgressBarMigrationPanel(uuid));
    }

    return instances.get(uuid);
  }

  private ProgressBarMigrationPanel(String uuid) {
    initWidget(uiBinder.createAndBindUi(this));
    this.databaseUUID = uuid;
    update();
  }

  private void init() {
    BrowserService.Util.getInstance().getProgressData(databaseUUID, new DefaultAsyncCallback<ProgressData>() {
      @Override
      public void onSuccess(ProgressData result) {
        result.reset();
        progressBar.setCurrent(0);
        content.clear();
        stopUpdating();
        autoUpdateTimer.scheduleRepeating(100);
      }
    });
  }

  public void setTitleText(String text) {
    title.setText(text);
  }

  public void setSubtitleText(String text) {
    subTitle.setText(text);
  }

  @Override
  protected void onAttach() {
    init();
    super.onAttach();
  }

  public void clear(String uuid) {
    progressBar.setCurrent(0);
    content.clear();
    instances.put(uuid, null);
  }

  private void update() {
    BrowserService.Util.getInstance().getProgressData(databaseUUID, new DefaultAsyncCallback<ProgressData>() {
      @Override
      public void onSuccess(ProgressData result) {
        update(result);
      }
    });
  }

  private void update(ProgressData progressData) {
    if (progressData.isDatabaseStructureRetrieved()) {
      int currentGlobalPercent = new Double(
          ((progressData.getProcessedRows() * 1.0D) / progressData.getTotalRows()) * 100).intValue();
      progressBar.setCurrent(currentGlobalPercent);

      final String totalTablesPercentage = buildPercentageMessage(messages.progressBarPanelTextForTables(),
        progressData.getProcessedTables(), progressData.getTotalTables());
      final String totalRowsPercentage = buildPercentageMessage(messages.progressBarPanelTextForRows(),
        progressData.getProcessedRows(), progressData.getTotalRows());
      final String currentTable = buildSimpleMessage(messages.progressBarPanelTextForCurrentTable(),
        progressData.getCurrentTableName());
      final String currentTableRowsPercentage = buildPercentageMessage(messages.progressBarPanelTextForCurrentRows(),
        progressData.getCurrentProcessedTableRows(), progressData.getCurrentTableTotalRows());

      addMessageToContent(1, totalTablesPercentage);
      addMessageToContent(2, totalRowsPercentage);
      addMessageToContent(3, currentTable);
      addMessageToContent(4, currentTableRowsPercentage);
    } else {
      final String retrieving = buildSimpleMessage("", messages.progressBarPanelTextForRetrievingTableStructure());
      addMessageToContent(0, retrieving);
    }

    if (progressData.isFinished()) {
      stopUpdating();
    }
  }

  @Override
  protected void onDetach() {
    stopUpdating();
    super.onDetach();
  }

  private void stopUpdating() {
    if (autoUpdateTimer != null) {
      autoUpdateTimer.cancel();
    }
  }
}
