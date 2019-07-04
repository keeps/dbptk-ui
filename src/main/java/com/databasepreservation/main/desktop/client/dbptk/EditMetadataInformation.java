package com.databasepreservation.main.desktop.client.dbptk;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.ViewerStructure.IsIndexed;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerMetadata;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.main.common.shared.client.common.LoadingDiv;
import com.databasepreservation.main.common.shared.client.tools.ViewerStringUtils;
import com.databasepreservation.main.common.shared.client.widgets.Toast;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
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
public class EditMetadataInformation extends Composite {
  interface EditMetadataInformationUiBinder extends UiBinder<Widget, EditMetadataInformation> {
  }

  private static EditMetadataInformationUiBinder uiBinder = GWT.create(EditMetadataInformationUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, EditMetadataInformation> instances = new HashMap<>();
  private ViewerDatabase database = null;
  private ViewerMetadata metadata = null;

  @UiField
  TextBox databaseName, archivalDate, archivist, archivistContact, clientMachine, databaseProduct, databaseUser,
    dataOriginTimeSpan, dataOwner, producerApplication;

  @UiField
  TextArea description;

  @UiField
  LoadingDiv loading;

  @UiField
  Button buttonEnableEdit, buttonCancel, buttonSave;

  public static EditMetadataInformation getInstance(String databaseUUID) {

    if (instances.get(databaseUUID) == null) {
      EditMetadataInformation instance = new EditMetadataInformation(databaseUUID);
      instances.put(databaseUUID, instance);
    }

    return instances.get(databaseUUID);
  }

  private EditMetadataInformation(final String databaseUUID) {

    initWidget(uiBinder.createAndBindUi(this));

    BrowserService.Util.getInstance().retrieve(databaseUUID, ViewerDatabase.class.getName(), databaseUUID,
      new DefaultAsyncCallback<IsIndexed>() {
        @Override
        public void onFailure(Throwable caught) {
          GWT.log("MetadataInformation onFailure " + caught);
        }

        @Override
        public void onSuccess(IsIndexed result) {
          GWT.log("MetadataInformation onSuccess " + result);
          database = (ViewerDatabase) result;
          init();
        }
      });
  }

  private void init() {
    GWT.log("Edit Metadata Information init ");
    metadata = database.getMetadata();
    writeOnViewerMetadataInformation(database.getMetadata());

    buttonSave.setEnabled(false);
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
  }

  private Map<String, String> updateSiardBundle() {
    GWT.log("updateSiardMetadata");

    Map<String, String> bundle = new HashMap<>();

    bundle.put(databaseName.getElement().getAttribute("name"), databaseName.getText());
    bundle.put(archivalDate.getElement().getAttribute("name"), archivalDate.getText());
    bundle.put(archivist.getElement().getAttribute("name"), archivist.getText());
    bundle.put(archivistContact.getElement().getAttribute("name"), archivistContact.getText());
    bundle.put(clientMachine.getElement().getAttribute("name"), clientMachine.getText());
    bundle.put(databaseProduct.getElement().getAttribute("name"), databaseProduct.getText());
    bundle.put(databaseUser.getElement().getAttribute("name"), databaseUser.getText());
    bundle.put(dataOriginTimeSpan.getElement().getAttribute("name"), dataOriginTimeSpan.getText());
    bundle.put(description.getElement().getAttribute("name"), description.getText());
    bundle.put(producerApplication.getElement().getAttribute("name"), producerApplication.getText());

    return bundle;
  }

  private void updateSolrMetadata() {
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
  }

  @UiHandler("buttonEnableEdit")
  void buttonEnableEditHandle(ClickEvent e) {
    buttonSave.setEnabled(true);
    NodeList<Element> elements = Document.get().getElementsByTagName("input");

    for (int i = 0; i < elements.getLength(); i++) {
      elements.getItem(i).removeAttribute("readonly");
      elements.getItem(i).removeClassName("metadata-edit-readonly");
    }

    description.setReadOnly(false);
    description.getElement().removeClassName("metadata-edit-readonly");
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    buttonSave.setEnabled(false);
    writeOnViewerMetadataInformation(database.getMetadata());
  }

  @UiHandler("buttonSave")
  void buttonSaveHandler(ClickEvent e) {
    GWT.log("Save Metadata " + databaseName.getText());

    loading.setVisible(true);

    Map<String, String> bundleSiard = updateSiardBundle();
    updateSolrMetadata();

    BrowserService.Util.getInstance().updateMetadataInformation(metadata, bundleSiard, database.getUUID(),
      database.getSIARDPath(), new DefaultAsyncCallback<ViewerMetadata>() {
        @Override
        public void onFailure(Throwable caught) {
          // TODO: error handling
          writeOnViewerMetadataInformation(database.getMetadata());
          Toast.showError("Metadata Update", database.getMetadata().getName());
          loading.setVisible(false);
        }

        @Override
        public void onSuccess(ViewerMetadata result) {
          loading.setVisible(false);
          writeOnViewerMetadataInformation(result);
          Toast.showInfo("Metadata Update", "Success");
        }
      });

  }
}