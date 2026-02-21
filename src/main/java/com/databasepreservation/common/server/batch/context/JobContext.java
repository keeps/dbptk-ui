package com.databasepreservation.common.server.batch.context;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.server.batch.core.JobProgressAggregator;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface JobContext {
  String getDatabaseUUID();

  CollectionStatus getCollectionStatus();

  JobProgressAggregator getJobProgressAggregator();

  DenormalizeConfiguration getDenormalizeConfig(String entryID);
}
