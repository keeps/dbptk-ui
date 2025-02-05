/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.index.filter.BasicSearchFilterParameter;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.sort.Sorter;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.index.facets.Facets;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseValidationStatus;
import com.databasepreservation.common.exceptions.ViewerException;
import com.databasepreservation.common.server.index.utils.IterableDatabaseResult;
import org.apache.solr.client.solrj.SolrClient;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.server.activity.log.strategies.ActivityLogStrategyFactory;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.common.server.index.factory.SolrClientFactory;

import java.util.ArrayList;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerFactory {
  private static final Logger LOGGER = LoggerFactory.getLogger(ViewerFactory.class);

  private static SolrClient solrClient;
  private static DatabaseRowsSolrManager solrManager;
  private static ViewerConfiguration configuration;
  private static ConfigurationManager configurationManager;
  private static ActivityLogStrategyFactory activityLogStrategyFactory;
  private static boolean instantiated = false;

  private static synchronized void instantiate() {
    if (!instantiated) {
      configuration = ViewerConfiguration.getInstance();
      solrClient = SolrClientFactory.get().getSolrClient();
      solrManager = new DatabaseRowsSolrManager(solrClient);
      configurationManager = new ConfigurationManager();
      activityLogStrategyFactory = new ActivityLogStrategyFactory();
      try {
        checkIngestingDBs();
      } catch (GenericException | RequestNotValidException | ViewerException e) {
        LOGGER.error("Error checking for ingesting databases in initialization: " + e.getMessage());
      }
      instantiated = true;
    }
  }

  public static void checkIngestingDBs() throws RequestNotValidException, GenericException, ViewerException {
    Filter ingestingFilter = new Filter(new BasicSearchFilterParameter(ViewerConstants.SOLR_DATABASES_STATUS, ViewerDatabaseStatus.INGESTING.toString()));

    IterableDatabaseResult<ViewerDatabase> dataBases = solrManager.findAll(ViewerDatabase.class, ingestingFilter, Sorter.NONE, new ArrayList<>());

    for (ViewerDatabase db : dataBases) {
      solrManager.markDatabaseCollection(db.getUuid(), ViewerDatabaseStatus.ERROR);
      ViewerFactory.getConfigurationManager().updateDatabaseStatus(db.getUuid(), ViewerDatabaseStatus.ERROR);
    }
  }

  public static Integer getEnvInt(String name, Integer defaultValue) {
    Integer envInt;
    try {
      String envString = System.getenv(name);
      envInt = envString != null ? Integer.valueOf(envString) : defaultValue;
    } catch (NumberFormatException e) {
      envInt = defaultValue;
      LOGGER.error("Invalid value for " + name + ", using default " + defaultValue, e);
    }
    return envInt;
  }

  public static Boolean getEnvBoolean(String name, Boolean defaultValue) {
    Boolean envInt;
    String envString = System.getenv(name);
    envInt = envString != null ? Boolean.valueOf(envString) : defaultValue;
    return envInt;
  }

  public static DatabaseRowsSolrManager getSolrManager() {
    instantiate();
    return solrManager;
  }

  public static SolrClient getSolrClient() {
    instantiate();
    return solrClient;
  }

  public static ViewerConfiguration getViewerConfiguration() {
    instantiate();
    return configuration;
  }

  public static ConfigurationManager getConfigurationManager() {
    instantiate();
    return configurationManager;
  }

  public static ActivityLogStrategyFactory getActivityLogStrategyFactory() {
    instantiate();
    return activityLogStrategyFactory;
  }
}
