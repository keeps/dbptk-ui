package com.databasepreservation.visualization.client.browse;

import com.databasepreservation.visualization.client.BrowserService;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.client.common.DefaultAsyncCallback;
import com.databasepreservation.visualization.client.main.BreadcrumbPanel;
import com.databasepreservation.visualization.shared.client.Tools.BreadcrumbManager;
import com.databasepreservation.visualization.shared.client.Tools.HistoryManager;
import com.databasepreservation.visualization.shared.client.widgets.Toast;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
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
  HTML content;

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
      float percent = 0;
      if (result.getTotalRows() > 0) {
        percent = (result.getIngestedRows() * 1.0F) / result.getTotalRows();
      }

      SafeHtmlBuilder shb = new SafeHtmlBuilder();
      StringBuilder sb = new StringBuilder();
      sb.append("Ingesting schema \"").append(result.getCurrentSchemaName()).append("\" (")
        .append(result.getIngestedSchemas()).append("/").append(result.getTotalSchemas()).append(")\n");
      sb.append("Ingesting table \"").append(result.getCurrentTableName()).append("\" (")
        .append(result.getIngestedTables()).append("/").append(result.getTotalTables()).append(")\n");
      sb.append("Ingested ").append(result.getIngestedRows()).append(" of ").append(result.getTotalRows())
        .append(" rows");
      if (percent >= 0) {
        sb.append(" (").append(NumberFormat.getPercentFormat().format(percent)).append(")");
      }
      sb.append(".");
      shb.appendEscapedLines(sb.toString());
      GWT.log("DBstatus: " + sb.toString().replaceAll("\n", "; "));
      content.setHTML(shb.toSafeHtml());
    } else if (result.getStatus().equals(ViewerDatabase.Status.AVAILABLE)) {
      Toast.showInfo("Success", "Database \"" + database.getMetadata().getName() + "\" has been loaded.");
      HistoryManager.gotoDatabase(result.getUUID());
    } else if (result.getStatus().equals(ViewerDatabase.Status.ERROR)) {
      content.setText("The database could not be converted due to an error. Check the logs for details.");
    } else if (result.getStatus().equals(ViewerDatabase.Status.REMOVING)) {
      content.setText("The database is being removed and can not be displayed.");
    } else {
      content.setText("The database is in an unhandled status.");
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
