/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.configuration2.builder.DefaultReloadingDetectorFactory;
import org.apache.commons.configuration2.builder.FileBasedBuilderParametersImpl;
import org.apache.commons.configuration2.builder.ReloadingDetectorFactory;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.reloading.ReloadingDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.filter.BasicSearchFilterParameter;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.sort.Sorter;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;
import com.databasepreservation.common.server.index.factory.SolrClientFactory;
import com.databasepreservation.common.server.index.utils.IterableDatabaseResult;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class IngestionGuardedReloadingDetectorFactory implements ReloadingDetectorFactory {
  private static final Logger LOGGER = LoggerFactory.getLogger(IngestionGuardedReloadingDetectorFactory.class);

  private final ReloadingDetectorFactory defaultFactory = new DefaultReloadingDetectorFactory();

  @Override
  public ReloadingDetector createReloadingDetector(FileHandler handler, FileBasedBuilderParametersImpl params)
    throws ConfigurationException {
    final ReloadingDetector fileDetector = defaultFactory.createReloadingDetector(handler, params);
    return new ReloadingDetector() {
      @Override
      public boolean isReloadingRequired() {
        // only query the index when the file has actually changed
        return fileDetector.isReloadingRequired() && !isAnyDatabaseIngesting();
      }

      @Override
      public void reloadingPerformed() {
        fileDetector.reloadingPerformed();
      }
    };
  }

  private static boolean isAnyDatabaseIngesting() {
    // when ingesting, postpone the reload. Exceptions must not escape, as they
    // would stop the periodic reloading trigger
    boolean ingesting = true;
    DatabaseRowsSolrManager solrManager = new DatabaseRowsSolrManager(SolrClientFactory.get().getSolrClient());
    Filter ingestingFilter = new Filter(
      new BasicSearchFilterParameter(ViewerConstants.SOLR_DATABASES_STATUS, ViewerDatabaseStatus.INGESTING.toString()));
    try (IterableDatabaseResult<ViewerDatabase> ingestingDatabases = solrManager.findAll(ViewerDatabase.class,
      ingestingFilter, Sorter.NONE, new ArrayList<>())) {
      ingesting = ingestingDatabases.getTotalCount() > 0;
      if (ingesting) {
        StringBuilder databaseUUIDs = new StringBuilder("{");
        ingestingDatabases.forEach(d -> databaseUUIDs.append(d.getUuid()).append(";"));
        databaseUUIDs.append("}");
        LOGGER.info("Postponing configuration reload because databases are currently being ingested: {}",
          databaseUUIDs);
      }
    } catch (IOException e) {
      LOGGER.warn("Error closing index results.");
    } catch (RuntimeException e) {
      LOGGER.warn("Could not check for databases being ingested, postponing configuration reload", e);
    }
    return ingesting;
  }
}