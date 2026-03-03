package com.databasepreservation.common.server.batch.core;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class BatchConstants {
  // Configuration
  public static final String TASK_EXECUTOR_BEAN_NAME = "dbptkeTaskExecutor";
  public static final String TASK_EXECUTOR_THREAD_NAME_PREFIX = "DBPTKE-Batch-Worker-";
  public static final String JOB_LAUNCHER_BEAN_NAME = "dbptkeJobLauncher";

  // Partitioning
  public static final String PARTITION_PREFIX = "partition-";
  public static final String PARTITION_WORKER_NAME = "Worker";

  // Context keys
  public static final String DATABASE_UUID_KEY = "databaseUUID";
  public static final String COLLECTION_UUID_KEY = "databaseUUID";
  public static final String JOB_UUID_KEY = "jobUUID";
  public static final String TABLE_ID_KEY = "tableId";
  public static final String FILTER_KEY = "filter";
  public static final String FIELDS_KEY = "fields";
  public static final String DB_VERSION_KEY = "dbVersion";
  public static final String DB_PATH_KEY = "dbPath";
  public static final String DENORMALIZATION_ENTRY_ID_KEY = "denormalizeEntryID";
  public static final String JOB_DISPLAY_NAME_KEY = "jobDisplayName";
}
