/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.visualization.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.visualization.utils.SolrManager;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerFactory {
  private static final Logger LOGGER = LoggerFactory.getLogger(ViewerFactory.class);

  // TODO: get this information from somewhere else
  private static final String DEVELOPMENT_SOLR_URL = "http://127.0.0.1:8983/solr/";

  private static SolrManager solr;
  private static ViewerConfiguration configuration;
  private static boolean instantiated = false;

  private static void instantiate() {
    solr = new SolrManager(DEVELOPMENT_SOLR_URL);
    configuration = ViewerConfiguration.getInstance();
    instantiated = true;
  }

  public static SolrManager getSolrManager() {
    if (!instantiated) {
      instantiate();
    }
    return solr;
  }

  public static ViewerConfiguration getViewerConfiguration() {
    if (!instantiated) {
      instantiate();
    }
    return configuration;
  }
}
