package com.databasepreservation.common.server.batchv2.registry;

import org.springframework.stereotype.Component;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.server.ConfigurationManager;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.batchv2.infra.DefaultTaskContext;
import com.databasepreservation.common.server.batchv2.common.TaskContext;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class ContextResolver {

  public TaskContext resolve(String databaseUUID) {
    ConfigurationManager configManager = ViewerFactory.getConfigurationManager();

    try {
      CollectionStatus collectionStatus = configManager.getConfigurationCollection(databaseUUID, databaseUUID);

      DefaultTaskContext context = new DefaultTaskContext(databaseUUID);
      context.addConfiguration(collectionStatus);

      return context;

    } catch (Exception e) {
      throw new RuntimeException("Failed to resolve TaskContext for database UUID: " + databaseUUID, e);
    }
  }
}
