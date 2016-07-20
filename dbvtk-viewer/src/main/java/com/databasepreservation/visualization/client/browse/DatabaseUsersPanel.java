package com.databasepreservation.visualization.client.browse;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.visualization.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.client.main.BreadcrumbPanel;
import com.databasepreservation.visualization.shared.client.Tools.BreadcrumbManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DatabaseUsersPanel extends RightPanel {
  private static Map<String, DatabaseUsersPanel> instances = new HashMap<>();

  public static DatabaseUsersPanel getInstance(ViewerDatabase database) {
    String code = database.getUUID();

    DatabaseUsersPanel instance = instances.get(code);
    if (instance == null) {
      instance = new DatabaseUsersPanel(database);
      instances.put(code, instance);
    }
    return instance;
  }

  interface DatabasePanelUiBinder extends UiBinder<Widget, DatabaseUsersPanel> {
  }

  private static DatabasePanelUiBinder uiBinder = GWT.create(DatabasePanelUiBinder.class);

  private ViewerDatabase database;

  @UiField
  HTML metadatahtml;

  private DatabaseUsersPanel(ViewerDatabase database) {
    initWidget(uiBinder.createAndBindUi(this));

    this.database = database;
    Label tmpNote = new Label("User information will be available on this page in a future release.");
    metadatahtml.setHTML(SafeHtmlUtils.fromSafeConstant(tmpNote.toString()));
  }

  private void init() {

  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(breadcrumb,
      BreadcrumbManager.forDatabase(database.getMetadata().getName(), database.getUUID()));
  }
}
