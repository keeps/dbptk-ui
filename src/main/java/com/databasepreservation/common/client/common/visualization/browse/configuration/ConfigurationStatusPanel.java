package com.databasepreservation.common.client.common.visualization.browse.configuration;

import java.util.List;

import com.databasepreservation.common.api.v1.utils.JobResponse;
import com.databasepreservation.common.client.ObserverManager;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.configuration.observer.ICollectionStatusObserver;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.services.CollectionService;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.widgets.Alert;
import com.databasepreservation.common.client.widgets.Toast;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ConfigurationStatusPanel extends Composite implements ICollectionStatusObserver {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField(provided = true)
  Alert alertPanel;

  @UiField
  Button btnApplyConfiguration;

  interface ConfigurationStatusPanelUiBinder extends UiBinder<Widget, ConfigurationStatusPanel> {
  }

  private static ConfigurationStatusPanelUiBinder binder = GWT.create(ConfigurationStatusPanelUiBinder.class);

  private CollectionStatus collectionStatus;
  private ViewerDatabase database;

  public ConfigurationStatusPanel() {
    alertPanel = new Alert(Alert.MessageAlertType.INFO, messages.configurationStatusPanelLabelForTitle(),
      FontAwesomeIconManager.DATABASE_INFORMATION);

    initWidget(binder.createAndBindUi(this));

    alertPanel.setVisible(false);

    ObserverManager.getCollectionObserver().addObserver(this);

    initHandlers();
  }

  public void setDatabase(ViewerDatabase database) {
    this.database = database;
    refreshStatusFromServer();
  }

  private void refreshStatusFromServer() {
    if (database == null)
      return;

    CollectionService.Util.call((List<CollectionStatus> statusList) -> {
      if (statusList != null && !statusList.isEmpty()) {
        updateCollection(statusList.get(0));
      }
    }).getCollectionConfiguration(database.getUuid(), database.getUuid());
  }

  private void initHandlers() {
    btnApplyConfiguration.addClickHandler(clickEvent -> {
      if (collectionStatus != null) {
        collectionStatus.setNeedsToBeProcessed(false);
        CollectionService.Util.call((Boolean updateSuccess) -> {
          runJob();
        }, errorMessage -> {
          Dialogs.showErrors("Error", errorMessage, messages.basicActionClose());
        }).updateCollectionConfiguration(database.getUuid(), database.getUuid(), collectionStatus);
      }
    });
  }

  private void runJob() {
    CollectionService.Util.call((JobResponse response) -> {
      updateVisualState();

      Toast.showInfo(messages.advancedConfigurationLabelForDataTransformation(), "Success");
      HistoryManager.gotoJobs();
    }, errorMessage -> {
      Dialogs.showErrors("Error", errorMessage, messages.basicActionClose());
    }).run(database.getUuid(), database.getUuid(), null);
  }

  @Override
  public void updateCollection(CollectionStatus newStatus) {
    if (database != null && newStatus != null && newStatus.getDatabaseUUID().equals(database.getUuid())) {
      this.collectionStatus = newStatus;
      updateVisualState();
    }
  }

  @Override
  protected void onAttach() {
    super.onAttach();
    refreshStatusFromServer();
  }

  private void updateVisualState() {
    if (collectionStatus != null) {
      boolean needsProcess = collectionStatus.isNeedsToBeProcessed();
      alertPanel.setVisible(needsProcess);
      btnApplyConfiguration.setEnabled(needsProcess);
    }
  }
}
