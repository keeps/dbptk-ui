/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.visualization.metadata.schemas.views;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerSIARDBundle;
import com.databasepreservation.common.client.models.structure.ViewerSchema;
import com.databasepreservation.common.client.models.structure.ViewerView;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.visualization.metadata.MetadataControlPanel;
import com.databasepreservation.common.client.common.visualization.metadata.MetadataPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MetadataViewPanel extends MetadataPanel {

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    // DO NOTHING
  }

  interface MetadataViewUiBinder extends UiBinder<Widget, MetadataViewPanel> {
  }

  private static MetadataViewUiBinder uiBinder = GWT.create(MetadataViewUiBinder.class);

  @UiField
  SimplePanel mainHeader;

  @UiField
  TextArea description;

  @UiField
  TabPanel tabPanel;

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, MetadataViewPanel> instances = new HashMap<>();
  private ViewerSIARDBundle SIARDbundle;
  private MetadataControlPanel controls;
  private ViewerDatabase database;
  private ViewerSchema schema;
  private ViewerView view;

  public static MetadataViewPanel getInstance(ViewerDatabase database, ViewerSIARDBundle SIARDbundle, String schemaUUID,
                                              String viewUUID) {
    String separator = "/";
    String code = database.getUuid() + separator + viewUUID;

    MetadataViewPanel instance = instances.get(code);
    if (instance == null) {
      instance = new MetadataViewPanel(database, SIARDbundle, schemaUUID, viewUUID);
      instances.put(code, instance);
    }

    return instance;
  }

  private MetadataViewPanel(ViewerDatabase database, ViewerSIARDBundle SIARDbundle, String schemaUUID, String viewUUID) {
    this.database = database;
    this.SIARDbundle = SIARDbundle;
    view = database.getMetadata().getView(viewUUID);
    schema = database.getMetadata().getSchema(schemaUUID);
    controls = MetadataControlPanel.getInstance(database.getUuid());

    initWidget(uiBinder.createAndBindUi(this));
    init();
  }

  private void init() {
    Label viewName = new Label();
    viewName.setText(schema.getName()+"."+view.getName());
    mainHeader.setWidget(viewName);

    description.getElement().setAttribute("placeholder", messages.viewDoesNotContainDescription());
    description.setText(view.getDescription());
    description.addKeyUpHandler(event -> {
      view.setDescription(description.getText());
      SIARDbundle.setView(schema.getName(), view.getName(), description.getText());
      controls.validate();
    });
    description.addFocusHandler(event -> description.selectAll());

    tabPanel.add(new MetadataViewColumns(SIARDbundle,schema, view, controls).createTable(), messages.columns());
    tabPanel.add(new MetadataViewQuery(view).createInfo(), messages.query());

    tabPanel.selectTab(0);

  }
}