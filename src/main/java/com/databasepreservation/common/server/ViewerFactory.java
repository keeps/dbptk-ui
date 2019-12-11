/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.common.server;

import org.apache.solr.client.solrj.SolrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.common.server.index.factory.SolrClientFactory;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerFactory {
  private static final Logger LOGGER = LoggerFactory.getLogger(ViewerFactory.class);

  private static SolrClient solrClient;
  private static DatabaseRowsSolrManager solrManager;
  private static ViewerConfiguration configuration;
  private static ActivityLogsManager activityLogsManager;
  private static boolean instantiated = false;

  private static synchronized void instantiate() {
    if (!instantiated) {
      configuration = ViewerConfiguration.getInstance();
      solrClient = SolrClientFactory.get().getSolrClient();
      solrManager = new DatabaseRowsSolrManager(solrClient);
      activityLogsManager = new ActivityLogsManager();
      instantiated = true;
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

  public static ActivityLogsManager getActivityLogsManager() {
    instantiate();
    return activityLogsManager;
  }
}
