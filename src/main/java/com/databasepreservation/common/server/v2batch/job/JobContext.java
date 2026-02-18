package com.databasepreservation.common.server.v2batch.job;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface JobContext {
  JobProgressAggregator getJobProgressAggregator();

  CollectionStatus getCollectionStatus();

  String getDatabaseUUID();

  DenormalizeConfiguration getDenormalizeConfig(String entryID);
}
