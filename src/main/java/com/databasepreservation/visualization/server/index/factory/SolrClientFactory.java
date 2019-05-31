package com.databasepreservation.visualization.server.index.factory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.visualization.exceptions.ViewerException;
import com.databasepreservation.visualization.server.ViewerConfiguration;
import com.databasepreservation.visualization.server.ViewerFactory;
import com.databasepreservation.visualization.server.index.schema.Field;
import com.databasepreservation.visualization.server.index.schema.SolrBootstrapUtils;
import com.databasepreservation.visualization.server.index.schema.SolrDefaultCollectionRegistry;
import com.databasepreservation.visualization.server.index.schema.SolrRowsCollectionRegistry;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.response.CollectionAdminResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SolrClientFactory<T extends SolrClient> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolrClientFactory.class);

    private static SolrClientFactory<? extends SolrClient> instance = null;

    public static synchronized SolrClientFactory<? extends SolrClient> get() {
        if (instance == null) {
            // TODO get factory type from config
            instance = new CloudSolrClientFactory();
            instance.init();
        }
        return instance;
    }

    private T solrClient;

    private void init() {
        ViewerConfiguration configuration = ViewerFactory.getViewerConfiguration();
        Field.initialize(configuration);
        
        this.solrClient = configureSolrClient();
        
        waitForSolrToInitialize();
        
        bootstrapDefaultCollections();
        
        try {
            SolrBootstrapUtils.bootstrapSchemas(getSolrClient());
        } catch (ViewerException e) {
            LOGGER.error("Solr bootstrap failed", e);
        }
    }
    
    public T getSolrClient() {
        return solrClient;
    }

    private Path tempSolrConf = null;

    protected synchronized Path createTempSolrConfigurationDir() throws IOException {
        if (tempSolrConf == null) {
            Path tempSolrConf = Files.createTempDirectory("solr-config-");
            ViewerConfiguration.copyFilesFromClasspath(ViewerConfiguration.RESOURCES_SOLR_CONFIG_PATH + "/",
                    tempSolrConf, true);
        }
        return tempSolrConf;
    }

    protected void bootstrapDefaultCollections() {
        CollectionAdminRequest.List req = new CollectionAdminRequest.List();
        try {
            CollectionAdminResponse response = req.process(getSolrClient());

            List<String> existingCollections = (List<String>) response.getResponse().get("collections");
            if (existingCollections == null) {
                existingCollections = new ArrayList<>();
            }

            Path tempSolrConf = createTempSolrConfigurationDir();

            for (String collection : SolrDefaultCollectionRegistry.registryIndexNames()) {
                if (!existingCollections.contains(collection)) {
                    createCollection(collection, tempSolrConf);
                }
            }

            SolrRowsCollectionRegistry.registerExisting(existingCollections);

        } catch (SolrServerException | IOException e) {
            LOGGER.error("Solr bootstrap failed", e);
        }
    }

    protected abstract T configureSolrClient();

    protected abstract void waitForSolrToInitialize();

    protected abstract boolean createCollection(String collection, Path config);

    public boolean createCollection(String collection) {
        try {
            return createCollection(collection, createTempSolrConfigurationDir());
        } catch (IOException e) {
            LOGGER.error("Error creating collection {}", collection, e);
            return false;
        }
    }

    
}