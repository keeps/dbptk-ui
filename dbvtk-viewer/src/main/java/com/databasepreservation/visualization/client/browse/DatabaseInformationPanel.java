package com.databasepreservation.visualization.client.browse;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.visualization.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerMetadata;
import com.databasepreservation.visualization.client.main.BreadcrumbPanel;
import com.databasepreservation.visualization.shared.client.Tools.BreadcrumbManager;
import com.databasepreservation.visualization.shared.client.Tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DatabaseInformationPanel extends RightPanel {
  private static Map<String, DatabaseInformationPanel> instances = new HashMap<>();

  public static DatabaseInformationPanel getInstance(ViewerDatabase database) {
    String code = database.getUUID();

    DatabaseInformationPanel instance = instances.get(code);
    if (instance == null) {
      instance = new DatabaseInformationPanel(database);
      instances.put(code, instance);
    }
    return instance;
  }

  interface DatabaseInformationPanelUiBinder extends UiBinder<Widget, DatabaseInformationPanel> {
  }

  private static DatabaseInformationPanelUiBinder uiBinder = GWT.create(DatabaseInformationPanelUiBinder.class);

  private ViewerDatabase database;

  @UiField
  HTML metadatahtml;

  private DatabaseInformationPanel(ViewerDatabase database) {
    this.database = database;
    initWidget(uiBinder.createAndBindUi(this));

    init();
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(breadcrumb,
      BreadcrumbManager.forDatabase(database.getMetadata().getName(), database.getUUID()));
  }

  private void init() {
    // database metadata
    ViewerMetadata metadata = database.getMetadata();
    SafeHtmlBuilder b = new SafeHtmlBuilder();
    b.append(getFieldHTML("Database Name", metadata.getName()));
    b.append(getFieldHTML("Archival Date", metadata.getArchivalDate()));
    b.append(getFieldHTML("Archivist", metadata.getArchiver()));
    b.append(getFieldHTML("Archivist contact", metadata.getArchiverContact()));
    b.append(getFieldHTML("Client machine", metadata.getClientMachine()));
    b.append(getFieldHTML("Database product", metadata.getDatabaseProduct()));
    b.append(getFieldHTML("Database user", metadata.getDatabaseUser()));
    b.append(getFieldHTML("Data origin time span", metadata.getDataOriginTimespan()));
    b.append(getFieldHTML("Data owner", metadata.getDataOwner()));
    if (ViewerStringUtils.isNotBlank(metadata.getDescription())) {
      b.append(getFieldHTML("Description", metadata.getDescription()));
    } else {
      b.append(getFieldHTML("Description", "A description for this database is not available."));
    }
    b.append(getFieldHTML("Producer application", metadata.getProducerApplication()));
    metadatahtml.setHTML(b.toSafeHtml());
  }

  private SafeHtml getFieldHTML(String label, String value) {
    SafeHtmlBuilder b = new SafeHtmlBuilder();
    if (value != null) {
      b.append(SafeHtmlUtils.fromSafeConstant("<div class=\"field\">"));
      b.append(SafeHtmlUtils.fromSafeConstant("<div class=\"label\">"));
      b.append(SafeHtmlUtils.fromString(label));
      b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
      b.append(SafeHtmlUtils.fromSafeConstant("<div class=\"value\">"));
      b.append(SafeHtmlUtils.fromString(value));
      b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
      b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
    }
    return b.toSafeHtml();
  }
}
