package com.databasepreservation.common.server.v2batch.job;

import com.databasepreservation.common.server.v2batch.exceptions.BatchJobException;
import org.roda.core.data.exceptions.GenericException;
import org.springframework.stereotype.Component;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.server.ConfigurationManager;
import com.databasepreservation.common.server.ViewerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class ContextResolver {

  public JobContext resolve(String databaseUUID) throws BatchJobException {
    ConfigurationManager configManager = ViewerFactory.getConfigurationManager();

    try {
      CollectionStatus collectionStatus = configManager.getConfigurationCollection(databaseUUID, databaseUUID);

      return new DefaultJobContext(databaseUUID, collectionStatus);
    } catch (GenericException e) {
      throw new BatchJobException("Failed to resolve TaskContext for database UUID: " + databaseUUID, e);
    }
  }
}
