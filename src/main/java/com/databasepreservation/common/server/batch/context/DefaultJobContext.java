package com.databasepreservation.common.server.batch.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.roda.core.data.exceptions.GenericException;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.batch.core.JobProgressAggregator;
import com.databasepreservation.common.server.batch.exceptions.BatchJobException;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DefaultJobContext implements JobContext {
  private final String databaseUUID;
  private final ViewerDatabase viewerDatabase;
  private final CollectionStatus collectionStatus;
  private final JobProgressAggregator progressAggregator;
  private final Map<String, DenormalizeConfiguration> denormalizeConfigs;

  private final List<String> stepNames = new ArrayList<>();
  private final AtomicInteger currentStepNumber = new AtomicInteger(0);
  private final AtomicInteger totalSteps = new AtomicInteger(1);
  private volatile String currentStepName = "";

  public DefaultJobContext(String databaseUUID, ViewerDatabase viewerDatabase, CollectionStatus collectionStatus)
    throws BatchJobException {
    this.databaseUUID = databaseUUID;
    this.viewerDatabase = viewerDatabase;
    this.collectionStatus = collectionStatus;
    this.progressAggregator = new JobProgressAggregator();
    this.denormalizeConfigs = new HashMap<>();

    loadDenormalizationConfigs();
  }

  private void loadDenormalizationConfigs() throws BatchJobException {
    Set<String> entries = collectionStatus.getDenormalizations();
    if (entries == null)
      return;

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
  public synchronized ViewerDatabase getViewerDatabase() {
    return viewerDatabase;
  }

  @Override
  public synchronized void changeViewerDatabase(Consumer<ViewerDatabase> consumer) {
    if (this.viewerDatabase != null) {
      consumer.accept(this.viewerDatabase);

      // Force reconstruction of internal maps and structures to reflect changes
      this.viewerDatabase.getMetadata().setSchemas(this.viewerDatabase.getMetadata().getSchemas());
    }
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

  @Override
  public List<String> getStepNames() {
    return stepNames;
  }

  @Override
  public void setStepNames(List<String> stepNames) {
    this.stepNames.clear();
    if (stepNames != null) {
      this.stepNames.addAll(stepNames);
    }
  }

  @Override
  public int getCurrentStepNumber() {
    return currentStepNumber.get();
  }

  @Override
  public void incrementStepNumber() {
    this.currentStepNumber.incrementAndGet();
  }

  @Override
  public int getTotalSteps() {
    return totalSteps.get();
  }

  @Override
  public void setTotalSteps(int totalSteps) {
    this.totalSteps.set(totalSteps);
  }

  @Override
  public String getCurrentStepName() {
    return currentStepName;
  }

  @Override
  public void setCurrentStepName(String currentStepName) {
    this.currentStepName = currentStepName;
  }
}
