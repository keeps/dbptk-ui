package com.databasepreservation.common.shared.client.common.visualization.browse;

import com.databasepreservation.common.client.BrowserService;
import com.databasepreservation.common.shared.ViewerConstants;
import com.databasepreservation.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.shared.client.common.Progressbar;
import com.databasepreservation.common.shared.client.common.RightPanel;
import com.databasepreservation.common.shared.client.common.utils.ApplicationType;
import com.databasepreservation.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.common.shared.client.tools.HistoryManager;
import com.databasepreservation.common.shared.client.widgets.Toast;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class UploadPanel extends RightPanel {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface UploadPanelUiBinder extends UiBinder<Widget, UploadPanel> {
  }

  public static UploadPanel createInstance(ViewerDatabase database) {
    return new UploadPanel(database);
  }

  private static UploadPanelUiBinder uiBinder = GWT.create(UploadPanelUiBinder.class);

  private ViewerDatabase database;
  private Timer autoUpdateTimer = new Timer() {
    @Override
    public void run() {
      UploadPanel.this.update();
    }
  };

  @UiField
  FlowPanel content;

  @UiField
  Label title, subtitle;

  @UiField
  Progressbar progressBar;

  private UploadPanel(ViewerDatabase database) {
    this.database = database;
    initWidget(uiBinder.createAndBindUi(this));
    update(database);
  }

  /**
   * Uses BreadcrumbManager to show available information in the breadcrumbPanel
   *
   * @param breadcrumb
   *          the BreadcrumbPanel for this database
   */
  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forNewUpload());
  }

  public void setTitleText(String titleText) {
    this.title.setText(titleText);
  }

  public void setSubtitleText(String subtitleText) {
    this.subtitle.setText(subtitleText);
  }

  private void init() {
    stopUpdating();
    autoUpdateTimer.scheduleRepeating(100);
  }

  @Override
  protected void onAttach() {
    init();
    super.onAttach();
  }

  private void update() {
    BrowserService.Util.getInstance().uploadSIARDStatus(database.getUUID(), new DefaultAsyncCallback<ViewerDatabase>() {
      @Override
      public void onSuccess(ViewerDatabase result) {
        update(result);
      }
    });
  }

  private void update(ViewerDatabase result) {
    if (result.getStatus().equals(ViewerDatabase.Status.INGESTING)
      || result.getStatus().equals(ViewerDatabase.Status.METADATA_ONLY)) {

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
      int currentGlobalPercent = new Double(((currentRows * 1.0D) / totalRows) * 100).intValue();
      progressBar.setCurrent(currentGlobalPercent);

      // create textual log

      // extra messages to smoothen the log
      if (database.getCurrentSchemaName() != null
        && !database.getCurrentSchemaName().equals(result.getCurrentSchemaName())) {
        // last message about a completed schema, schemas and rows at 100%
        addMessageToContent(buildMessage(database.getCurrentSchemaName(), database.getIngestedSchemas(),
          database.getTotalSchemas(), database.getCurrentTableName(), database.getTotalTables(),
          database.getTotalTables(), database.getTotalRows(), database.getTotalRows()));
      } else {
        if (database.getCurrentTableName() != null
          && !database.getCurrentTableName().equals(result.getCurrentTableName())) {
          // last message about a completed schema, rows at 100%
          addMessageToContent(buildMessage(database.getCurrentSchemaName(), database.getIngestedSchemas(),
            database.getTotalSchemas(), database.getCurrentTableName(), database.getIngestedTables(),
            database.getTotalTables(), database.getTotalRows(), database.getTotalRows()));
        }
      }

      // current message
      addMessageToContent(buildMessage(result.getCurrentSchemaName(), result.getIngestedSchemas(),
        result.getTotalSchemas(), result.getCurrentTableName(), result.getIngestedTables(), result.getTotalTables(),
        result.getIngestedRows(), result.getTotalRows()));

      database = result;
    } else if (result.getStatus().equals(ViewerDatabase.Status.AVAILABLE)) {
      Toast.showInfo("Success", "Database \"" + database.getMetadata().getName() + "\" has been loaded.");
      if (ApplicationType.getType().equals(ViewerConstants.DESKTOP)) {
        HistoryManager.gotoSIARDInfo(result.getUUID());
      } else {
        HistoryManager.gotoDatabase(result.getUUID());
      }
    } else if (result.getStatus().equals(ViewerDatabase.Status.ERROR)) {
      addMessageToContent("The database could not be converted due to an error. Check the logs for details.");
      stopUpdating();
    } else if (result.getStatus().equals(ViewerDatabase.Status.REMOVING)) {
      addMessageToContent("The database is being removed and can not be displayed.");
      stopUpdating();
    } else {
      addMessageToContent("The database is in an unhandled status.");
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

  private void addMessageToContent(String message) {
    Label newMessage = new Label(message);
    if (content.getWidgetCount() > 0) {
      Label lastMessage = (Label) content.getWidget(content.getWidgetCount() - 1);
      if (!newMessage.getText().equals(lastMessage.getText())) {
        content.add(newMessage);
        content.getElement().setScrollTop(content.getElement().getScrollHeight());
      }
    } else {
      content.add(newMessage);
      content.getElement().setScrollTop(content.getElement().getScrollHeight());
    }
  }

  private String buildMessage(String currentSchemaName, long ingestedSchemas, long totalSchemas,
    String currentTableName, long ingestedTables, long totalTables, long ingestedRows, long totalRows) {

    // show number of "completed and processing" instead of only completed ones
    if (ingestedSchemas < totalSchemas) {
      ingestedSchemas++;
    }
    if (ingestedTables < totalTables) {
      ingestedTables++;
    }

    float percent = 0;
    if (totalRows > 0) {
      percent = (ingestedRows * 1.0F) / totalRows;
    }

    StringBuilder sb = new StringBuilder();
    sb.append("Loading schema \"").append(currentSchemaName).append("\"");
    sb.append(" (").append(ingestedSchemas).append(" of ").append(totalSchemas).append(")");
    sb.append(", table \"").append(currentTableName).append("\" (").append(ingestedTables).append(" of ")
      .append(totalTables).append(")");
    if (percent > 0) {
      sb.append(", ").append(NumberFormat.getPercentFormat().format(percent)).append(" done");
    }
    return sb.toString();
  }
}
