package com.databasepreservation.common.client.common.visualization.metadata.information;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.common.client.common.LoadingDiv;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.visualization.metadata.MetadataControlPanel;
import com.databasepreservation.common.client.common.visualization.metadata.MetadataPanel;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerSIARDBundle;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

import config.i18n.client.ClientMessages;

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
  private MetadataControlPanel controls = null;

  @UiField
  TextBox databaseName, archivist, archivistContact, clientMachine, databaseProduct, databaseUser, dataOriginTimeSpan,
    dataOwner, producerApplication;

  @UiField
  TextArea description;

  @UiField
  LoadingDiv loading;

  @UiField
  DateBox archivalDate;

  // @UiField
  // Button buttonEnableEdit;

  public static MetadataInformation getInstance(ViewerDatabase database, ViewerSIARDBundle SIARDbundle) {
    String code = database.getUuid();

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
      BreadcrumbManager.forSIARDEditMetadataPage(database.getUuid(), database.getMetadata().getName()));
  }

  private void init() {
    metadata = database.getMetadata();
    GWT.log("init: " + metadata.getArchivalDate());
    controls = MetadataControlPanel.getInstance(database.getUuid());
    writeOnViewerMetadataInformation(database.getMetadata());
  }

  private void writeOnViewerMetadataInformation(ViewerMetadata metadata) {
    setupElement(databaseName, metadata.getName(), "dbname", true);
    setupElement(archivist, metadata.getArchiver(), "archiver", false);
    setupElement(archivistContact, metadata.getArchiverContact(), "archiverContact", false);
    setupElement(clientMachine, metadata.getClientMachine(), "clientMachine", false);
    setupElement(databaseProduct, metadata.getDatabaseProduct(), "databaseProduct", false);
    setupElement(databaseUser, metadata.getDatabaseUser(), "databaseUser", false);
    setupElement(dataOriginTimeSpan, metadata.getDataOriginTimespan(), "dataOriginTimespan", true);
    setupElement(dataOwner, metadata.getDataOwner(), "dataOwner", true);

    setupElement(description, ViewerStringUtils.isNotBlank(metadata.getDescription()) ? metadata.getDescription()
      : messages.siardMetadata_DescriptionUnavailable(), "description", false);

    setupElement(producerApplication, metadata.getProducerApplication(), "producerApplication", false);
    setupElementDate(archivalDate, metadata.getArchivalDate(), "archivalDate", true);
  }

  private void setupElementDate(DateBox element, String dateStr, String name, boolean mandatory) {

    DateBox.DefaultFormat dateFormat = new DateBox.DefaultFormat(DateTimeFormat.getFormat("yyyy-MM-dd"));
    Date date = dateFormat.parse(element, dateStr, true);
    element.setFormat(dateFormat);
    element.setValue(date);
    element.getTextBox().setReadOnly(true);

    element.addValueChangeHandler(event -> {
      String nameEl = element.getElement().getAttribute("name");
      controls.checkIfElementIsMandatory(nameEl, element);
      SIARDbundle.setInformation(name, element.getTextBox().getValue());
      updateMetadata();
    });

    setupElement(element.getTextBox(), element.getTextBox().getText(), name, mandatory);
  }

  private void setupElement(TextBoxBase element, String text, String name, boolean mandatory) {
    element.setText(text);
    element.getElement().setAttribute("name", name);
    if (mandatory) {
      controls.setMandatoryItems(name, false);
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
        controls.checkIfElementIsMandatory(name, element);

        SIARDbundle.setInformation(name, element.getText());
        updateMetadata();
      }
    });
    element.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        String name = element.getElement().getAttribute("name");
        controls.checkIfElementIsMandatory(name, element);
      }
    });
  }

  private void updateMetadata() {
    /* Mandatory if empty keep original */
    if (!databaseName.getText().isEmpty()) {
      metadata.setName(databaseName.getText());
    }

    if (!archivalDate.getValue().toString().isEmpty()) {
      metadata.setArchivalDate(archivalDate.getTextBox().getValue());
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