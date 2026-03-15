package com.databasepreservation.common.server.batch.steps.virtual;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.exceptions.BatchJobException;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Service
public class VirtualEntityMetadataService {
  private static final Logger LOGGER = LoggerFactory.getLogger(VirtualEntityMetadataService.class);
  private final DatabaseRowsSolrManager solrManager;

  public VirtualEntityMetadataService(DatabaseRowsSolrManager solrManager) {
    this.solrManager = solrManager;
  }

  public void updateAndPersistMetadata(JobContext jobContext, Consumer<ViewerMetadata> metadataMutator)
    throws BatchJobException {
    String databaseUUID = jobContext.getDatabaseUUID();
    LOGGER.info("Starting central metadata synchronization for database: {}", databaseUUID);

    try {
      ViewerDatabase database = solrManager.retrieve(ViewerDatabase.class, databaseUUID);
      ViewerMetadata metadata = database.getMetadata();

      // Execute the specific step logic
      metadataMutator.accept(metadata);

      LOGGER.debug("Writing updated metadata to Solr...");
      solrManager.updateDatabaseMetadata(databaseUUID, metadata);

      LOGGER.debug("Writing CollectionStatus to local disk...");
      ViewerFactory.getConfigurationManager().updateCollectionStatus(databaseUUID, jobContext.getCollectionStatus());

      LOGGER.info("Metadata successfully synchronized and persisted for database: {}", databaseUUID);
    } catch (Exception e) {
      LOGGER.error("CRITICAL: Failed to update metadata centrally for database: {}", databaseUUID, e);
      throw new BatchJobException("Failed to update metadata centrally for DB: " + databaseUUID, e);
    }
  }
}
