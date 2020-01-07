package com.databasepreservation.common.server;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.models.activity.logs.ActivityLogEntry;
import com.databasepreservation.common.client.models.status.database.DatabaseStatus;
import com.databasepreservation.common.client.models.status.database.Indicators;
import com.databasepreservation.common.client.models.status.database.ValidationStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseValidationStatus;
import com.databasepreservation.common.exceptions.ViewerException;
import com.databasepreservation.common.server.index.utils.JsonTransformer;
import com.databasepreservation.common.server.storage.fs.FSUtils;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ConfigurationManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationManager.class);

  private final Object logFileLock = new Object();
  private final Object databaseStatusFileLock = new Object();
  private long entryLogLineNumber = -1;

  public ConfigurationManager() {
  }

  public void updateIndicators(String id, Indicators indicators, Path databaseStatusDirectory) {
    synchronized (databaseStatusFileLock) {
      try {
        Path statusFile = databaseStatusDirectory.resolve(id + ".json");
        // verify if file exists
        if (FSUtils.exists(statusFile)) {
          final DatabaseStatus databaseStatus = JsonUtils.readObjectFromFile(statusFile, DatabaseStatus.class);
          databaseStatus.getValidationStatus().setIndicators(indicators);

          // Save backup
          JsonTransformer.writeObjectToFile(databaseStatus, statusFile);
        }
      } catch (GenericException | ViewerException e) {
        LOGGER.debug(e.getMessage(), e);
      }
    }
  }

  public void updateValidationStatus(String id, ViewerDatabaseValidationStatus status, String date,
    String validationReportPath, String dbptkVersion, Path databaseStatusDirectory) {
    synchronized (databaseStatusFileLock) {
      try {
        Path statusFile = databaseStatusDirectory.resolve(id + ".json");

        // verify if file exists
        if (FSUtils.exists(statusFile)) {
          final DatabaseStatus databaseStatus = JsonUtils.readObjectFromFile(statusFile, DatabaseStatus.class);
          final ValidationStatus validationStatus = databaseStatus.getValidationStatus();
          validationStatus.setValidationStatus(status);
          validationStatus.setCreatedOn(date);
          validationStatus.setReportLocation(validationReportPath);
          validationStatus.setValidatorVersion(dbptkVersion);
          databaseStatus.setValidationStatus(validationStatus);

          JsonTransformer.writeObjectToFile(databaseStatus, statusFile);
        }
      } catch (GenericException | ViewerException e) {
        LOGGER.debug(e.getMessage(), e);
      }
    }
  }

  public void addDatabaseStatus(DatabaseStatus status, Path databaseStatusDirectory) throws GenericException {
    synchronized (databaseStatusFileLock) {
      String filename = status.getId();
      Path statusFile = databaseStatusDirectory.resolve(filename + ".json");

      // verify if file exists
      if (!FSUtils.exists(statusFile)) {
        try {
          Files.createFile(statusFile);
        } catch (FileAlreadyExistsException e) {
          // do nothing (just caused due to concurrency)
        } catch (IOException e) {
          throw new GenericException("Error creating file to write the backup information", e);
        }
      } else {
        status = new DatabaseStatus(JsonUtils.readObjectFromFile(statusFile, DatabaseStatus.class));
      }

      // Save backup
      JsonUtils.writeObjectToFile(status, statusFile);
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
