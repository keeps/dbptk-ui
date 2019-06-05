package com.databasepreservation.visualization.server.index.factory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import com.databasepreservation.visualization.exceptions.ViewerException;
import com.databasepreservation.visualization.server.ViewerConfiguration;
import com.databasepreservation.visualization.server.index.schema.Field;
import com.databasepreservation.visualization.server.index.schema.SolrBootstrapUtils;
import com.databasepreservation.visualization.server.index.schema.SolrDefaultCollectionRegistry;
import com.databasepreservation.visualization.server.index.schema.SolrRowsCollectionRegistry;

import org.apache.solr.client.solrj.SolrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SolrClientFactory<T extends SolrClient> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolrClientFactory.class);

    private static SolrClientFactory<? extends SolrClient> instance = null;

    public static synchronized SolrClientFactory<? extends SolrClient> get() {
        if (instance == null) {
            if (ViewerConfiguration.getInstance()
                    .getViewerConfigurationAsString("", ViewerConfiguration.PROPERTY_SOLR_ZOOKEEPER_HOSTS).isEmpty()) {
                instance = new EmbeddedSolrClientFactory();
            } else {
                // TODO get factory type from config
                instance = new CloudSolrClientFactory();
            }

            instance.init();
        }
        return instance;
    }

    private T solrClient;

    private void init() {
        ViewerConfiguration configuration = ViewerConfiguration.getInstance();
        Field.initialize(configuration);

        this.solrClient = configureSolrClient();

        waitForSolrToInitialize();


        try {
            bootstrapDefaultCollections();
            SolrBootstrapUtils.bootstrapSchemas(getSolrClient());
        } catch (ViewerException | IOException e) {
            LOGGER.error("Solr bootstrap failed", e);
        }
    }

    public T getSolrClient() {
        return solrClient;
    }

    private Path tempSolrConf = null;

    protected synchronized Path createTempSolrConfigurationDir() throws IOException {
        if (this.tempSolrConf == null) {
            this.tempSolrConf = Files.createTempDirectory("solr-config-");
            ViewerConfiguration.copyFilesFromClasspath(ViewerConfiguration.RESOURCES_SOLR_CONFIG_PATH + "/",
                    this.tempSolrConf, true);
        }
        return this.tempSolrConf;
    }

    protected void bootstrapDefaultCollections() throws IOException {

        Collection<String> existingCollections = getCollectionList();
        for (String collection : SolrDefaultCollectionRegistry.registryIndexNames()) {
            if (!existingCollections.contains(collection)) {
                createCollection(collection, createTempSolrConfigurationDir());
            }
        }

        SolrRowsCollectionRegistry.registerExisting(existingCollections);

    }

    protected abstract T configureSolrClient();

    protected abstract void waitForSolrToInitialize();

    protected abstract boolean createCollection(String collection, Path config);

    protected abstract Collection<String> getCollectionList();

    public boolean createCollection(String collection) {
        try {
            return createCollection(collection, createTempSolrConfigurationDir());
        } catch (IOException e) {
            LOGGER.error("Error creating collection {}", collection, e);
            return false;
        }
    }

}