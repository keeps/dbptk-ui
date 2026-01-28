package com.databasepreservation.common.client.common.visualization.browse.configuration;

import com.databasepreservation.common.api.v1.utils.JobResponse;
import com.databasepreservation.common.client.ClientLogger;
import com.databasepreservation.common.client.ObserverManager;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.configuration.observer.ICollectionStatusObserver;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
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
  private ClientLogger logger = new ClientLogger(getClass().getName());
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField(provided = true)
  Alert alertPanel;

  @UiField
  Button btnApplyConfiguration;

  interface ConfigurationStatusPanelUiBinder extends UiBinder<Widget, ConfigurationStatusPanel> {
  }

  private static ConfigurationStatusPanelUiBinder binder = GWT.create(ConfigurationStatusPanelUiBinder.class);

  public static ConfigurationStatusPanel getInstance(String databaseUUID) {
    return new ConfigurationStatusPanel();
  }

  public ConfigurationStatusPanel() {
    alertPanel = new Alert(Alert.MessageAlertType.INFO, messages.configurationStatusPanelLabelForTitle(),
      FontAwesomeIconManager.DATABASE_INFORMATION);
    initWidget(binder.createAndBindUi(this));
    ObserverManager.getCollectionObserver().addObserver(this);
  }

  private void configure(String databaseUUID) {

  }

  @Override
  public void updateCollection(CollectionStatus collectionStatus) {
    if (collectionStatus.getDatabaseUUID() != null) {
      GWT.log(" ==== Collection status updated for database " + collectionStatus.getDatabaseUUID());
      String databaseUUID = collectionStatus.getDatabaseUUID();
      alertPanel.setVisible(true);
      btnApplyConfiguration.setEnabled(true);
      btnApplyConfiguration.addClickHandler(clickEvent -> {
        CollectionService.Util.call((JobResponse response) -> {
          Toast.showInfo(messages.advancedConfigurationLabelForDataTransformation(),
            "Apply configuration job started with success for database " + databaseUUID);
          HistoryManager.gotoJobs();
        }, errorMessage -> {
          Dialogs.showErrors(messages.advancedConfigurationLabelForDataTransformation(), errorMessage,
            messages.basicActionClose());
        }).run(databaseUUID, databaseUUID, null);
        alertPanel.setVisible(false);
      });
    }
  }
}
