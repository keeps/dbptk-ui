package com.databasepreservation.common.server.batch.context;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.springframework.stereotype.Component;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.server.ConfigurationManager;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.batch.exceptions.BatchJobException;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class ContextResolver {

  private final DatabaseRowsSolrManager solrManager;

  public ContextResolver(DatabaseRowsSolrManager solrManager) {
    this.solrManager = solrManager;
  }

  public JobContext resolve(String databaseUUID) throws BatchJobException {
    ConfigurationManager configManager = ViewerFactory.getConfigurationManager();

    try {
      CollectionStatus collectionStatus = configManager.getConfigurationCollection(databaseUUID, databaseUUID);
      ViewerDatabase database = solrManager.retrieve(ViewerDatabase.class, databaseUUID);

      return new DefaultJobContext(databaseUUID, database, collectionStatus);
    } catch (GenericException | NotFoundException e) {
      throw new BatchJobException("Failed to resolve JobContext for database UUID: " + databaseUUID, e);
    }
  }
}
