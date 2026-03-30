package com.databasepreservation.common.client.common.visualization.browse.configuration.columns;

import com.databasepreservation.common.api.v1.utils.ConfigurationContext;
import com.databasepreservation.common.client.common.DefaultMethodCallback;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.services.CollectionService;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ConfigurationStateController {
  public interface ProjectionSyncCallback {
    void onSuccess(ViewerDatabase database, CollectionStatus collectionStatus);
  }

  public interface UpdateSuccessCallback {
    void onSuccess();
  }

  public interface ErrorCallback {
    void onError(String message, String details);
  }

  private static ConfigurationStateController instance;

  private ConfigurationStateController() {
  }

  public static ConfigurationStateController getInstance() {
    if (instance == null) {
      instance = new ConfigurationStateController();
    }
    return instance;
  }

  public void fetchProjectedDatabase(String databaseUuid, ProjectionSyncCallback callback) {
    CollectionService.Util.call((ConfigurationContext context) -> {
      if (context != null && context.getProjectedDatabase() != null) {
        callback.onSuccess(context.getProjectedDatabase(), context.getCollectionStatus());
      }
    }).getConfigurationContext(databaseUuid, databaseUuid);
  }

  public void updateConfigurationContext(String databaseUuid, CollectionStatus status,
    ProjectionSyncCallback successCallback, ErrorCallback errorCallback) {
    CollectionService.Util.callDetailed((ConfigurationContext context) -> {
      successCallback.onSuccess(context.getProjectedDatabase(), context.getCollectionStatus());
    }, errorMessage -> {
      String msg = errorMessage.get(DefaultMethodCallback.MESSAGE_KEY);
      String details = errorMessage.get(DefaultMethodCallback.DETAILS_KEY);
      errorCallback.onError(msg, details);
    }).updateConfigurationContext(databaseUuid, databaseUuid, status);
  }

  public void updateCollectionCustomizeProperties(String databaseUuid, CollectionStatus status,
    UpdateSuccessCallback successCallback) {
    CollectionService.Util.call((Boolean result) -> {
      successCallback.onSuccess();
    }).updateCollectionCustomizeProperties(databaseUuid, databaseUuid, status);
  }

  public void updateCollectionConfiguration(String databaseUuid, CollectionStatus status,
    UpdateSuccessCallback successCallback, ErrorCallback errorCallback) {
    CollectionService.Util.callDetailed((Boolean result) -> {
      successCallback.onSuccess();
    }, errorMessage -> {
      String msg = errorMessage.get(DefaultMethodCallback.MESSAGE_KEY);
      String details = errorMessage.get(DefaultMethodCallback.DETAILS_KEY);
      errorCallback.onError(msg, details);
    }).updateCollectionConfiguration(databaseUuid, databaseUuid, status);
  }
}
