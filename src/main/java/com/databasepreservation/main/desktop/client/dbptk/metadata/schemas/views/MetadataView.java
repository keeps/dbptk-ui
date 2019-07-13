package com.databasepreservation.main.desktop.client.dbptk.metadata.schemas.views;

import com.databasepreservation.main.common.server.index.factory.SolrClientFactory;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSIARDBundle;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerView;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.desktop.client.dbptk.metadata.MetadataPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import config.i18n.client.ClientMessages;
import org.apache.solr.client.solrj.SolrClient;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MetadataView extends MetadataPanel {

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {

  }

  interface MetadataViewUiBinder extends UiBinder<Widget, MetadataView> {
  }

  private static MetadataViewUiBinder uiBinder = GWT.create(MetadataViewUiBinder.class);

  @UiField
  SimplePanel mainHeader;

  @UiField
  TextArea description;

  @UiField
  FlowPanel query, queryOriginal;

  @UiField
  TabPanel tabPanel;

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, MetadataView> instances = new HashMap<>();
  private ViewerSIARDBundle SIARDbundle;
  private ViewerDatabase database;
  private ViewerSchema schema;
  private ViewerView view;

  public static MetadataView getInstance(ViewerDatabase database, ViewerSIARDBundle SIARDbundle, String schemaUUID,
    String viewUUID) {
    String separator = "/";
    String code = database.getUUID() + separator + viewUUID;

    MetadataView instance = instances.get(code);
    if (instance == null) {
      instance = new MetadataView(database, SIARDbundle, schemaUUID, viewUUID);
      instances.put(code, instance);
    }

    return instance;
  }

  private MetadataView(ViewerDatabase database, ViewerSIARDBundle SIARDbundle, String schemaUUID, String viewUUID) {
    this.database = database;
    this.SIARDbundle = SIARDbundle;
    view = database.getMetadata().getView(viewUUID);
    schema = database.getMetadata().getSchema(schemaUUID);

    GWT.log("MetadataView::" + view.getName());

    initWidget(uiBinder.createAndBindUi(this));
    init();
  }

  private void init() {
    Label viewName = new Label();
    viewName.setText(view.getName());
    mainHeader.setWidget(viewName);

    description
      .setText(view.getDescription() == null ? messages.viewDoesNotContainDescription() : view.getDescription());
    description.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        view.setDescription(description.getText());
        SIARDbundle.setView(schema.getName(), view.getName(), description.getText());
      }
    });

    addContent(messages.query(), view.getQuery(), query, messages.viewDoesNotContainQuery());
    addContent(messages.originalQuery(), view.getQueryOriginal(), query, messages.viewDoesNotContainQueryOriginal());

    tabPanel.add(new MetadataViewColumns(SIARDbundle).createTable(view, schema), messages.columns());

    tabPanel.selectTab(0);

  }

  private void addContent(String headerLabel, String bodyValue, FlowPanel panel, String emptyMessage){
    Label label = new Label();
    Label value = new Label();

    label.setText(headerLabel);
    label.addStyleName("label");

    if(bodyValue != null && !bodyValue.isEmpty() ){
      value.setText(bodyValue);
    } else {
      value.setText(emptyMessage);
    }
    value.addStyleName("value");

    panel.add(label);
    panel.add(value);
  }
}