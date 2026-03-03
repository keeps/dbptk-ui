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

  /**
   * Retrieves the current step number being executed (1-based index). This is
   * primarily used for progress tracking in the UI (e.g., "Step 1 of N").
   * 
   * @return The current step number.
   */
  int getCurrentStepNumber();

  /**
   * Increments the current step counter by one. This should typically be called
   * by a step listener whenever a new step begins execution.
   */
  void incrementStepNumber();

  /**
   * Retrieves the total number of steps planned for execution in this job.
   * 
   * @return The total number of executable steps.
   */
  int getTotalSteps();

  /**
   * Sets the total number of steps planned for execution. This is usually
   * calculated dynamically by the orchestrator before the job starts, based on
   * step execution policies.
   * 
   * @param totalSteps
   *          The total number of executable steps.
   */
  void setTotalSteps(int totalSteps);

  /**
   * Retrieves the human-readable display name of the currently executing step.
   * 
   * @return The display name of the current step.
   */
  String getCurrentStepName();

  /**
   * Sets the human-readable display name of the currently executing step.
   * 
   * @param currentStepName
   *          The display name to be set (e.g., "Text Extraction").
   */
  void setCurrentStepName(String currentStepName);
}
