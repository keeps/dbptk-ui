package com.databasepreservation.common.server.v2batch.job;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.v2batch.exceptions.BatchJobException;
import org.roda.core.data.exceptions.GenericException;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DefaultJobContext implements JobContext {
  private final String databaseUUID;
  private final CollectionStatus collectionStatus;
  private final JobProgressAggregator progressAggregator;

  private final Map<String, DenormalizeConfiguration> denormalizeConfigs;

  public DefaultJobContext(String databaseUUID, CollectionStatus collectionStatus) throws BatchJobException {
    this.databaseUUID = databaseUUID;
    this.collectionStatus = collectionStatus;
    this.progressAggregator = new JobProgressAggregator();
    this.denormalizeConfigs = new HashMap<>();

    loadDenormalizationConfigs();
  }

  private void loadDenormalizationConfigs() throws BatchJobException {
    Set<String> entries = collectionStatus.getDenormalizations();

    if (entries == null) {
      return;
    }

    for (String entryID : entries) {
      try {
        DenormalizeConfiguration config = ViewerFactory.getConfigurationManager()
          .getDenormalizeConfigurationFromCollectionStatusEntry(databaseUUID, entryID);

        if (config != null) {
          denormalizeConfigs.put(entryID, config);
        }
      } catch (GenericException e) {
        throw new BatchJobException("Failed to load denormalization config for entryID: " + entryID, e);
      }
    }
  }

  @Override
  public String getDatabaseUUID() {
    return databaseUUID;
  }

  @Override
  public CollectionStatus getCollectionStatus() {
    return collectionStatus;
  }

  @Override
  public JobProgressAggregator getJobProgressAggregator() {
    return progressAggregator;
  }

  @Override
  public DenormalizeConfiguration getDenormalizeConfig(String entryID) {
    return denormalizeConfigs.get(entryID);
  }
}
