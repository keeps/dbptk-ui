package com.databasepreservation.common.server.batchv2.infra;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.databasepreservation.common.api.exceptions.IllegalAccessException;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.collection.VirtualColumnStatus;
import com.databasepreservation.common.exceptions.ViewerException;
import com.databasepreservation.common.server.ConfigurationManager;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.batchv2.common.TaskContext;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DefaultTaskContext implements TaskContext {
  private final String databaseUUID;
  private final ConfigurationManager configManager;

  private final Map<Class<?>, Object> configurations = new ConcurrentHashMap<>();

  public DefaultTaskContext(String databaseUUID) {
    this.databaseUUID = databaseUUID;
    this.configManager = ViewerFactory.getConfigurationManager();
  }

  public void addConfiguration(Object config) {
    this.configurations.put(config.getClass(), config);
  }

  @Override
  public <T> T getConfiguration(Class<T> clazz) {
    return clazz.cast(configurations.get(clazz));
  }

  @Override
  public CollectionStatus getCollectionStatus() {
    return getConfiguration(CollectionStatus.class);
  }

  @Override
  public String getDatabaseUUID() {
    return databaseUUID;
  }

  @Override
  public void updateExecutionStatus(String partitionId, Map<String, Object> trackingInfo, String exitStatus) {

    if (trackingInfo.containsKey("tableId") && trackingInfo.containsKey("columnId")) {
      updateVirtualColumnStatus((String) trackingInfo.get("tableId"), (String) trackingInfo.get("columnId"),
        exitStatus);
    } else if (trackingInfo.containsKey("denormalizationUUID")) {
      updateDenormalizationStatus((String) trackingInfo.get("denormalizationUUID"), exitStatus);
    }
  }

  private void updateVirtualColumnStatus(String tableId, String columnId, String exitStatus) {
    CollectionStatus status = getCollectionStatus();
    TableStatus table = status.getTableStatus(tableId);

    if (table != null) {
      ColumnStatus column = table.getColumnById(columnId);
      if (column != null && column.getVirtualColumnStatus() != null) {
        VirtualColumnStatus vcs = column.getVirtualColumnStatus();
        vcs.setLastExecutionDate(new Date());
        vcs.setExecutionStatus(exitStatus);

        try {
          configManager.updateCollectionStatus(databaseUUID, status);
        } catch (ViewerException | IllegalAccessException e) {
          // TODO: Log
          throw new RuntimeException(e);
        }
      }
    }
  }

  private void updateDenormalizationStatus(String denormUUID, String exitStatus) {
    // TODO
  }
}
