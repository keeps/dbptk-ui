package com.databasepreservation.visualization.shared;

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

  private static boolean instantiated = false;
  private static SolrManager solr;

  private static void instantiate(String solrUrl) {
    solr = new SolrManager(solrUrl);
    instantiated = true;
  }

  public static SolrManager getSolrManager() {
    if (!instantiated) {
      instantiate(DEVELOPMENT_SOLR_URL);
    }
    return solr;
  }
}
