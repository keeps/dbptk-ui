package com.databasepreservation.common.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.search.SavedSearch;
import com.databasepreservation.common.client.models.activity.logs.ActivityLogEntry;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.database.DatabaseStatus;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseValidationStatus;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.exceptions.ViewerException;
import com.databasepreservation.common.server.index.utils.JsonTransformer;
import com.databasepreservation.common.server.storage.fs.FSUtils;
import com.databasepreservation.common.utils.StatusUtils;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ConfigurationManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationManager.class);

  private final Object logFileLock = new Object();
  private final Object databaseStatusFileLock = new Object();
  private final Object collectionStatusFileLock = new Object();
  private long entryLogLineNumber = -1;

  public ConfigurationManager() {
  }

  public CollectionStatus getConfigurationCollection(String databaseUUID, String collectionUUID)
    throws GenericException {
    Path databasesDirectoryPath = ViewerFactory.getViewerConfiguration().getDatabasesPath();
    Path databaseDirectoryPath = databasesDirectoryPath.resolve(databaseUUID);
    Path collectionStatusFile = databaseDirectoryPath
      .resolve(ViewerConstants.SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX + collectionUUID + ".json");

    return JsonUtils.readObjectFromFile(collectionStatusFile, CollectionStatus.class);
  }

  public void editSearch(String databaseUUID, String uuid, String name, String description) {
    try {
      final CollectionStatus collectionStatus = getCollectionStatus(databaseUUID,
        ViewerConstants.SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX + databaseUUID);

      final SavedSearch savedSearch = collectionStatus.getSavedSearch(uuid);
      savedSearch.setName(name);
      savedSearch.setDescription(description);
      collectionStatus.updateSavedSearch(savedSearch);

      updateCollectionStatus(databaseUUID, collectionStatus);
    } catch (GenericException | ViewerException e) {
      LOGGER.debug("Failed to manipulate the JSON file", e);
    }
  }

  public void addSearch(SavedSearch savedSearch) {
    try {
      final CollectionStatus collectionStatus = getCollectionStatus(savedSearch.getDatabaseUUID(),
        ViewerConstants.SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX + savedSearch.getDatabaseUUID());
      collectionStatus.addSavedSearch(savedSearch);
      updateCollectionStatus(savedSearch.getDatabaseUUID(), collectionStatus);
    } catch (GenericException | ViewerException e) {
      LOGGER.debug("Failed to manipulate the JSON file", e);
    }
  }

  public void addTable(String databaseUUID, Collection<ViewerTable> tables) {
    try {
      final DatabaseStatus databaseStatus = getDatabaseStatus(databaseUUID);
      // At the moment there is only one collection per database
      if (databaseStatus.getCollections().size() >= 1) {
        final String collectionId = databaseStatus.getCollections().get(0);
        final CollectionStatus collectionStatus = getCollectionStatus(databaseUUID, collectionId);
        collectionStatus.setTables(StatusUtils.getTableStatusFromList(tables));
        // Update collection
        updateCollectionStatus(databaseUUID, collectionStatus);
      }
    } catch (GenericException | ViewerException e) {
      LOGGER.debug("Failed to manipulate the JSON file", e);
    }
  }

  public void addDenormalization(String databaseUUID, String denormalizationUUID) throws GenericException {
    try {
      final DatabaseStatus databaseStatus = getDatabaseStatus(databaseUUID);
      // At the moment there is only one collection per database
      if (databaseStatus.getCollections().size() >= 1) {
        final String collectionId = databaseStatus.getCollections().get(0);
        final CollectionStatus collectionStatus = getCollectionStatus(databaseUUID, collectionId);
        collectionStatus.addDenormalization(denormalizationUUID);
        // Update collection
        updateCollectionStatus(databaseUUID, collectionStatus);
      }
    } catch (GenericException | ViewerException e) {
      throw new GenericException("Failed to manipulate the JSON file", e);
    }
  }

  public void removeDenormalization(String databaseUUID, String denormalizationUUID) throws GenericException {
    try {
      final DatabaseStatus databaseStatus = getDatabaseStatus(databaseUUID);
      if (databaseStatus.getCollections().size() >= 1) {
        final String collectionId = databaseStatus.getCollections().get(0);
        final CollectionStatus collectionStatus = getCollectionStatus(databaseUUID, collectionId);
        collectionStatus.getDenormalizations().remove(denormalizationUUID);
        // Update collection
        updateCollectionStatus(databaseUUID, collectionStatus);
      }
    } catch (GenericException | ViewerException e) {
      throw new GenericException("Failed to manipulate the JSON file", e);
    }
  }

  public void addDenormalizationColumns(String databaseUUID, String tableUUID, ViewerColumn column,
    List<String> nestedId) throws GenericException {
    try {
      final DatabaseStatus databaseStatus = getDatabaseStatus(databaseUUID);
      if (databaseStatus.getCollections().size() >= 1) {
        final String collectionId = databaseStatus.getCollections().get(0);
        final CollectionStatus collectionStatus = getCollectionStatus(databaseUUID, collectionId);
        TableStatus table = collectionStatus.getTableStatus(tableUUID);
        // table.getColumns().removeIf(c -> !c.getNestedColumns().isEmpty());

        int order = table.getLastColumnOrder();
        ColumnStatus columnStatus = StatusUtils.getColumnStatus(column, false, ++order);
        columnStatus.getNestedColumns().addAll(nestedId);
        table.addColumnStatus(columnStatus);

        // Update collection
        updateCollectionStatus(databaseUUID, collectionStatus);
      }
    } catch (GenericException | ViewerException e) {
      throw new GenericException("Failed to manipulate the JSON file", e);
    }
  }

  public void removeDenormalizationColumns(String databaseUUID, String tableUUID) throws GenericException {
    try {
      final DatabaseStatus databaseStatus = getDatabaseStatus(databaseUUID);
      if (databaseStatus.getCollections().size() >= 1) {
        final String collectionId = databaseStatus.getCollections().get(0);
        final CollectionStatus collectionStatus = getCollectionStatus(databaseUUID, collectionId);
        TableStatus table = collectionStatus.getTableStatus(tableUUID);
        table.getColumns().removeIf(c -> !c.getNestedColumns().isEmpty());
        // Update collection
        updateCollectionStatus(databaseUUID, collectionStatus);
      }
    } catch (GenericException | ViewerException e) {
      throw new GenericException("Failed to manipulate the JSON file", e);
    }
  }

  public void addCollection(String databaseUUID, String solrCollectionName) {
    final CollectionStatus collectionStatus = StatusUtils.getCollectionStatus(solrCollectionName);

    try {
      final DatabaseStatus databaseStatus = getDatabaseStatus(databaseUUID);

      Path collectionFile = getCollectionStatusPath(databaseUUID, solrCollectionName);
      // verify if file exists
      if (!FSUtils.exists(collectionFile)) {
        try {
          Files.createFile(collectionFile);
        } catch (FileAlreadyExistsException e) {
          // do nothing (just caused due to concurrency)
        } catch (IOException e) {
          throw new GenericException("Error creating file to write the collection information", e);
        }

        // Save collection file and update database file
        JsonUtils.writeObjectToFile(collectionStatus, collectionFile);
        synchronized (databaseStatusFileLock) {
          databaseStatus.addBrowseCollection(solrCollectionName);
          updateDatabaseStatus(databaseStatus);
        }
      }
    } catch (GenericException | ViewerException e) {
      LOGGER.debug("Failed to manipulate the JSON file", e);
    }
  }

  private DatabaseStatus getDatabaseStatus(String databaseUUID) throws GenericException {
    synchronized (databaseStatusFileLock) {
      final Path databaseStatusFile = getDatabaseStatusPath(databaseUUID);
      return JsonUtils.readObjectFromFile(databaseStatusFile, DatabaseStatus.class);
    }
  }

  private CollectionStatus getCollectionStatus(String databaseUUID, String id) throws GenericException {
    synchronized (collectionStatusFileLock) {
      Path collectionStatusFile = getCollectionStatusPath(databaseUUID, id);
      return JsonUtils.readObjectFromFile(collectionStatusFile, CollectionStatus.class);
    }
  }

  private Path getDatabaseStatusPath(String id) {
    final Path databasesDirectoryPath = ViewerFactory.getViewerConfiguration().getDatabasesPath();
    final Path databaseDirectoryPath = databasesDirectoryPath.resolve(id);

    return databaseDirectoryPath.resolve(ViewerConstants.DATABASE_STATUS_PREFIX + id + ".json");
  }

  private Path getCollectionStatusPath(String databaseUUID, String id) {
    final Path databasesDirectoryPath = ViewerFactory.getViewerConfiguration().getDatabasesPath();
    final Path databaseDirectoryPath = databasesDirectoryPath.resolve(databaseUUID);

    return databaseDirectoryPath.resolve(id + ".json");
  }

  public void updateIndicators(String id, String passed, String failed, String warnings, String skipped) {
    synchronized (databaseStatusFileLock) {
      try {
        final Path databasesDirectoryPath = ViewerFactory.getViewerConfiguration().getDatabasesPath();
        final Path databaseDirectoryPath = databasesDirectoryPath.resolve(id);

        Path databaseFile = databaseDirectoryPath.resolve(ViewerConstants.DATABASE_STATUS_PREFIX + id + ".json");
        // verify if file exists
        if (FSUtils.exists(databaseFile)) {
          final DatabaseStatus databaseStatus = JsonUtils.readObjectFromFile(databaseFile, DatabaseStatus.class);
          databaseStatus.getValidationStatus()
            .setIndicators(StatusUtils.getIndicators(passed, failed, warnings, skipped));

          // update database file
          JsonTransformer.writeObjectToFile(databaseStatus, databaseFile);
        }
      } catch (GenericException | ViewerException e) {
        LOGGER.debug(e.getMessage(), e);
      }
    }
  }

  public void updateDatabaseStatus(DatabaseStatus status) throws ViewerException {
    synchronized (databaseStatusFileLock) {
      Path statusFile = getDatabaseStatusPath(status.getId());
      JsonTransformer.writeObjectToFile(status, statusFile);
    }
  }

  public void updateCollectionStatus(String databaseUUID, CollectionStatus status) throws ViewerException {
    synchronized (collectionStatusFileLock) {
      Path statusFile = getCollectionStatusPath(databaseUUID, status.getId());
      JsonTransformer.writeObjectToFile(status, statusFile);
    }
  }

  public void updateValidationStatus(String id, ViewerDatabaseValidationStatus status, String date,
    String validationReportPath, String dbptkVersion) {
    synchronized (databaseStatusFileLock) {
      try {
        final Path databasesDirectoryPath = ViewerFactory.getViewerConfiguration().getDatabasesPath();
        final Path databaseDirectoryPath = databasesDirectoryPath.resolve(id);

        Path databaseFile = databaseDirectoryPath.resolve(ViewerConstants.DATABASE_STATUS_PREFIX + id + ".json");

        // verify if file exists
        if (FSUtils.exists(databaseFile)) {
          final DatabaseStatus databaseStatus = JsonUtils.readObjectFromFile(databaseFile, DatabaseStatus.class);
          databaseStatus.setValidationStatus(StatusUtils.getValidationStatus(status, date, validationReportPath,
            dbptkVersion, databaseStatus.getValidationStatus().getIndicators()));

          JsonTransformer.writeObjectToFile(databaseStatus, databaseFile);
        }
      } catch (GenericException | ViewerException e) {
        LOGGER.debug(e.getMessage(), e);
      }
    }
  }

  public void addDatabase(ViewerDatabase database) throws GenericException {
    final Path databasesFolder = ViewerFactory.getViewerConfiguration().getDatabasesPath();
    Path databasePath = databasesFolder.resolve(database.getUuid());
    if (FSUtils.createDirectory(databasePath)) {
      String filename = ViewerConstants.DATABASE_STATUS_PREFIX + database.getUuid();
      Path databaseStatusPath = databasePath.resolve(filename + ".json");
      if (!FSUtils.exists(databaseStatusPath)) {
        try {
          Files.createFile(databaseStatusPath);
          // Write file
          JsonUtils.writeObjectToFile(StatusUtils.getDatabaseStatus(database), databaseStatusPath);

          addCollection(database.getUuid(), ViewerConstants.SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX + database.getUuid());
          addTable(database.getUuid(), database.getMetadata().getTables().values());
        } catch (FileAlreadyExistsException e) {
          // do nothing (just caused due to concurrency)
        } catch (IOException e) {
          throw new GenericException("Error creating file to write the database information", e);
        }
      }
    }
  }

  public void deleteDatabaseFolder(String databaseUUID) throws GenericException {
    final Path databasesDirectoryPath = ViewerFactory.getViewerConfiguration().getDatabasesPath();
    final Path databaseDirectoryPath = databasesDirectoryPath.resolve(databaseUUID);
    try {
      Files.walk(databaseDirectoryPath).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
      LOGGER.info("Database folder removed from system ({})", databaseDirectoryPath.toAbsolutePath());
    } catch (IOException e) {
      throw new GenericException("Could not delete the database folder for uuid: " + databaseUUID + " from the system",
        e);
    }
  }

  public void addLogEntry(ActivityLogEntry logEntry, Path logDirectory) throws GenericException {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    synchronized (logFileLock) {
      String filename = sdf.format(new Date());
      Path logFile = logDirectory.resolve(filename + ".log");

      // verify if file exists and if not creates the file
      if (!FSUtils.exists(logFile)) {
        entryLogLineNumber = 1;
        try {
          Files.createFile(logFile);
        } catch (FileAlreadyExistsException e) {
          // do nothing (just caused due to concurrency)
        } catch (IOException e) {
          throw new GenericException("Error creating file to write log into", e);
        }
      } else if (entryLogLineNumber == -1) {
        // recalculate entryLogLineNumber as file exists but no value is set
        // memory
        entryLogLineNumber = JsonUtils.calculateNumberOfLines(logFile) + 1;
      }

      // write to log file
      logEntry.setLineNumber(entryLogLineNumber);
      JsonUtils.appendObjectToFile(logEntry, logFile);
      entryLogLineNumber++;

      // write to Solr
      try {
        ViewerFactory.getSolrManager().addLogEntry(logEntry);
      } catch (NotFoundException | GenericException e) {
        // Do nothing
      }
    }
  }
}
