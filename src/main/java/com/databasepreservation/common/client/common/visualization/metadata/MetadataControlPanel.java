package com.databasepreservation.common.client.common.visualization.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.client.common.LoadingDiv;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.utils.ApplicationType;
import com.databasepreservation.common.client.common.utils.JavascriptUtils;
import com.databasepreservation.common.client.models.parameters.SIARDUpdateParameters;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerSIARDBundle;
import com.databasepreservation.common.client.services.SiardService;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.widgets.Toast;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MetadataControlPanel extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface MetadataControlPanelUiBinder extends UiBinder<Widget, MetadataControlPanel> {
  }

  private static MetadataControlPanelUiBinder uiBinder = GWT.create(MetadataControlPanelUiBinder.class);

  private static Map<String, MetadataControlPanel> instances = new HashMap<>();
  private ViewerDatabase database = null;
  private ViewerSIARDBundle SIARDbundle = null;
  private Map<String, Boolean> mandatoryItems = new HashMap<>();
  private static final long ALERT_SIARD_FILE_SIZE = 1000000000;

  @UiField
  Button buttonSave;

  @UiField
  Button buttonClear;

  @UiField
  Button buttonCancel;

  @UiField
  Label toolTip;

  @UiField
  LoadingDiv loading;

  public static MetadataControlPanel getInstance(String databaseUUID) {

    MetadataControlPanel instance = instances.get(databaseUUID);
    if (instance == null) {
      instance = new MetadataControlPanel();
      instances.put(databaseUUID, instance);
    }

    return instance;
  }

  private MetadataControlPanel() {
    initWidget(uiBinder.createAndBindUi(this));
    buttonSave.setEnabled(false);
    buttonClear.setEnabled(false);
  }

  public void init(ViewerDatabase database, ViewerSIARDBundle SIARDbundle) {
    this.database = database;
    this.SIARDbundle = SIARDbundle;
  }

  public void setMandatoryItems(String name, Boolean required) {
    mandatoryItems.put(name, required);
  }

  public void checkIfElementIsMandatory(String name, Widget element) {
    String value = null;
    if (element instanceof TextBoxBase) {
      value = ((TextBoxBase) element).getText();
    } else if (element instanceof DateBox) {
      value = ((DateBox) element).getDatePicker().getValue().toString();
    }
    if (mandatoryItems.get(name) != null) {
      if (value == null || value.isEmpty()) {
        mandatoryItems.replace(name, true);
      } else {
        mandatoryItems.replace(name, false);
      }
    }
    validate();
  }

  public void validate() {
    List<String> mandatoryItemsRequired = new ArrayList<>();
    buttonSave.setEnabled(true);
    for (Map.Entry<String, Boolean> entry : mandatoryItems.entrySet()) {
      if (entry.getValue()) {
        buttonSave.setEnabled(false);
        mandatoryItemsRequired.add(entry.getKey());
      }
    }

    buttonClear.setEnabled(true);
    toolTip.setVisible(true);
    if (mandatoryItemsRequired.isEmpty()) {
      toolTip.setText(messages.metadataHasUpdates());
      toolTip.removeStyleName("missing");
    } else {
      toolTip.setText(messages.metadataMissingFields(mandatoryItemsRequired.toString()));
      toolTip.addStyleName("missing");
    }
  }

  public void reset() {
    buttonSave.setEnabled(false);
    buttonClear.setEnabled(false);
    toolTip.setVisible(false);
  }

  private void updateMetadata() {
    ViewerMetadata metadata = database.getMetadata();

    loading.setVisible(true);
    reset();

    SiardService.Util.call((ViewerMetadata result) -> {
      loading.setVisible(false);
      Toast.showInfo(messages.metadataSuccessUpdated(), "");
    }, (String errorString) -> {
      Toast.showError(messages.metadataFailureUpdated(), errorString);
      loading.setVisible(false);
      buttonSave.setEnabled(true);
      buttonClear.setEnabled(true);
      toolTip.setVisible(true);
    }).updateMetadataInformation(database.getUuid(), database.getUuid(), database.getPath(),
      new SIARDUpdateParameters(metadata, SIARDbundle));
  }

  @UiHandler("buttonSave")
  void buttonSaveHandler(ClickEvent e) {

    String message = messages.dialogConfirmUpdateMetadata();
    if (database.getSize() > ALERT_SIARD_FILE_SIZE) {
      message = messages.dialogLargeFileConfirmUpdateMetadata();
    }

    if (ApplicationType.getType().equals(ViewerConstants.APPLICATION_ENV_DESKTOP)) {
      JavascriptUtils.confirmationDialog(messages.dialogUpdateMetadata(), message, messages.basicActionCancel(),
        messages.basicActionConfirm(), new DefaultAsyncCallback<Boolean>() {

          @Override
          public void onSuccess(Boolean confirm) {
            if (confirm) {
              updateMetadata();
            }
          }

        });
    } else {
      Dialogs.showConfirmDialog(messages.dialogUpdateMetadata(), message, messages.basicActionCancel(),
        messages.basicActionConfirm(), new DefaultAsyncCallback<Boolean>() {

          @Override
          public void onFailure(Throwable caught) {
            Toast.showError(messages.metadataFailureUpdated(), caught.getMessage());
          }

          @Override
          public void onSuccess(Boolean confirm) {
            if (confirm) {
              updateMetadata();
            }
          }
        });
    }
  }

  @UiHandler("buttonCancel")
  void cancelButtonHandler(ClickEvent e) {
    HistoryManager.gotoSIARDInfo(database.getUuid());
  }

  @UiHandler("buttonClear")
  void clearButtonHandler(ClickEvent e) {
    Window.Location.reload();
  }
}
