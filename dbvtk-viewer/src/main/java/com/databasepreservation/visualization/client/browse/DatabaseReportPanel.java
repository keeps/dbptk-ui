package com.databasepreservation.visualization.client.browse;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.visualization.client.common.DefaultAsyncCallback;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.roda.core.data.exceptions.NotFoundException;

import com.databasepreservation.visualization.client.BrowserService;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.client.common.lists.BasicTablePanel;
import com.databasepreservation.visualization.client.main.BreadcrumbPanel;
import com.databasepreservation.visualization.shared.client.Tools.BreadcrumbManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DatabaseReportPanel extends RightPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, DatabaseReportPanel> instances = new HashMap<>();

  public static DatabaseReportPanel getInstance(ViewerDatabase database) {
    String code = database.getUUID();

    DatabaseReportPanel instance = instances.get(code);
    if (instance == null) {
      instance = new DatabaseReportPanel(database);
      instances.put(code, instance);
    }
    return instance;
  }

  interface ReportPanelUiBinder extends UiBinder<Widget, DatabaseReportPanel> {
  }

  private static ReportPanelUiBinder uiBinder = GWT.create(ReportPanelUiBinder.class);

  private ViewerDatabase database;

  @UiField
  HTML contentItems;

  private DatabaseReportPanel(ViewerDatabase database) {
    this.database = database;

    initWidget(uiBinder.createAndBindUi(this));

    init();
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(breadcrumb,
      BreadcrumbManager.forDatabaseReport(database.getMetadata().getName(), database.getUUID()));
  }

  private void init() {
    BrowserService.Util.getInstance().getReport(database.getUUID(), new DefaultAsyncCallback<String>() {
      @Override public void onFailure(Throwable caught) {
        if(caught instanceof NotFoundException){
          contentItems.setHTML(SafeHtmlUtils.fromString(caught.getMessage()));
        }else{
          super.onFailure(caught);
        }
      }

      @Override
      public void onSuccess(String result) {
        contentItems.setHTML(SafeHtmlUtils.fromString(result));
      }
    });
  }
}
