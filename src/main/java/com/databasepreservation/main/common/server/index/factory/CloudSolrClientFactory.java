package com.databasepreservation.main.common.server.index.factory;

import java.io.IOException;
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

import com.databasepreservation.main.common.server.ViewerConfiguration;
import com.databasepreservation.main.common.server.ViewerFactory;

public class CloudSolrClientFactory extends SolrClientFactory<CloudSolrClient> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CloudSolrClientFactory.class);

    protected CloudSolrClientFactory() {
    }

    protected CloudSolrClient configureSolrClient() {
        String solrCloudZooKeeperUrls = ViewerConfiguration.getInstance()
                .getViewerConfigurationAsString("localhost:9983", ViewerConfiguration.PROPERTY_SOLR_ZOOKEEPER_HOSTS);

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

        return new CloudSolrClient.Builder(zkHosts, zkChroot).build();
    }

    protected void waitForSolrToInitialize() {
        ViewerConfiguration configuration = ViewerConfiguration.getInstance();
        int retries = configuration.getViewerConfigurationAsInt(100,
                ViewerConfiguration.PROPERTY_SOLR_HEALTHCHECK_RETRIES);
        long timeout = configuration.getViewerConfigurationAsInt(10000,
                ViewerConfiguration.PROPERTY_SOLR_HEALTHCHECK_TIMEOUT);

        boolean recovering;

        while ((recovering = !checkSolrCluster()) && retries > 0) {
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

    private boolean checkSolrCluster() {
        int connectTimeout = ViewerConfiguration.getInstance().getViewerConfigurationAsInt(60000,
                ViewerConfiguration.PROPERTY_SOLR_HEALTHCHECK_TIMEOUT);

        try {
            LOGGER.info("Connecting to Solr Cloud with a timeout of {} ms...", connectTimeout);
            getSolrClient().connect(connectTimeout, TimeUnit.MILLISECONDS);
            LOGGER.info("Connected to Solr Cloud");
        } catch (TimeoutException | InterruptedException e) {
            LOGGER.error("Could not connect to Solr Cloud", e);
        }

        ZkStateReader zkStateReader = getSolrClient().getZkStateReader();
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

    protected boolean createCollection(String collection, Path configPath) {
        boolean ret;
        try {
            LOGGER.info("Creating SOLR collection {}", collection);

            int numShards = ViewerFactory.getEnvInt("SOLR_NUM_SHARDS", 1);
            int numReplicas = ViewerFactory.getEnvInt("SOLR_REPLICATION_FACTOR", 1);

            getSolrClient().getZkStateReader().getZkClient().upConfig(configPath, collection);

            CollectionAdminRequest.Create createCollection = CollectionAdminRequest.createCollection(collection,
                    collection, numShards, numReplicas);
            createCollection.setMaxShardsPerNode(ViewerFactory.getEnvInt("SOLR_MAX_SHARDS_PER_NODE", 1));
            createCollection.setAutoAddReplicas(ViewerFactory.getEnvBoolean("SOLR_AUTO_ADD_REPLICAS", false));

            CollectionAdminResponse response = createCollection.process(getSolrClient());
            if (!response.isSuccess()) {
                LOGGER.error("Could not create collection {}: {}", collection, response.getErrorMessages());
                ret = false;
            } else {
                ret = true;
            }
        } catch (SolrServerException | SolrException | IOException e) {
            LOGGER.error("Error creating collection {}", collection, e);
            ret = false;
        }
        return ret;
    }

    @Override
    protected Collection<String> getCollectionList() {
        Collection<String> ret = new ArrayList<>();
        CollectionAdminRequest.List req = new CollectionAdminRequest.List();
        try {
            CollectionAdminResponse response = req.process(getSolrClient());

            ret = (List<String>) response.getResponse().get("collections");
            if (ret == null) {
                ret = new ArrayList<>();
            }
        } catch (SolrServerException | IOException e) {
            LOGGER.error("Could not retrieve list of collections", e);
        }
        return ret;

    }

}