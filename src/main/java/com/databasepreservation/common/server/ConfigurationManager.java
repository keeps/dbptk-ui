/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.search.SavedSearch;
import com.databasepreservation.common.client.models.activity.logs.ActivityLogEntry;
import com.databasepreservation.common.client.models.authorization.AuthorizationDetails;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.NestedColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.database.DatabaseStatus;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseValidationStatus;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerType;
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
    return getConfigurationCollection(databaseUUID, collectionUUID, false);
  }

  public CollectionStatus getConfigurationCollection(String databaseUUID, String collectionUUID, boolean prefixed)
    throws GenericException {
    Path databasesDirectoryPath = ViewerFactory.getViewerConfiguration().getDatabasesPath();
    Path databaseDirectoryPath = databasesDirectoryPath.resolve(databaseUUID);
    Path collectionStatusFile;
    if (prefixed) {
      collectionStatusFile = databaseDirectoryPath.resolve(collectionUUID + ViewerConstants.JSON_EXTENSION);
    } else {
      collectionStatusFile = databaseDirectoryPath.resolve(
        ViewerConstants.SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX + collectionUUID + ViewerConstants.JSON_EXTENSION);
    }

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

  public void addTable(ViewerDatabase database) {
    try {
      final DatabaseStatus databaseStatus = getDatabaseStatus(database.getUuid());
      // At the moment there is only one collection per database
      if (databaseStatus.getCollections().size() >= 1) {
        final String collectionId = databaseStatus.getCollections().get(0);
        final CollectionStatus collectionStatus = getCollectionStatus(database.getUuid(), collectionId);
        collectionStatus.setTables(StatusUtils.getTableStatusFromList(database));
        // Update collection
        updateCollectionStatus(database.getUuid(), collectionStatus);
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
    NestedColumnStatus nestedId, String template, String originalType, String typeName, String nullable)
    throws GenericException {
    try {
      final DatabaseStatus databaseStatus = getDatabaseStatus(databaseUUID);
      if (!databaseStatus.getCollections().isEmpty()) {
        final String collectionId = databaseStatus.getCollections().get(0);
        final CollectionStatus collectionStatus = getCollectionStatus(databaseUUID, collectionId);
        TableStatus table = collectionStatus.getTableStatus(tableUUID);

        int order = table.getLastColumnOrder();
        ColumnStatus columnStatus = StatusUtils.getColumnStatus(column, true, ++order);
        columnStatus.setNestedColumns(nestedId);
        columnStatus.setOriginalType(originalType);
        columnStatus.setTypeName(typeName);
        columnStatus.setNullable(nullable);
        columnStatus.setType(ViewerType.dbTypes.NESTED);
        table.addColumnStatus(columnStatus);
        columnStatus.getExportStatus().getTemplateStatus().setTemplate(template);
        columnStatus.getDetailsStatus().getTemplateStatus().setTemplate(template);
        columnStatus.getSearchStatus().getList().getTemplate().setTemplate(template);

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
        table.getColumns().removeIf(c -> c.getNestedColumns() != null);
        table.reorderColumns();
        // Update collection
        updateCollectionStatus(databaseUUID, collectionStatus);
      }
    } catch (GenericException | ViewerException e) {
      throw new GenericException("Failed to manipulate the JSON file", e);
    }
  }

  public void addCollection(String databaseUUID, String databaseName, String databaseDescription,
    String solrCollectionName) {
    final CollectionStatus collectionStatus = StatusUtils.getCollectionStatus(databaseUUID, databaseName,
      databaseDescription, solrCollectionName);

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

  public DatabaseStatus getDatabaseStatus(String databaseUUID) throws GenericException {
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

    return databaseDirectoryPath.resolve(ViewerConstants.DATABASE_STATUS_PREFIX + id + ViewerConstants.JSON_EXTENSION);
  }

  private Path getCollectionStatusPath(String databaseUUID, String id) {
    final Path databasesDirectoryPath = ViewerFactory.getViewerConfiguration().getDatabasesPath();
    final Path databaseDirectoryPath = databasesDirectoryPath.resolve(databaseUUID);

    return databaseDirectoryPath.resolve(id + ViewerConstants.JSON_EXTENSION);
  }

  public void updateIndicators(String id, String passed, String failed, String warnings, String skipped) {
    synchronized (databaseStatusFileLock) {
      try {
        final Path databasesDirectoryPath = ViewerFactory.getViewerConfiguration().getDatabasesPath();
        final Path databaseDirectoryPath = databasesDirectoryPath.resolve(id);

        Path databaseFile = databaseDirectoryPath
          .resolve(ViewerConstants.DATABASE_STATUS_PREFIX + id + ViewerConstants.JSON_EXTENSION);
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

  public void updateDatabasePermissions(String databaseId, Map<String, AuthorizationDetails> permissions)
    throws GenericException, ViewerException {
    synchronized (databaseStatusFileLock) {
      try {
        final Path databasesDirectoryPath = ViewerFactory.getViewerConfiguration().getDatabasesPath();
        final Path databaseDirectoryPath = databasesDirectoryPath.resolve(databaseId);

        Path databaseFile = databaseDirectoryPath
          .resolve(ViewerConstants.DATABASE_STATUS_PREFIX + databaseId + ViewerConstants.JSON_EXTENSION);

        // verify if file exists
        if (FSUtils.exists(databaseFile)) {
          final DatabaseStatus databaseStatus = JsonUtils.readObjectFromFile(databaseFile, DatabaseStatus.class);
          databaseStatus.setPermissions(permissions);

          // update database file
          JsonTransformer.writeObjectToFile(databaseStatus, databaseFile);
        }
      } catch (GenericException | ViewerException e) {
        LOGGER.debug(e.getMessage(), e);
        throw e;
      }
    }
  }

  public void updateDatabaseSearchAllAvailability(String databaseId, boolean isAvailableToSearchAll)
    throws GenericException, ViewerException {
    synchronized (databaseStatusFileLock) {
      try {
        final Path databasesDirectoryPath = ViewerFactory.getViewerConfiguration().getDatabasesPath();
        final Path databaseDirectoryPath = databasesDirectoryPath.resolve(databaseId);

        Path databaseFile = databaseDirectoryPath
          .resolve(ViewerConstants.DATABASE_STATUS_PREFIX + databaseId + ViewerConstants.JSON_EXTENSION);

        // verify if file exists
        if (FSUtils.exists(databaseFile)) {
          final DatabaseStatus databaseStatus = JsonUtils.readObjectFromFile(databaseFile, DatabaseStatus.class);
          databaseStatus.setAvailableToSearchAll(isAvailableToSearchAll);

          // update database file
          JsonTransformer.writeObjectToFile(databaseStatus, databaseFile);
        }
      } catch (GenericException | ViewerException e) {
        LOGGER.debug(e.getMessage(), e);
        throw e;
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

  public void updateCollectionStatus(String databaseUUID, ViewerMetadata metadata, boolean updateOnModel)
    throws GenericException, ViewerException {
    List<TableStatus> list = StatusUtils.getTableStatusFromList(metadata);

    CollectionStatus collectionStatus = getCollectionStatus(databaseUUID,
      ViewerConstants.SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX + databaseUUID);

    if (updateOnModel) {
      collectionStatus.setName(metadata.getName());
      collectionStatus.setDescription(metadata.getDescription());
    }

    for (TableStatus table : list) {
      collectionStatus.getTableStatusByTableId(table.getId()).setDescription(table.getDescription());
      if (updateOnModel) {
        collectionStatus.getTableStatusByTableId(table.getId()).setCustomDescription(table.getDescription());
      }
      for (ColumnStatus column : table.getColumns()) {
        collectionStatus.getColumnByTableIdAndColumn(table.getId(), column.getId())
          .setDescription(column.getDescription());
        if (updateOnModel) {
          collectionStatus.getColumnByTableIdAndColumn(table.getId(), column.getId())
            .setCustomDescription(column.getDescription());
        }
      }
    }

    updateCollectionStatus(databaseUUID, collectionStatus);
  }

  public void updateValidationStatus(String id, ViewerDatabaseValidationStatus status, String date,
    String validationReportPath, String dbptkVersion) {
    synchronized (databaseStatusFileLock) {
      try {
        final Path databasesDirectoryPath = ViewerFactory.getViewerConfiguration().getDatabasesPath();
        final Path databaseDirectoryPath = databasesDirectoryPath.resolve(id);

        Path databaseFile = databaseDirectoryPath
          .resolve(ViewerConstants.DATABASE_STATUS_PREFIX + id + ViewerConstants.JSON_EXTENSION);

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
      Path databaseStatusPath = databasePath.resolve(filename + ViewerConstants.JSON_EXTENSION);
      if (!FSUtils.exists(databaseStatusPath)) {
        try {
          Files.createFile(databaseStatusPath);
          // Write file
          JsonUtils.writeObjectToFile(StatusUtils.getDatabaseStatus(database), databaseStatusPath);

          addCollection(database.getUuid(), database.getMetadata().getName(), database.getMetadata().getDescription(),
            ViewerConstants.SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX + database.getUuid());
          addTable(database);
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

  public void deleteCollection(String databaseUUID, String collectionUUID) throws GenericException {
    final Path databasesDirectoryPath = ViewerFactory.getViewerConfiguration().getDatabasesPath();
    final Path databaseDirectoryPath = databasesDirectoryPath.resolve(databaseUUID);

    final Path denormalizationFilePath = databaseDirectoryPath.resolve(collectionUUID + ViewerConstants.JSON_EXTENSION);

    try {
      Files.deleteIfExists(denormalizationFilePath);
    } catch (IOException e) {
      throw new GenericException(
        "Could not delete the collection file " + collectionUUID + ViewerConstants.JSON_EXTENSION + " from the system",
        e);
    }
  }

  public void deleteDenormalizationFromCollection(String databaseUUID, String denormalizationUUID)
    throws GenericException {
    final Path databasesDirectoryPath = ViewerFactory.getViewerConfiguration().getDatabasesPath();
    final Path databaseDirectoryPath = databasesDirectoryPath.resolve(databaseUUID);

    final Path denormalizationFilePath = databaseDirectoryPath
      .resolve(denormalizationUUID + ViewerConstants.JSON_EXTENSION);

    try {
      Files.deleteIfExists(denormalizationFilePath);
    } catch (IOException e) {
      throw new GenericException("Could not delete the denormalization file " + denormalizationUUID
        + ViewerConstants.JSON_EXTENSION + " from the system", e);
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
