package com.databasepreservation.visualization.client.browse;

import com.databasepreservation.visualization.client.BrowserService;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerSchema;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerTable;
import com.databasepreservation.visualization.client.common.DefaultAsyncCallback;
import com.databasepreservation.visualization.client.common.Progressbar;
import com.databasepreservation.visualization.client.main.BreadcrumbPanel;
import com.databasepreservation.visualization.shared.client.Tools.BreadcrumbManager;
import com.databasepreservation.visualization.shared.client.Tools.HistoryManager;
import com.databasepreservation.visualization.shared.client.widgets.Toast;
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

  private void init() {
    stopUpdating();
    autoUpdateTimer.scheduleRepeating(1000);
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
    if (result.getStatus().equals(ViewerDatabase.Status.INGESTING)) {

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

      float percent = 0;
      if (result.getTotalRows() > 0) {
        percent = (result.getIngestedRows() * 1.0F) / result.getTotalRows();
      }

      StringBuilder sb = new StringBuilder();
      sb.append("Loading schema \"").append(result.getCurrentSchemaName()).append("\" (")
        .append(result.getIngestedSchemas()).append(" of ").append(result.getTotalSchemas()).append("), ");
      sb.append("table \"").append(result.getCurrentTableName()).append("\" (").append(result.getIngestedTables())
        .append(" of ").append(result.getTotalTables()).append(")");
      if (percent > 0) {
        sb.append(", ").append(NumberFormat.getPercentFormat().format(percent)).append(" done");
      }

      GWT.log("DBstatus: " + sb.toString());
      addMessageToContent(sb.toString());
    } else if (result.getStatus().equals(ViewerDatabase.Status.AVAILABLE)) {
      Toast.showInfo("Success", "Database \"" + database.getMetadata().getName() + "\" has been loaded.");
      HistoryManager.gotoDatabase(result.getUUID());
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
}
