/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.visualization.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.response.CollectionAdminResponse;
import org.apache.solr.cloud.ZkController;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.cloud.ClusterState;
import org.apache.solr.common.cloud.DocCollection;
import org.apache.solr.common.cloud.Replica;
import org.apache.solr.common.cloud.Slice;
import org.apache.solr.common.cloud.ZkStateReader;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.visualization.exceptions.ViewerException;
import com.databasepreservation.visualization.server.index.SolrManager;
import com.databasepreservation.visualization.server.index.schema.Field;
import com.databasepreservation.visualization.server.index.schema.SolrBootstrapUtils;
import com.databasepreservation.visualization.server.index.schema.SolrDefaultCollectionRegistry;
import com.databasepreservation.visualization.server.index.schema.SolrRowsCollectionRegistry;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerFactory {
  private static final Logger LOGGER = LoggerFactory.getLogger(ViewerFactory.class);

  private static SolrManager solrManager;
  private static CloudSolrClient cloudSolrClient;
  private static ViewerConfiguration configuration;
  private static boolean instantiated = false;

  private static synchronized void instantiate() {
    if(!instantiated) {
      configuration = ViewerConfiguration.getInstance();
      instantiateSolrCloud();
      instantiated = true;
    }
  }

  private static void instantiateSolrCloud() {
    Field.initialize(configuration);

    String solrCloudZooKeeperUrls = configuration.getViewerConfigurationAsString("localhost:9983",
      ViewerConfiguration.PROPERTY_SOLR_ZOOKEEPER_HOSTS);

    LOGGER.info("Instantiating SOLR Cloud at {}", solrCloudZooKeeperUrls);

    try {
      ZkController.checkChrootPath(solrCloudZooKeeperUrls, true);
    } catch (KeeperException | InterruptedException e) {
      LOGGER.error("Could not check zookeeper chroot path", e);
    }

    List<String> zkHosts;
    Optional<String> zkChroot;

    // parse config
    int indexOfSlash = solrCloudZooKeeperUrls.indexOf('/');

    if (indexOfSlash > 0) {
      // has chroot
      zkHosts = Arrays.asList(solrCloudZooKeeperUrls.substring(0, indexOfSlash).split(","));
      zkChroot = Optional.of(solrCloudZooKeeperUrls.substring(indexOfSlash));
    } else {
      // does not have chroot
      zkHosts = Arrays.asList(solrCloudZooKeeperUrls.split(","));
      zkChroot = Optional.empty();
    }

    cloudSolrClient = new CloudSolrClient.Builder(zkHosts, zkChroot).build();

    waitForSolrCluster(cloudSolrClient);

    bootstrap(cloudSolrClient);

    try {
      SolrBootstrapUtils.bootstrapSchemas(cloudSolrClient);
    } catch (ViewerException e) {
      LOGGER.error("Solr bootstrap failed", e);
    }

    solrManager = new SolrManager(cloudSolrClient);
  }

  private static void waitForSolrCluster(CloudSolrClient cloudSolrClient) {
    int retries = configuration.getViewerConfigurationAsInt(100, ViewerConfiguration.PROPERTY_SOLR_HEALTHCHECK_RETRIES);
    long timeout = configuration.getViewerConfigurationAsInt(10000,
      ViewerConfiguration.PROPERTY_SOLR_HEALTHCHECK_TIMEOUT);

    boolean recovering;

    while ((recovering = !checkSolrCluster(cloudSolrClient)) && retries > 0) {
      LOGGER.info("Solr Cluster not yet ready, waiting {}ms, retries: {}", timeout, retries);
      retries--;
      try {
        Thread.sleep(timeout);
      } catch (InterruptedException e) {
        LOGGER.warn("Sleep interrupted");
      }
    }

    if (recovering) {
      LOGGER.error("Timeout while waiting for Solr Cluster to recover collections");
    }
  }

  private static boolean checkSolrCluster(CloudSolrClient cloudSolrClient) {
    int connectTimeout = ViewerConfiguration.getInstance().getViewerConfigurationAsInt(60000,
      ViewerConfiguration.PROPERTY_SOLR_HEALTHCHECK_TIMEOUT);

    try {
      LOGGER.info("Connecting to Solr Cloud with a timeout of {} ms...", connectTimeout);
      cloudSolrClient.connect(connectTimeout, TimeUnit.MILLISECONDS);
      LOGGER.info("Connected to Solr Cloud");
    } catch (TimeoutException | InterruptedException e) {
      LOGGER.error("Could not connect to Solr Cloud", e);
    }

    ZkStateReader zkStateReader = cloudSolrClient.getZkStateReader();
    ClusterState clusterState = zkStateReader.getClusterState();

    Map<String, DocCollection> collectionStates = clusterState.getCollectionsMap();
    Set<String> allCollections = new HashSet<>();
    Set<String> healthyCollections = new HashSet<>();

    boolean healthy;

    for (Map.Entry<String, DocCollection> entry : collectionStates.entrySet()) {
      String col = entry.getKey();
      DocCollection docs = entry.getValue();

      if (docs != null) {
        allCollections.add(col);

        Collection<Slice> slices = docs.getActiveSlices();

        boolean collectionHealthy = true;
        // collection healthy if all slices are healthy

        for (Slice slice : slices) {
          boolean sliceHealthy = false;

          // if at least one replica is active then the slice is healthy
          for (Replica replica : slice.getReplicas()) {
            if (Replica.State.ACTIVE.equals(replica.getState())) {
              sliceHealthy = true;
              break;
            } else {
              LOGGER.info("Replica {} on node {} is {}", replica.getName(), replica.getNodeName(),
                replica.getState().name());
            }
          }

          collectionHealthy &= sliceHealthy;
        }

        if (collectionHealthy) {
          healthyCollections.add(col);
        }
      }
    }

    if (healthyCollections.containsAll(allCollections)) {
      healthy = true;
      LOGGER.info("All available Solr Cloud collections are healthy, collections: {}", healthyCollections);
    } else {
      healthy = false;

      Set<String> unhealthyCollections = new HashSet<>(allCollections);
      unhealthyCollections.removeAll(healthyCollections);

      LOGGER.info("Solr Cloud healthy collections:   " + healthyCollections);
      LOGGER.info("Solr Cloud unhealthy collections: " + unhealthyCollections);
    }

    return healthy;
  }

  private static void bootstrap(CloudSolrClient cloudSolrClient) {
    CollectionAdminRequest.List req = new CollectionAdminRequest.List();
    try {
      CollectionAdminResponse response = req.process(cloudSolrClient);

      List<String> existingCollections = (List<String>) response.getResponse().get("collections");
      if (existingCollections == null) {
        existingCollections = new ArrayList<>();
      }

      Path tempSolrConf = createTempSolrConfigurationDir();

      for (String collection : SolrDefaultCollectionRegistry.registryIndexNames()) {
        if (!existingCollections.contains(collection)) {
          createCollection(cloudSolrClient, collection, tempSolrConf);
        }
      }

      SolrRowsCollectionRegistry.registerExisting(existingCollections);

    } catch (SolrServerException | IOException e) {
      LOGGER.error("Solr bootstrap failed", e);
    } finally {
      // TODO: remover pasta temporaria que tem as configs
    }
  }

  public static Path createTempSolrConfigurationDir() throws IOException {
    Path tempSolrConf = Files.createTempDirectory("solr-config-");
    ViewerConfiguration.copyFilesFromClasspath(ViewerConfiguration.RESOURCES_SOLR_CONFIG_PATH + "/", tempSolrConf,
      true);
    return tempSolrConf;
  }

  public static boolean createCollection(String collection, Path configPath) {
    return createCollection(getSolrClient(), collection, configPath);
  }

  private static boolean createCollection(CloudSolrClient cloudSolrClient, String collection, Path configPath) {
    boolean success = false;
    try {
      LOGGER.info("Creating SOLR collection {}", collection);

      int numShards = getEnvInt("SOLR_NUM_SHARDS", 1);
      int numReplicas = getEnvInt("SOLR_REPLICATION_FACTOR", 1);

      cloudSolrClient.getZkStateReader().getZkClient().upConfig(configPath, collection);

      CollectionAdminRequest.Create createCollection = CollectionAdminRequest.createCollection(collection, collection,
        numShards, numReplicas);
      createCollection.setMaxShardsPerNode(getEnvInt("SOLR_MAX_SHARDS_PER_NODE", 1));
      createCollection.setAutoAddReplicas(getEnvBoolean("SOLR_AUTO_ADD_REPLICAS", false));

      CollectionAdminResponse response = createCollection.process(cloudSolrClient);
      if (!response.isSuccess()) {
        LOGGER.error("Could not create collection {}: {}", collection, response.getErrorMessages());
      } else {
        success = true;
      }
    } catch (SolrServerException | SolrException | IOException e) {
      LOGGER.error("Error creating collection {}", collection, e);
    }
    return success;
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

  public static SolrManager getSolrManager() {
    instantiate();
    return solrManager;
  }

  public static CloudSolrClient getSolrClient() {
    instantiate();
    return cloudSolrClient;
  }

  public static ViewerConfiguration getViewerConfiguration() {
    instantiate();
    return configuration;
  }
}
