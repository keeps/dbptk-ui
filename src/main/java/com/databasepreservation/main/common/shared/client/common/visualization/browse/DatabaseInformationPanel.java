package com.databasepreservation.main.common.shared.client.common.visualization.browse;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.main.common.shared.ViewerConstants;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerMetadata;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.common.shared.client.common.RightPanel;
import com.databasepreservation.main.common.shared.client.common.utils.ApplicationType;
import com.databasepreservation.main.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.main.common.shared.client.tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DatabaseInformationPanel extends RightPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
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
    if (ApplicationType.getType().equals(ViewerConstants.DESKTOP)) {
      BreadcrumbManager.updateBreadcrumb(breadcrumb,
        BreadcrumbManager.forDesktopSIARDBrowse(database.getUUID(), database.getMetadata().getName()));
    } else {
      BreadcrumbManager.updateBreadcrumb(breadcrumb,
        BreadcrumbManager.forDatabase(database.getMetadata().getName(), database.getUUID()));
    }
  }

  private void init() {
    // database metadata
    ViewerMetadata metadata = database.getMetadata();
    SafeHtmlBuilder b = new SafeHtmlBuilder();
    b.append(getFieldHTML(messages.siardMetadata_databaseName(), metadata.getName()));
    b.append(getFieldHTML(messages.siardMetadata_archivalDate(),
      metadata.getArchivalDate() != null ? metadata.getArchivalDate().substring(0, 10) : metadata.getArchivalDate()));
    b.append(getFieldHTML(messages.siardMetadata_archivist(), metadata.getArchiver()));
    b.append(getFieldHTML(messages.siardMetadata_archivistContact(), metadata.getArchiverContact()));
    b.append(getFieldHTML(messages.siardMetadata_clientMachine(), metadata.getClientMachine()));
    b.append(getFieldHTML(messages.siardMetadata_databaseProduct(), metadata.getDatabaseProduct()));
    b.append(getFieldHTML(messages.siardMetadata_databaseUser(), metadata.getDatabaseUser()));
    b.append(getFieldHTML(messages.siardMetadata_dataOriginTimeSpan(), metadata.getDataOriginTimespan()));
    b.append(getFieldHTML(messages.siardMetadata_dataOwner(), metadata.getDataOwner()));
    if (ViewerStringUtils.isNotBlank(metadata.getDescription())) {
      b.append(getFieldHTML(messages.description(), metadata.getDescription()));
    } else {
      b.append(getFieldHTML(messages.description(), messages.siardMetadata_DescriptionUnavailable()));
    }
    b.append(getFieldHTML(messages.siardMetadata_producerApplication(), metadata.getProducerApplication()));
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
