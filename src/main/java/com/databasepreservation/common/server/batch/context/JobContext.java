package com.databasepreservation.common.server.batch.context;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.server.batch.core.JobProgressAggregator;

/**
 * Provides access to job-level configuration, global status, and progress
 * tracking shared across steps.
 * 
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface JobContext {
  /**
   * @return The UUID of the database being processed.
   */
  String getDatabaseUUID();

  /**
   * @return The current status of the collection and its structure.
   */
  CollectionStatus getCollectionStatus();

  /**
   * @return The aggregator used to update and track global job progress.
   */
  JobProgressAggregator getJobProgressAggregator();

  /**
   * Retrieves specific denormalization settings for a given ID.
   * 
   * @param entryID
   *          The identifier for the denormalization entry.
   */
  DenormalizeConfiguration getDenormalizeConfig(String entryID);
}
