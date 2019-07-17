package com.databasepreservation.main.desktop.client.dbptk.metadata.information;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerMetadata;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSIARDBundle;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.common.shared.client.common.LoadingDiv;
import com.databasepreservation.main.common.shared.client.common.utils.JavascriptUtils;
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
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
  private Map<String, Boolean> mandatoryItem = new HashMap<>();

  @UiField
  TextBox databaseName, archivalDate, archivist, archivistContact, clientMachine, databaseProduct, databaseUser,
    dataOriginTimeSpan, dataOwner, producerApplication;

  @UiField
  TextArea description;

  @UiField
  LoadingDiv loading;

  // @UiField
  // Button buttonEnableEdit;

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
    BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forSIARDEditMetadataPage(database.getUUID()));
  }

  private void init() {
    GWT.log("Edit Metadata Information init ");
    metadata = database.getMetadata();
    writeOnViewerMetadataInformation(database.getMetadata());
  }

  private void writeOnViewerMetadataInformation(ViewerMetadata metadata) {

    setupElement(databaseName, metadata.getName(), "dbname", "text", true);
    setupElement(archivalDate,
      metadata.getArchivalDate() != null ? metadata.getArchivalDate().substring(0, 10) : metadata.getArchivalDate(),
      "archivalDate", "date", true);

    setupElement(archivist, metadata.getArchiver(), "archiver", "text", false);
    setupElement(archivistContact, metadata.getArchiverContact(), "archiverContact", "text", false);
    setupElement(clientMachine, metadata.getClientMachine(), "clientMachine", "text", false);
    setupElement(databaseProduct, metadata.getDatabaseProduct(), "databaseProduct", "text", false);
    setupElement(databaseUser, metadata.getDatabaseUser(), "databaseUser", "text", false);
    setupElement(dataOriginTimeSpan, metadata.getDataOriginTimespan(), "dataOriginTimespan", "text", true);
    setupElement(dataOwner, metadata.getDataOwner(), "dataOwner", "text", true);

    setupElement(description, ViewerStringUtils.isNotBlank(metadata.getDescription()) ? metadata.getDescription()
      : messages.siardMetadata_DescriptionUnavailable(), "description", "text", false);

    setupElement(producerApplication, metadata.getProducerApplication(), "producerApplication", "text", false);
  }

  private void setupElement(TextBoxBase element, String text, String name, String type, boolean mandatory) {
    element.setText(text);
    element.getElement().setAttribute("name", name);
    element.getElement().setAttribute("type", type);
    if(mandatory){
      mandatoryItem.put(name, false);
      element.getElement().setAttribute("required", "required");
    }
    element.addFocusHandler(new FocusHandler() {
      @Override
      public void onFocus(FocusEvent event) {
        element.selectAll();
      }
    });
    element.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        String name = element.getElement().getAttribute("name");
        checkIfElementIsMandatory(name, element);

        SIARDbundle.setInformation(name, element.getText());
        updateMetadata();
        JavascriptUtils.alertUpdatedMetadata();
      }
    });
  }


  private void checkIfElementIsMandatory(String name, TextBoxBase element){

    if(mandatoryItem.get(name) != null) {
      //validate
      if(element.getText().isEmpty()){
        mandatoryItem.replace(name, true);
      } else {
        mandatoryItem.replace(name, false);
      }
    }

    for (Map.Entry<String, Boolean> entry : mandatoryItem.entrySet()) {
      if(entry.getValue()) {
        JavascriptUtils.disableSaveMetadataButton(true);
        break;
      }
      JavascriptUtils.disableSaveMetadataButton(false);
    }
  }

  private void updateMetadata() {
    /* Mandatory if empty keep original */
    if (!databaseName.getText().isEmpty()) {
      metadata.setName(databaseName.getText());
    }

    if (!archivalDate.getText().isEmpty()) {
      metadata.setArchivalDate(archivalDate.getText());
    }
    if (!dataOwner.getText().isEmpty()) {
      metadata.setDataOwner(dataOwner.getText());
    }
    if (!dataOriginTimeSpan.getText().isEmpty()) {
      metadata.setDataOriginTimespan(dataOriginTimeSpan.getText());
    }

    /* Optional */
    metadata.setArchiver(archivist.getText());
    metadata.setArchiverContact(archivistContact.getText());
    metadata.setClientMachine(clientMachine.getText());
    metadata.setDatabaseProduct(databaseProduct.getText());
    metadata.setDatabaseUser(databaseUser.getText());
    metadata.setDescription(description.getText());
    metadata.setProducerApplication(producerApplication.getText());

    database.setMetadata(metadata);
  }
}