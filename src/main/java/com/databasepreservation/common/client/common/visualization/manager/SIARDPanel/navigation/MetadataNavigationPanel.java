package com.databasepreservation.common.client.common.visualization.manager.SIARDPanel.navigation;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.tools.Humanize;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;

import config.i18n.client.ClientMessages;

public class MetadataNavigationPanel {
  private static Map<String, MetadataNavigationPanel> instances = new HashMap<>();
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private ViewerDatabase database;
  private MetadataField archivalDate;
  private MetadataField archiver;
  private MetadataField archiverContact;
  private MetadataField clientMachine;
  private MetadataField databaseProduct;
  private MetadataField dataOriginTimespan;
  private MetadataField dataOwner;
  private MetadataField producerApplication;

  public static MetadataNavigationPanel getInstance(ViewerDatabase database) {
    String databaseUUID = database.getUuid();
    if (instances.get(databaseUUID) == null) {
      instances.put(databaseUUID, new MetadataNavigationPanel(database));
    }
    return instances.get(databaseUUID);
  }

  private MetadataNavigationPanel(ViewerDatabase database) {
    this.database = database;
  }

  public FlowPanel build() {
    FlowPanel panel = new FlowPanel();
    panel.addStyleName("metadata-information-panel");
    FlowPanel left = new FlowPanel();
    left.addStyleName("metadata-information");
    FlowPanel right = new FlowPanel();
    right.addStyleName("metadata-information");

    archivalDate = MetadataField.createInstance(messages.SIARDHomePageLabelForViewerMetadataArchivalDate(),
        Humanize.formatDateTime(database.getMetadata().getArchivalDate()));
    archivalDate.setCSS("metadata-field", "metadata-information-element-label",
      "metadata-information-element-value");
    archiver = MetadataField.createInstance(messages.SIARDHomePageLabelForViewerMetadataArchiver(),
      database.getMetadata().getArchiver());
    archiver.setCSS("metadata-field", "metadata-information-element-label",
      "metadata-information-element-value");
    archiverContact = MetadataField.createInstance(messages.SIARDHomePageLabelForViewerMetadataArchiverContact(),
      database.getMetadata().getArchiverContact());
    archiverContact.setCSS("metadata-field", "metadata-information-element-label",
      "metadata-information-element-value");
    clientMachine = MetadataField.createInstance(messages.SIARDHomePageLabelForViewerMetadataClientMachine(),
      database.getMetadata().getClientMachine());
    clientMachine.setCSS("metadata-field", "metadata-information-element-label",
      "metadata-information-element-value");

    left.add(archivalDate);
    left.add(archiver);
    left.add(archiverContact);
    left.add(clientMachine);

    databaseProduct = MetadataField.createInstance(messages.SIARDHomePageLabelForViewerMetadataDatabaseProduct(),
      database.getMetadata().getDatabaseProduct());
    databaseProduct.setCSS("metadata-field", "metadata-information-element-label",
      "metadata-information-element-value");
    dataOriginTimespan = MetadataField.createInstance(messages.SIARDHomePageLabelForViewerMetadataDataOriginTimespan(),
      database.getMetadata().getDataOriginTimespan());
    dataOriginTimespan.setCSS("metadata-field", "metadata-information-element-label",
      "metadata-information-element-value");
    dataOwner = MetadataField.createInstance(messages.SIARDHomePageLabelForViewerMetadataDataOwner(),
      database.getMetadata().getDataOwner());
    dataOwner.setCSS("metadata-field", "metadata-information-element-label",
      "metadata-information-element-value");
    producerApplication = MetadataField.createInstance(
      messages.SIARDHomePageLabelForViewerMetadataProducerApplication(),
      database.getMetadata().getProducerApplication());
    producerApplication.setCSS("metadata-field", "metadata-information-element-label",
      "metadata-information-element-value");

    right.add(databaseProduct);
    right.add(dataOriginTimespan);
    right.add(dataOwner);
    right.add(producerApplication);

    panel.add(left);
    panel.add(right);
    return panel;
  }

  public void update(ViewerDatabase database) {
    archivalDate.updateText(Humanize.formatDateTime(database.getMetadata().getArchivalDate()));
    archiver.updateText(database.getMetadata().getArchiver());
    archiverContact.updateText(database.getMetadata().getArchiverContact());
    clientMachine.updateText(database.getMetadata().getClientMachine());
    databaseProduct.updateText(database.getMetadata().getDatabaseProduct());
    dataOriginTimespan.updateText(database.getMetadata().getDataOriginTimespan());
    dataOwner.updateText(database.getMetadata().getDataOwner());
    producerApplication.updateText(database.getMetadata().getProducerApplication());
  }
}
