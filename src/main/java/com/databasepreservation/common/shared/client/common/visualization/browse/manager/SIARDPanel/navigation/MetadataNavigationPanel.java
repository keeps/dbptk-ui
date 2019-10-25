package com.databasepreservation.common.shared.client.common.visualization.browse.manager.SIARDPanel.navigation;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.common.shared.client.common.MetadataField;
import com.databasepreservation.common.shared.client.tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;

import config.i18n.client.ClientMessages;

public class MetadataNavigationPanel {
  private static Map<String, MetadataNavigationPanel> instances = new HashMap<>();
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private ViewerDatabase database;
  private final String archivalDateHumanized;
  private MetadataField dbname;
  private MetadataField archivalDate;
  private MetadataField archiver;
  private MetadataField archiverContact;
  private MetadataField clientMachine;
  private MetadataField databaseProduct;
  private MetadataField dataOriginTimespan;
  private MetadataField dataOwner;
  private MetadataField producerApplication;
  private MetadataField descriptionPanel;

  public static MetadataNavigationPanel getInstance(ViewerDatabase database, String archivalDateHumanized) {
    String databaseUUID = database.getUUID();
    if (instances.get(databaseUUID) == null) {
      instances.put(databaseUUID, new MetadataNavigationPanel(database, archivalDateHumanized));
    }
    return instances.get(databaseUUID);
  }

  private MetadataNavigationPanel(ViewerDatabase database, String archivalDateHumanized) {
    this.database = database;
    this.archivalDateHumanized = archivalDateHumanized;
  }

  public FlowPanel build() {
    FlowPanel panel = new FlowPanel();
    panel.addStyleName("metadata-information-panel");
    FlowPanel left = new FlowPanel();
    left.addStyleName("metadata-information");
    FlowPanel right = new FlowPanel();
    right.addStyleName("metadata-information");

    dbname = MetadataField.createInstance(messages.SIARDHomePageLabelForViewerMetadataName(),
      database.getMetadata().getName());
    dbname.setCSSMetadata("metadata-field", "metadata-information-element-label", "metadata-information-element-value");
    archivalDate = MetadataField.createInstance(messages.SIARDHomePageLabelForViewerMetadataArchivalDate(),
      archivalDateHumanized);
    archivalDate.setCSSMetadata("metadata-field", "metadata-information-element-label",
      "metadata-information-element-value");
    archiver = MetadataField.createInstance(messages.SIARDHomePageLabelForViewerMetadataArchiver(),
      database.getMetadata().getArchiver());
    archiver.setCSSMetadata("metadata-field", "metadata-information-element-label",
      "metadata-information-element-value");
    archiverContact = MetadataField.createInstance(messages.SIARDHomePageLabelForViewerMetadataArchiverContact(),
      database.getMetadata().getArchiverContact());
    archiverContact.setCSSMetadata("metadata-field", "metadata-information-element-label",
      "metadata-information-element-value");
    clientMachine = MetadataField.createInstance(messages.SIARDHomePageLabelForViewerMetadataClientMachine(),
      database.getMetadata().getClientMachine());
    clientMachine.setCSSMetadata("metadata-field", "metadata-information-element-label",
      "metadata-information-element-value");

    left.add(dbname);
    left.add(archivalDate);
    left.add(archiver);
    left.add(archiverContact);
    left.add(clientMachine);

    databaseProduct = MetadataField.createInstance(messages.SIARDHomePageLabelForViewerMetadataDatabaseProduct(),
      database.getMetadata().getDatabaseProduct());
    databaseProduct.setCSSMetadata("metadata-field", "metadata-information-element-label",
      "metadata-information-element-value");
    dataOriginTimespan = MetadataField.createInstance(messages.SIARDHomePageLabelForViewerMetadataDataOriginTimespan(),
      database.getMetadata().getDataOriginTimespan());
    dataOriginTimespan.setCSSMetadata("metadata-field", "metadata-information-element-label",
      "metadata-information-element-value");
    dataOwner = MetadataField.createInstance(messages.SIARDHomePageLabelForViewerMetadataDataOwner(),
      database.getMetadata().getDataOwner());
    dataOwner.setCSSMetadata("metadata-field", "metadata-information-element-label",
      "metadata-information-element-value");
    producerApplication = MetadataField.createInstance(
      messages.SIARDHomePageLabelForViewerMetadataProducerApplication(),
      database.getMetadata().getProducerApplication());
    producerApplication.setCSSMetadata("metadata-field", "metadata-information-element-label",
      "metadata-information-element-value");

    right.add(databaseProduct);
    right.add(dataOriginTimespan);
    right.add(dataOwner);
    right.add(producerApplication);

    panel.add(left);
    panel.add(right);
    return panel;
  }

  public SimplePanel buildDescription() {
    SimplePanel panel = new SimplePanel();
    panel.setStyleName("metadata-description");
    String descriptionTxt = database.getMetadata().getDescription();

    if (ViewerStringUtils.isBlank(descriptionTxt) || descriptionTxt.contentEquals("unspecified")) {
      descriptionPanel = MetadataField.createInstance(messages.SIARDHomePageLabelForViewerMetadataDescription(),
        messages.SIARDHomePageTextForMissingDescription());
    } else {
      descriptionPanel = MetadataField.createInstance(messages.SIARDHomePageLabelForViewerMetadataDescription(),
        descriptionTxt);
    }

    descriptionPanel.setCSSMetadata("metadata-field", "metadata-information-description-label",
      "metadata-information-element-value");

    panel.add(descriptionPanel);

    return panel;
  }

  public void update(ViewerDatabase database) {
    dbname.updateText(database.getMetadata().getName());
    archivalDate.updateText(database.getMetadata().getArchivalDate());
    archiver.updateText(database.getMetadata().getArchiver());
    archiverContact.updateText(database.getMetadata().getArchiverContact());
    clientMachine.updateText(database.getMetadata().getClientMachine());
    databaseProduct.updateText(database.getMetadata().getDatabaseProduct());
    dataOriginTimespan.updateText(database.getMetadata().getDataOriginTimespan());
    dataOwner.updateText(database.getMetadata().getDataOwner());
    producerApplication.updateText(database.getMetadata().getProducerApplication());
    descriptionPanel.updateText(database.getMetadata().getDescription());
  }
}
