package com.databasepreservation.common.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import com.databasepreservation.common.client.models.structure.ViewerType;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.search.SavedSearch;
import com.databasepreservation.common.client.models.activity.logs.ActivityLogEntry;
import com.databasepreservation.common.client.models.configuration.collection.ViewerCollectionConfiguration;
import com.databasepreservation.common.client.models.configuration.collection.ViewerColumnConfiguration;
import com.databasepreservation.common.client.models.configuration.collection.ViewerNestedColumnConfiguration;
import com.databasepreservation.common.client.models.configuration.collection.ViewerTableConfiguration;
import com.databasepreservation.common.client.models.configuration.database.ViewerDatabaseConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseValidationStatus;
import com.databasepreservation.common.exceptions.ViewerException;
import com.databasepreservation.common.server.index.utils.JsonTransformer;
import com.databasepreservation.common.server.storage.fs.FSUtils;
import com.databasepreservation.common.utils.ViewerConfigurationUtils;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ConfigurationManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationManager.class);

  private final Object logFileLock = new Object();
  private final Object databaseConfigurationFileLock = new Object();
  private final Object collectionConfigurationFileLock = new Object();
  private long entryLogLineNumber = -1;

  public ConfigurationManager() {
  }

  public ViewerCollectionConfiguration getViewerCollectionConfiguration(String databaseUUID, String collectionUUID)
    throws GenericException {
    Path databasesDirectoryPath = ViewerFactory.getViewerConfiguration().getDatabasesPath();
    Path databaseDirectoryPath = databasesDirectoryPath.resolve(databaseUUID);
    Path collectionStatusFile = databaseDirectoryPath
      .resolve(ViewerConstants.SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX + collectionUUID + ".json");

    return JsonUtils.readObjectFromFile(collectionStatusFile, ViewerCollectionConfiguration.class);
  }

  public void editSearch(String databaseUUID, String uuid, String name, String description) {
    try {
      final ViewerCollectionConfiguration viewerCollectionConfiguration = getCollectionConfiguration(databaseUUID,
        ViewerConstants.SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX + databaseUUID);

      final SavedSearch savedSearch = viewerCollectionConfiguration.getSavedSearch(uuid);
      savedSearch.setName(name);
      savedSearch.setDescription(description);
      viewerCollectionConfiguration.updateSavedSearch(savedSearch);

      updateCollectionConfiguration(databaseUUID, viewerCollectionConfiguration);
    } catch (GenericException | ViewerException e) {
      LOGGER.debug("Failed to manipulate the JSON file", e);
    }
  }

  public void addSearch(SavedSearch savedSearch) {
    try {
      final ViewerCollectionConfiguration viewerCollectionConfiguration = getCollectionConfiguration(savedSearch.getDatabaseUUID(),
        ViewerConstants.SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX + savedSearch.getDatabaseUUID());
      viewerCollectionConfiguration.addSavedSearch(savedSearch);
      updateCollectionConfiguration(savedSearch.getDatabaseUUID(), viewerCollectionConfiguration);
    } catch (GenericException | ViewerException e) {
      LOGGER.debug("Failed to manipulate the JSON file", e);
    }
  }

  public void addTable(ViewerDatabase database) {
    try {
      final ViewerDatabaseConfiguration viewerDatabaseConfiguration = getDatabaseConfiguration(database.getUuid());
      // At the moment there is only one collection per database
      if (viewerDatabaseConfiguration.getCollections().size() >= 1) {
        final String collectionId = viewerDatabaseConfiguration.getCollections().get(0);
        final ViewerCollectionConfiguration viewerCollectionConfiguration = getCollectionConfiguration(database.getUuid(), collectionId);
        viewerCollectionConfiguration.setTables(ViewerConfigurationUtils.getTableConfigurationFromList(database));
        // Update collection
        updateCollectionConfiguration(database.getUuid(), viewerCollectionConfiguration);
      }
    } catch (GenericException | ViewerException e) {
      LOGGER.debug("Failed to manipulate the JSON file", e);
    }
  }

  public void addDenormalization(String databaseUUID, String denormalizationUUID) throws GenericException {
    try {
      final ViewerDatabaseConfiguration viewerDatabaseConfiguration = getDatabaseConfiguration(databaseUUID);
      // At the moment there is only one collection per database
      if (viewerDatabaseConfiguration.getCollections().size() >= 1) {
        final String collectionId = viewerDatabaseConfiguration.getCollections().get(0);
        final ViewerCollectionConfiguration viewerCollectionConfiguration = getCollectionConfiguration(databaseUUID, collectionId);
        viewerCollectionConfiguration.addDenormalization(denormalizationUUID);
        // Update collection
        updateCollectionConfiguration(databaseUUID, viewerCollectionConfiguration);
      }
    } catch (GenericException | ViewerException e) {
      throw new GenericException("Failed to manipulate the JSON file", e);
    }
  }

  public void removeDenormalization(String databaseUUID, String denormalizationUUID) throws GenericException {
    try {
      final ViewerDatabaseConfiguration viewerDatabaseConfiguration = getDatabaseConfiguration(databaseUUID);
      if (viewerDatabaseConfiguration.getCollections().size() >= 1) {
        final String collectionId = viewerDatabaseConfiguration.getCollections().get(0);
        final ViewerCollectionConfiguration viewerCollectionConfiguration = getCollectionConfiguration(databaseUUID, collectionId);
        viewerCollectionConfiguration.getDenormalizations().remove(denormalizationUUID);
        // Update collection
        updateCollectionConfiguration(databaseUUID, viewerCollectionConfiguration);
      }
    } catch (GenericException | ViewerException e) {
      throw new GenericException("Failed to manipulate the JSON file", e);
    }
  }

  public void addDenormalizationColumns(String databaseUUID, String tableUUID, ViewerColumn column,
                                        ViewerNestedColumnConfiguration nestedId, String template, String originalType, String typeName, String nullable)
    throws GenericException {
    try {
      final ViewerDatabaseConfiguration viewerDatabaseConfiguration = getDatabaseConfiguration(databaseUUID);
      if (!viewerDatabaseConfiguration.getCollections().isEmpty()) {
        final String collectionId = viewerDatabaseConfiguration.getCollections().get(0);
        final ViewerCollectionConfiguration viewerCollectionConfiguration = getCollectionConfiguration(databaseUUID, collectionId);
        ViewerTableConfiguration table = viewerCollectionConfiguration.getViewerTableConfiguration(tableUUID);

        int order = table.getLastColumnOrder();
        ViewerColumnConfiguration viewerColumnConfiguration = ViewerConfigurationUtils.getColumnConfiguration(column, true, ++order);
        viewerColumnConfiguration.setNestedColumns(nestedId);
        viewerColumnConfiguration.setOriginalType(originalType);
        viewerColumnConfiguration.setTypeName(typeName);
        viewerColumnConfiguration.setNullable(nullable);
        viewerColumnConfiguration.setType(ViewerType.dbTypes.NESTED);
        table.addColumnStatus(viewerColumnConfiguration);
        viewerColumnConfiguration.getViewerExportConfiguration().getViewerTemplateConfiguration().setTemplate(template);
        viewerColumnConfiguration.getViewerDetailsConfiguration().getViewerTemplateConfiguration().setTemplate(template);
        viewerColumnConfiguration.getViewerSearchConfiguration().getList().getTemplate().setTemplate(template);

        // Update collection
        updateCollectionConfiguration(databaseUUID, viewerCollectionConfiguration);
      }
    } catch (GenericException | ViewerException e) {
      throw new GenericException("Failed to manipulate the JSON file", e);
    }
  }

  public void removeDenormalizationColumns(String databaseUUID, String tableUUID) throws GenericException {
    try {
      final ViewerDatabaseConfiguration viewerDatabaseConfiguration = getDatabaseConfiguration(databaseUUID);
      if (viewerDatabaseConfiguration.getCollections().size() >= 1) {
        final String collectionId = viewerDatabaseConfiguration.getCollections().get(0);
        final ViewerCollectionConfiguration viewerCollectionConfiguration = getCollectionConfiguration(databaseUUID, collectionId);
        ViewerTableConfiguration table = viewerCollectionConfiguration.getViewerTableConfiguration(tableUUID);
        table.getColumns().removeIf(c -> c.getNestedColumns() != null);
        table.reorderColumns();
        // Update collection
        updateCollectionConfiguration(databaseUUID, viewerCollectionConfiguration);
      }
    } catch (GenericException | ViewerException e) {
      throw new GenericException("Failed to manipulate the JSON file", e);
    }
  }

  public void addCollection(String databaseUUID, String solrCollectionName) {
    final ViewerCollectionConfiguration viewerCollectionConfiguration = ViewerConfigurationUtils.getCollectionConfiguration(databaseUUID, solrCollectionName);

    try {
      final ViewerDatabaseConfiguration viewerDatabaseConfiguration = getDatabaseConfiguration(databaseUUID);

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
        JsonUtils.writeObjectToFile(viewerCollectionConfiguration, collectionFile);
        synchronized (databaseConfigurationFileLock) {
          viewerDatabaseConfiguration.addBrowseCollection(solrCollectionName);
          updateDatabaseConfiguration(viewerDatabaseConfiguration);
        }
      }
    } catch (GenericException | ViewerException e) {
      LOGGER.debug("Failed to manipulate the JSON file", e);
    }
  }

  private ViewerDatabaseConfiguration getDatabaseConfiguration(String databaseUUID) throws GenericException {
    synchronized (databaseConfigurationFileLock) {
      final Path databaseStatusFile = getDatabaseConfigurationPath(databaseUUID);
      return JsonUtils.readObjectFromFile(databaseStatusFile, ViewerDatabaseConfiguration.class);
    }
  }

  private ViewerCollectionConfiguration getCollectionConfiguration(String databaseUUID, String id) throws GenericException {
    synchronized (collectionConfigurationFileLock) {
      Path collectionStatusFile = getCollectionStatusPath(databaseUUID, id);
      return JsonUtils.readObjectFromFile(collectionStatusFile, ViewerCollectionConfiguration.class);
    }
  }

  private Path getDatabaseConfigurationPath(String id) {
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
    synchronized (databaseConfigurationFileLock) {
      try {
        final Path databasesDirectoryPath = ViewerFactory.getViewerConfiguration().getDatabasesPath();
        final Path databaseDirectoryPath = databasesDirectoryPath.resolve(id);

        Path databaseFile = databaseDirectoryPath.resolve(ViewerConstants.DATABASE_STATUS_PREFIX + id + ".json");
        // verify if file exists
        if (FSUtils.exists(databaseFile)) {
          final ViewerDatabaseConfiguration viewerDatabaseConfiguration = JsonUtils.readObjectFromFile(databaseFile, ViewerDatabaseConfiguration.class);
          viewerDatabaseConfiguration.getViewerValidationConfiguration()
            .setViewerValidationIndicators(ViewerConfigurationUtils.getIndicators(passed, failed, warnings, skipped));

          // update database file
          JsonTransformer.writeObjectToFile(viewerDatabaseConfiguration, databaseFile);
        }
      } catch (GenericException | ViewerException e) {
        LOGGER.debug(e.getMessage(), e);
      }
    }
  }

  public void updateDatabaseConfiguration(ViewerDatabaseConfiguration status) throws ViewerException {
    synchronized (databaseConfigurationFileLock) {
      Path statusFile = getDatabaseConfigurationPath(status.getId());
      JsonTransformer.writeObjectToFile(status, statusFile);
    }
  }

  public void updateCollectionConfiguration(String databaseUUID, ViewerCollectionConfiguration configuration) throws ViewerException {
    synchronized (collectionConfigurationFileLock) {
      Path path = getCollectionStatusPath(databaseUUID, configuration.getId());
      JsonTransformer.writeObjectToFile(configuration, path);
    }
  }

  public void updateValidationStatus(String id, ViewerDatabaseValidationStatus validationStatus, String date,
    String validationReportPath, String dbptkVersion) {
    synchronized (databaseConfigurationFileLock) {
      try {
        final Path databasesDirectoryPath = ViewerFactory.getViewerConfiguration().getDatabasesPath();
        final Path databaseDirectoryPath = databasesDirectoryPath.resolve(id);

        Path databaseFile = databaseDirectoryPath.resolve(ViewerConstants.DATABASE_STATUS_PREFIX + id + ".json");

        // verify if file exists
        if (FSUtils.exists(databaseFile)) {
          final ViewerDatabaseConfiguration viewerDatabaseConfiguration = JsonUtils.readObjectFromFile(databaseFile, ViewerDatabaseConfiguration.class);
          viewerDatabaseConfiguration.setViewerValidationConfiguration(ViewerConfigurationUtils.getValidationConfiguration(validationStatus, date, validationReportPath,
            dbptkVersion, viewerDatabaseConfiguration.getViewerValidationConfiguration().getViewerValidationIndicators()));

          JsonTransformer.writeObjectToFile(viewerDatabaseConfiguration, databaseFile);
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
          JsonUtils.writeObjectToFile(ViewerConfigurationUtils.getDatabaseConfiguration(database), databaseStatusPath);

          addCollection(database.getUuid(), ViewerConstants.SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX + database.getUuid());
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
