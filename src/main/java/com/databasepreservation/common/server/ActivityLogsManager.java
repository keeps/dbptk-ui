package com.databasepreservation.common.server;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.databasepreservation.common.client.exceptions.RESTException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.models.activity.logs.ActivityLogEntry;
import com.databasepreservation.common.server.storage.fs.FSUtils;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ActivityLogsManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(ActivityLogsManager.class);

  private static final DateTimeFormatter LOG_NAME_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
  private final Object logFileLock = new Object();
  private long entryLogLineNumber = -1;

  public ActivityLogsManager() {

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
