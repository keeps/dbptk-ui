package com.databasepreservation.main.desktop.client.dbptk.wizard.common.progressBar;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ProgressBarIngestPanel extends ProgressBarPanel {
  private Timer autoUpdateTimer = new Timer() {
    @Override
    public void run() {
      ProgressBarIngestPanel.this.update();
    }
  };
  private ViewerDatabase database = null;

  public static ProgressBarPanel getInstance(String uuid) {
    if (instances.get(uuid) == null) {
      instances.put(uuid, new ProgressBarIngestPanel(uuid));
    }

    return instances.get(uuid);
  }

  private ProgressBarIngestPanel(String uuid) {
    initWidget(uiBinder.createAndBindUi(this));
    this.databaseUUID = uuid;
    update();
  }

  private void init() {
    stopUpdating();
    autoUpdateTimer.scheduleRepeating(100);
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
    BrowserService.Util.getInstance().uploadSIARDStatus(databaseUUID, new DefaultAsyncCallback<ViewerDatabase>() {
      @Override
      public void onSuccess(ViewerDatabase result) {
        update(result);
      }
    });
  }

  private void update(ViewerDatabase result) {
    if (result.getStatus().equals(ViewerDatabase.Status.INGESTING)) {

      GWT.log("Current Schema: " + result.getCurrentSchemaName());
      GWT.log("Ingested Rows: " + result.getIngestedRows());
      // obtain global progress percentage as an integer
      boolean tablesDone = true;
      long currentRows = 0;
      long totalRows = 0;
      for (ViewerSchema viewerSchema : result.getMetadata().getSchemas()) {
        for (ViewerTable viewerTable : viewerSchema.getTables()) {
          totalRows += viewerTable.getCountRows();
          if (tablesDone) {
            if (viewerSchema.getName().equals(result.getCurrentSchemaName())
              && viewerTable.getName().equals(result.getCurrentTableName())) {
              currentRows += result.getIngestedRows();
              tablesDone = false;
            } else {
              currentRows += viewerTable.getCountRows();
            }
          }
        }
      }
      GWT.log("Total of rows: " + totalRows);
      int currentGlobalPercent = new Double(((currentRows * 1.0D) / totalRows) * 100).intValue();
      GWT.log("Progress: " + currentGlobalPercent);
      progressBar.setCurrent(currentGlobalPercent);

      // create textual log

      // extra messages to smoothen the log
      if (database.getCurrentSchemaName() != null
        && !database.getCurrentSchemaName().equals(result.getCurrentSchemaName())) {
        // last message about a completed schema, schemas and rows at 100%
        // addMessageToContent(buildMessage(database.getCurrentSchemaName(),
        // database.getIngestedSchemas(),
        // database.getTotalSchemas(), database.getCurrentTableName(),
        // database.getTotalTables(),
        // database.getTotalTables(), database.getTotalRows(),
        // database.getTotalRows()));
      } else {
        if (database.getCurrentTableName() != null
          && !database.getCurrentTableName().equals(result.getCurrentTableName())) {
          // last message about a completed schema, rows at 100%
          // addMessageToContent(buildMessage(database.getCurrentSchemaName(),
          // database.getIngestedSchemas(),
          // database.getTotalSchemas(), database.getCurrentTableName(),
          // database.getIngestedTables(),
          // database.getTotalTables(), database.getTotalRows(),
          // database.getTotalRows()));
        }
      }

      // current message
      // addMessageToContent(buildMessage(result.getCurrentSchemaName(),
      // result.getIngestedSchemas(),
      // result.getTotalSchemas(), result.getCurrentTableName(),
      // result.getIngestedTables(), result.getTotalTables(),
      // result.getIngestedRows(), result.getTotalRows()));

      database = result;
    } else if (result.getStatus().equals(ViewerDatabase.Status.AVAILABLE)) {
      // Toast.showInfo("Success", "Database \"" + database.getMetadata().getName() +
      // "\" has been loaded.");
      // if (ApplicationType.getType().equals(ViewerConstants.ELECTRON)) {
      // HistoryManager.gotoSIARDInfo(result.getUUID());
      // } else {
      // HistoryManager.gotoDatabase(result.getUUID());
      // }
    } else if (result.getStatus().equals(ViewerDatabase.Status.ERROR)) {
      // addMessageToContent("The database could not be converted due to an error.
      // Check the logs for details.");
      stopUpdating();
    } else if (result.getStatus().equals(ViewerDatabase.Status.REMOVING)) {
      // addMessageToContent("The database is being removed and can not be
      // displayed.");
      stopUpdating();
    } else {
      // addMessageToContent("The database is in an unhandled status.");
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
