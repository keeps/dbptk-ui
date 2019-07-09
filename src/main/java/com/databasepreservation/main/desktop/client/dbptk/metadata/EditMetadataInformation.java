package com.databasepreservation.main.desktop.client.dbptk.metadata;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerMetadata;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.common.shared.client.common.LoadingDiv;
import com.databasepreservation.main.common.shared.client.common.RightPanel;
import com.databasepreservation.main.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.main.common.shared.client.tools.ViewerStringUtils;
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
import com.google.gwt.user.client.ui.FlowPanel;
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
public class EditMetadataInformation extends MetadataRightPanel {
  interface EditMetadataInformationUiBinder extends UiBinder<Widget, EditMetadataInformation> {
  }

  private static EditMetadataInformationUiBinder uiBinder = GWT.create(EditMetadataInformationUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, EditMetadataInformation> instances = new HashMap<>();
  private ViewerDatabase database = null;
  private ViewerMetadata metadata = null;
  private Map<String, String> SIARDbundle;

  @UiField
  TextBox databaseName, archivalDate, archivist, archivistContact, clientMachine, databaseProduct, databaseUser,
    dataOriginTimeSpan, dataOwner, producerApplication;

  @UiField
  TextArea description;

  @UiField
  LoadingDiv loading;

  @UiField
  Button buttonEnableEdit;

  public static EditMetadataInformation getInstance(ViewerDatabase database, Map<String, String> SIARDbundle) {
    String code = database.getUUID();

    EditMetadataInformation instance = instances.get(code);
    if (instance == null) {
      instance = new EditMetadataInformation(database, SIARDbundle);
      instances.put(code, instance);
    }

    return instance;
  }

  private EditMetadataInformation(ViewerDatabase database, Map<String, String> SIARDbundle) {
    this.database = database;
    this.SIARDbundle = SIARDbundle;
    initWidget(uiBinder.createAndBindUi(this));

    init();
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(breadcrumb,
            BreadcrumbManager.forSIARDEditMetadataPage());
  }

  private void init() {
    GWT.log("Edit Metadata Information init ");
    metadata = database.getMetadata();
    writeOnViewerMetadataInformation(database.getMetadata());
  }

  private void writeOnViewerMetadataInformation(ViewerMetadata metadata) {

    setupElement(databaseName, metadata.getName(), "dbname");
    setupElement(archivalDate,
      metadata.getArchivalDate() != null ? metadata.getArchivalDate().substring(0, 10) : metadata.getArchivalDate(),
      "archivalDate");
    archivalDate.getElement().setAttribute("type", "date");

    setupElement(archivist, metadata.getArchiver(), "archiver");
    setupElement(archivistContact, metadata.getArchiverContact(), "archiverContact");
    setupElement(clientMachine, metadata.getClientMachine(), "clientMachine");
    setupElement(databaseProduct, metadata.getDatabaseProduct(), "databaseProduct");
    setupElement(databaseUser, metadata.getDatabaseUser(), "databaseUser");
    setupElement(dataOriginTimeSpan, metadata.getDataOriginTimespan(), "dataOriginTimespan");
    setupElement(dataOwner, metadata.getDataOwner(), "dataOwner");

    setupElement(description, ViewerStringUtils.isNotBlank(metadata.getDescription()) ? metadata.getDescription()
      : messages.siardMetadata_DescriptionUnavailable(), "dataOwner");

    setupElement(producerApplication, metadata.getProducerApplication(), "producerApplication");
  }

  private void setupElement(TextBoxBase element, String text, String name) {
    element.setReadOnly(true);
    element.setText(text);
    element.getElement().setAttribute("name", name);
    element.getElement().addClassName("metadata-edit-readonly");
    element.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        updateSiardBundle(element);
        updateSolrMetadata();
      }
    });
  }

  private void updateSiardBundle(TextBoxBase element) {
    SIARDbundle.put(element.getElement().getAttribute("name"), element.getText());
  }

  private void updateSolrMetadata() {

    GWT.log("onChange metadata: " + databaseName.getText());
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