package com.databasepreservation.common.server.batch.context;

import java.util.List;
import java.util.function.Consumer;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
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
   * Retrieves the "Shadow Database" kept in memory during the job. Contains the
   * metadata schema, version, and path without needing Solr calls.
   */
  ViewerDatabase getViewerDatabase();

  /**
   *
   * Safe and synchronized way for Worker Steps to inject structures into the
   * Shadow Schema.
   */
  void changeViewerDatabase(Consumer<ViewerDatabase> consumer);

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
   * Retrieves the list of human-readable step names corresponding to the steps
   * defined in the job. This is used for UI display purposes (e.g., showing "Step
   * 1: Text Extraction", "Step 2: Denormalization", etc.).
   * 
   * @return A list of step display names in the order they are executed.
   */
  List<String> getStepNames();

  /**
   * Sets the list of human-readable step names corresponding to the steps defined
   * in the job. This should be called by the orchestrator before the job starts,
   * after determining the execution plan and order of steps.
   * 
   * @param stepNames
   *          A list of step display names in the order they are executed.
   */
  void setStepNames(List<String> stepNames);

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
