package com.databasepreservation.main.desktop.client.dbptk.metadata.information;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerMetadata;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSIARDBundle;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.common.shared.client.common.LoadingDiv;
import com.databasepreservation.main.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.main.common.shared.client.tools.ViewerStringUtils;
import com.databasepreservation.main.desktop.client.dbptk.metadata.MetadataPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MetadataInformation extends MetadataPanel {
  interface EditMetadataInformationUiBinder extends UiBinder<Widget, MetadataInformation> {
  }

  private static EditMetadataInformationUiBinder uiBinder = GWT.create(EditMetadataInformationUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, MetadataInformation> instances = new HashMap<>();
  private ViewerDatabase database = null;
  private ViewerMetadata metadata = null;
  private ViewerSIARDBundle SIARDbundle;

  @UiField
  TextBox databaseName, archivalDate, archivist, archivistContact, clientMachine, databaseProduct, databaseUser,
    dataOriginTimeSpan, dataOwner, producerApplication;

  @UiField
  TextArea description;

  @UiField
  LoadingDiv loading;

  @UiField
  Button buttonEnableEdit;

  public static MetadataInformation getInstance(ViewerDatabase database, ViewerSIARDBundle SIARDbundle) {
    String code = database.getUUID();

    MetadataInformation instance = instances.get(code);
    if (instance == null) {
      instance = new MetadataInformation(database, SIARDbundle);
      instances.put(code, instance);
    }

    return instance;
  }

  private MetadataInformation(ViewerDatabase database, ViewerSIARDBundle SIARDbundle) {
    this.database = database;
    this.SIARDbundle = SIARDbundle;
    initWidget(uiBinder.createAndBindUi(this));

    init();
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(breadcrumb,
            BreadcrumbManager.forSIARDEditMetadataPage(database.getUUID()));
  }

  private void init() {
    GWT.log("Edit Metadata Information init ");
    metadata = database.getMetadata();
    writeOnViewerMetadataInformation(database.getMetadata());
  }

  private void writeOnViewerMetadataInformation(ViewerMetadata metadata) {

    setupElement(databaseName, metadata.getName(), "dbname", "text");
    setupElement(archivalDate,
      metadata.getArchivalDate() != null ? metadata.getArchivalDate().substring(0, 10) : metadata.getArchivalDate(),
      "archivalDate", "date");

    setupElement(archivist, metadata.getArchiver(), "archiver", "text");
    setupElement(archivistContact, metadata.getArchiverContact(), "archiverContact", "text");
    setupElement(clientMachine, metadata.getClientMachine(), "clientMachine", "text");
    setupElement(databaseProduct, metadata.getDatabaseProduct(), "databaseProduct", "text");
    setupElement(databaseUser, metadata.getDatabaseUser(), "databaseUser", "text");
    setupElement(dataOriginTimeSpan, metadata.getDataOriginTimespan(), "dataOriginTimespan", "text");
    setupElement(dataOwner, metadata.getDataOwner(), "dataOwner", "text");

    setupElement(description, ViewerStringUtils.isNotBlank(metadata.getDescription()) ? metadata.getDescription()
      : messages.siardMetadata_DescriptionUnavailable(), "description", "text");

    setupElement(producerApplication, metadata.getProducerApplication(), "producerApplication", "text");
  }

  private void setupElement(TextBoxBase element, String text, String name, String type) {
    element.setReadOnly(true);
    element.setText(text);
    element.getElement().setAttribute("name", name);
    element.getElement().setAttribute("type", type);
    element.getElement().addClassName("metadata-edit-readonly");
    element.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
//        updateSiardBundle(element);
        GWT.log("INFORMATION:::" + element.getText());
        SIARDbundle.setInformation(element.getElement().getAttribute("name"), element.getText());
        GWT.log("COMMAND:::" + SIARDbundle.getInformation(element.getElement().getAttribute("name")));
        updateMetadata();
      }
    });
  }

  private void updateMetadata() {
    metadata.setName(databaseName.getText());
    metadata.setArchivalDate(archivalDate.getText());
    metadata.setArchiver(archivist.getText());
    metadata.setArchiverContact(archivistContact.getText());
    metadata.setClientMachine(clientMachine.getText());
    metadata.setDatabaseProduct(databaseProduct.getText());
    metadata.setDatabaseUser(databaseUser.getText());
    metadata.setDataOriginTimespan(dataOriginTimeSpan.getText());
    metadata.setDescription(description.getText());
    metadata.setProducerApplication(producerApplication.getText());

    database.setMetadata(metadata);
  }


  @UiHandler("buttonEnableEdit")
  void buttonEnableEditHandle(ClickEvent e) {
    NodeList<Element> elements = Document.get().getElementsByTagName("input");

    for (int i = 0; i < elements.getLength(); i++) {
      elements.getItem(i).removeAttribute("readonly");
      elements.getItem(i).removeClassName("metadata-edit-readonly");
    }

    description.setReadOnly(false);
    description.getElement().removeClassName("metadata-edit-readonly");
  }
}